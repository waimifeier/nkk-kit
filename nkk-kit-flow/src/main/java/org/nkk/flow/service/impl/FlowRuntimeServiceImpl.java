package org.nkk.flow.service.impl;

import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.core.context.FlowExecution;
import org.nkk.flow.core.extension.id.FlowIdGenerator;
import org.nkk.flow.core.extension.identity.FlowInstanceAccessStrategy;
import org.nkk.flow.dao.FlowExtInstanceDao;
import org.nkk.flow.dao.FlowHisInstanceDao;
import org.nkk.flow.dao.FlowInstanceDao;
import org.nkk.flow.dao.FlowTaskDao;
import org.nkk.flow.entity.FlowExtInstance;
import org.nkk.flow.entity.FlowHisInstance;
import org.nkk.flow.entity.FlowInstance;
import org.nkk.flow.entity.FlowProcess;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.enums.runtime.FlowEventTypeEnum;
import org.nkk.flow.enums.runtime.FlowExecuteTypeEnum;
import org.nkk.flow.enums.runtime.FlowInstanceOperateEnum;
import org.nkk.flow.enums.core.FlowInstanceEnum.InstanceState;
import org.nkk.flow.enums.core.FlowTaskEnum.TaskState;
import org.nkk.flow.model.node.FlowNodeModel;
import org.nkk.flow.model.FlowProcessModel;
import org.nkk.flow.service.FlowRuntimeService;
import org.nkk.flow.service.FlowTaskService;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.Function;

/**
 * 默认流程实例服务。
 */
public class FlowRuntimeServiceImpl implements FlowRuntimeService {

    private final FlowContext context;
    private final FlowIdGenerator idGenerator;
    private final FlowInstanceDao instanceDao;
    private final FlowHisInstanceDao hisInstanceDao;
    private final FlowExtInstanceDao extInstanceDao;
    private final FlowTaskDao taskDao;
    private final FlowTaskService taskService;

    public FlowRuntimeServiceImpl(FlowContext context, FlowIdGenerator idGenerator, FlowInstanceDao instanceDao,
                                  FlowHisInstanceDao hisInstanceDao, FlowExtInstanceDao extInstanceDao,
                                  FlowTaskDao taskDao, FlowTaskService taskService) {
        this.context = context;
        this.idGenerator = idGenerator;
        this.instanceDao = instanceDao;
        this.hisInstanceDao = hisInstanceDao;
        this.extInstanceDao = extInstanceDao;
        this.taskDao = taskDao;
        this.taskService = taskService;
    }

    @Override
    public FlowInstance createInstance(FlowProcess process, FlowCreator creator, Map<String, Object> args,
                                       FlowNodeModel startNode, Long parentInstanceId) {
        return createInstance(process, creator, args, startNode, parentInstanceId, false, null);
    }

    @Override
    public FlowInstance createInstance(FlowProcess process, FlowCreator creator, Map<String, Object> args,
                                       FlowNodeModel startNode, Long parentInstanceId, boolean saveAsDraft,
                                       Supplier<FlowInstance> supplier) {
        FlowInstance instance = new FlowInstance();
        if (supplier != null) {
            FlowInstance supplied = supplier.get();
            if (supplied != null) {
                instance = supplied;
            }
        }
        instance.setId(idGenerator.nextId(instance.getId()));
        instance.setTenantId(creator.getTenantId());
        instance.setCreateId(creator.getCreateId());
        instance.setCreateBy(creator.getCreateBy());
        instance.setCreateTime(context.getCreateTimeHandler().getCurrentTime(FlowExecuteTypeEnum.START, null, null));
        instance.setProcessId(process.getId());
        instance.setProcessKey(process.getProcessKey());
        instance.setParentInstanceId(parentInstanceId);
        instance.setCurrentNodeName(startNode.getNodeName());
        instance.setCurrentNodeKey(startNode.getNodeKey());
        instance.setLastUpdateBy(creator.getCreateBy());
        instance.setLastUpdateTime(instance.getCreateTime());
        instance.putAllVariable(args);
        instanceDao.insert(instance);

        FlowHisInstance his = FlowHisInstance.of(instance, saveAsDraft ? InstanceState.DRAFT : InstanceState.ACTIVE);
        hisInstanceDao.insert(his);
        extInstanceDao.insert(FlowExtInstance.of(instance, process));
        notifyInstance(FlowEventTypeEnum.INSTANCE_STARTED, his, startNode, creator);
        return instance;
    }

    @Override
    public FlowProcessModel getProcessModelByInstanceId(Long instanceId) {
        String cacheKey = modelCacheKey(instanceId);
        FlowProcessModel cached = context.getCachedProcessModel(cacheKey);
        if (cached != null) {
            return cached;
        }
        FlowExtInstance ext = extInstanceDao.selectById(instanceId);
        if (ext == null) {
            throw new IllegalArgumentException("流程实例模型不存在，instanceId=" + instanceId);
        }
        FlowProcessModel model = ext.model();
        context.cacheProcessModel(cacheKey, model);
        return model;
    }

    @Override
    public boolean updateInstanceModelById(Long instanceId, FlowProcessModel processModel) {
        if (instanceId == null || processModel == null) {
            return false;
        }
        FlowExtInstance ext = new FlowExtInstance();
        ext.setId(instanceId);
        ext.setModelContent(FlowContext.toJson(processModel.cleanParentNode()));
        boolean updated = extInstanceDao.updateById(ext);
        processModel.buildParentNode();
        context.cacheProcessModel(modelCacheKey(instanceId), processModel);
        if (updated) {
            notifyInstance(FlowEventTypeEnum.INSTANCE_MODEL_UPDATED, instanceId, null, context.getSystemCreator());
        }
        return updated;
    }

    @Override
    public void appendNodeModel(Long taskId, FlowNodeModel nodeModel, boolean beforeAfter) {
        FlowTask task = taskDao.selectById(taskId);
        if (task == null || nodeModel == null) {
            return;
        }
        FlowProcessModel model = getProcessModelByInstanceId(task.getInstanceId());
        boolean changed = beforeAfter
                ? model.getNodeConfig().insertBefore(task.getTaskKey(), nodeModel)
                : model.getNodeConfig().insertAfter(task.getTaskKey(), nodeModel);
        if (changed) {
            updateInstanceModelById(task.getInstanceId(), model);
            notifyInstance(FlowEventTypeEnum.INSTANCE_NODE_APPENDED, task.getInstanceId(), nodeModel, context.getSystemCreator());
        }
    }

    @Override
    public boolean removeNodeModel(Long instanceId, String nodeKey, Function<FlowNodeModel, Boolean> checkFunc) {
        FlowProcessModel model = getProcessModelByInstanceId(instanceId);
        FlowNodeModel node = model.getNode(nodeKey);
        if (node == null) {
            return false;
        }
        if (checkFunc != null && !Boolean.TRUE.equals(checkFunc.apply(node))) {
            return false;
        }
        boolean removed = model.getNodeConfig().removeNode(nodeKey);
        boolean updated = removed && updateInstanceModelById(instanceId, model);
        if (updated) {
            notifyInstance(FlowEventTypeEnum.INSTANCE_NODE_REMOVED, instanceId, node, context.getSystemCreator());
        }
        return updated;
    }

    @Override
    public boolean endInstance(FlowExecution execution, Long instanceId, FlowNodeModel endNode, InstanceState state) {
        FlowInstance instance = instanceDao.selectById(instanceId);
        if (instance == null) {
            return true;
        }
        List<FlowTask> activeTasks = taskDao.selectListByInstanceId(instanceId);
        if (activeTasks != null && !activeTasks.isEmpty()) {
            return true;
        }
        instanceDao.deleteById(instanceId);
        FlowHisInstance his = FlowHisInstance.of(instance, state);
        his.setCurrentNodeKey(endNode.getNodeKey());
        his.setCurrentNodeName(endNode.getNodeName());
        his.setEndTime(new Date());
        hisInstanceDao.updateById(his);
        finishParentCallProcess(instance, his, state);
        notifyInstance(eventTypeOf(state), his, endNode, execution.getFlowCreator());
        return true;
    }

    @Override
    public boolean terminate(Long instanceId, FlowCreator creator) {
        return terminate(instanceId, null, creator);
    }

    @Override
    public boolean terminate(Long instanceId, FlowTask currentTask, FlowCreator creator) {
        assertInstanceOperationAllowed(instanceId, creator, FlowInstanceOperateEnum.TERMINATE,
                "当前用户无权终止流程");
        return finishInstance(instanceId, currentTask, creator, InstanceState.TERMINATED, TaskState.TERMINATED);
    }

    @Override
    public boolean suspendInstanceById(Long instanceId, FlowCreator creator) {
        assertInstanceOperationAllowed(instanceId, creator, FlowInstanceOperateEnum.SUSPEND,
                "当前用户无权挂起流程");
        return changeHisInstanceState(instanceId, creator, InstanceState.ACTIVE, InstanceState.SUSPENDED);
    }

    @Override
    public boolean activeInstanceById(Long instanceId, FlowCreator creator) {
        assertInstanceOperationAllowed(instanceId, creator, FlowInstanceOperateEnum.ACTIVE,
                "当前用户无权激活流程");
        return changeHisInstanceState(instanceId, creator, InstanceState.SUSPENDED, InstanceState.ACTIVE);
    }

    @Override
    public boolean reject(Long instanceId, FlowTask currentTask, FlowCreator creator) {
        return finishInstance(instanceId, currentTask, creator, InstanceState.REJECTED, TaskState.REJECTED);
    }

    @Override
    public boolean revoke(Long instanceId, FlowTask currentTask, FlowCreator creator) {
        assertInstanceOperationAllowed(instanceId, creator, FlowInstanceOperateEnum.REVOKE,
                "当前用户无权撤回流程");
        return finishInstance(instanceId, currentTask, creator, InstanceState.REVOKED, TaskState.REVOKED);
    }

    @Override
    public boolean timeout(Long instanceId, FlowTask currentTask, FlowCreator creator) {
        assertInstanceOperationAllowed(instanceId, creator, FlowInstanceOperateEnum.TIMEOUT,
                "当前用户无权处理超时流程");
        return finishInstance(instanceId, currentTask, creator, InstanceState.TIMEOUT, TaskState.TIMEOUT);
    }

    @Override
    public boolean destroyByInstanceId(Long instanceId, FlowCreator creator, Map<String, Object> args) {
        assertInstanceOperationAllowed(instanceId, creator, FlowInstanceOperateEnum.DESTROY,
                "当前用户无权作废流程");
        FlowInstance instance = instanceDao.selectById(instanceId);
        if (instance != null) {
            FlowHisInstance his = hisInstanceDao.selectById(instanceId);
            if (his == null || InstanceState.DESTROYED.value().equals(his.getInstanceState())) {
                return false;
            }
            instance.putAllVariable(args);
            return finishInstance(instanceId, null, creator, InstanceState.DESTROYED, TaskState.DESTROYED);
        }
        FlowHisInstance his = hisInstanceDao.selectById(instanceId);
        if (his == null || InstanceState.DESTROYED.value().equals(his.getInstanceState())) {
            return false;
        }
        his.putAllVariable(args);
        his.setInstanceState(InstanceState.DESTROYED.value());
        his.setEndTime(context.getCreateTimeHandler().getFinishTime(instanceId, null));
        boolean updated = hisInstanceDao.updateById(his);
        if (updated) {
            notifyInstance(FlowEventTypeEnum.INSTANCE_DESTROYED, his, null,
                    creator == null ? context.getSystemCreator() : creator);
        }
        return updated;
    }

    @Override
    public boolean addVariable(Long instanceId, Map<String, Object> args, Function<FlowInstance, FlowInstance> function) {
        FlowInstance instance = instanceDao.selectById(instanceId);
        if (instance == null) {
            return false;
        }
        instance.putAllVariable(args);
        FlowInstance update = function == null ? instance : function.apply(instance);
        if (update == null) {
            update = instance;
        }
        update.setId(instanceId);
        boolean updated = instanceDao.updateById(update);
        FlowHisInstance his = new FlowHisInstance();
        his.setId(instanceId);
        his.setVariable(update.getVariable());
        hisInstanceDao.updateById(his);
        if (updated) {
            notifyInstance(FlowEventTypeEnum.INSTANCE_VARIABLE_UPDATED, instanceId, null, context.getSystemCreator());
        }
        return updated;
    }

    @Override
    public void cascadeRemoveByInstanceId(Long instanceId, FlowCreator creator) {
        if (instanceId == null) {
            return;
        }
        java.util.List<Long> ids = collectInstanceIds(instanceId);
        for (Long id : ids) {
            notifyInstance(FlowEventTypeEnum.INSTANCE_CASCADE_REMOVED, id, null,
                    creator == null ? context.getSystemCreator() : creator);
        }
        taskService.cascadeRemoveByInstanceIds(ids);
        extInstanceDao.deleteByIds(ids);
        instanceDao.deleteByIds(ids);
        hisInstanceDao.deleteByIds(ids);
        for (Long id : ids) {
            context.invalidateProcessModel(modelCacheKey(id));
        }
    }

    @Override
    public void cascadeRemoveByProcessId(Long processId) {
        List<FlowHisInstance> instances = hisInstanceDao.selectListByProcessId(processId);
        if (instances == null || instances.isEmpty()) {
            return;
        }
        for (FlowHisInstance instance : instances) {
            cascadeRemoveByInstanceId(instance.getId(), context.getSystemCreator());
        }
    }

    private boolean finishInstance(Long instanceId, FlowTask currentTask, FlowCreator creator,
                                   InstanceState instanceState, TaskState taskState) {
        FlowInstance instance = instanceDao.selectById(instanceId);
        if (instance == null) {
            return false;
        }
        FlowInstance top = getTopParentInstance(instance);
        List<FlowInstance> instances = collectInstances(top);
        for (FlowInstance item : instances) {
            FlowTask matchedTask = currentTask != null && item.getId().equals(currentTask.getInstanceId()) ? currentTask : null;
            finishSingleInstance(item, matchedTask, creator, instanceState, taskState);
        }
        return true;
    }

    private boolean finishSingleInstance(FlowInstance instance, FlowTask currentTask, FlowCreator creator,
                                         InstanceState instanceState, TaskState taskState) {
        taskService.completeActiveTasksForJump(instance.getId(), currentTask, creator,
                currentTask == null ? null : currentTask.variableToMap(), taskState,
                currentTask == null ? null : currentTask.getOpinion());
        instanceDao.deleteById(instance.getId());
        FlowHisInstance his = FlowHisInstance.of(instance, instanceState);
        if (currentTask != null) {
            his.putAllVariable(currentTask.variableToMap());
            his.setCurrentNodeKey(currentTask.getTaskKey());
            his.setCurrentNodeName(currentTask.getTaskName());
        }
        his.setEndTime(context.getCreateTimeHandler().getFinishTime(instance.getId(), null));
        boolean updated = hisInstanceDao.updateById(his);
        if (updated) {
            notifyInstance(eventTypeOf(instanceState), his, null, creator);
        }
        return updated;
    }

    private boolean changeHisInstanceState(Long instanceId, FlowCreator creator, InstanceState expectedState,
                                           InstanceState targetState) {
        FlowInstance instance = instanceDao.selectById(instanceId);
        if (instance == null) {
            return false;
        }
        FlowHisInstance current = hisInstanceDao.selectById(instanceId);
        if (current == null || !expectedState.value().equals(current.getInstanceState())) {
            return false;
        }
        FlowHisInstance his = new FlowHisInstance();
        his.setId(instanceId);
        his.setInstanceState(targetState.value());
        his.setLastUpdateBy(creator == null ? null : creator.getCreateBy());
        his.setLastUpdateTime(context.getCreateTimeHandler().getCurrentTime(FlowExecuteTypeEnum.UPDATE_INSTANCE, instanceId, null));
        boolean updated = hisInstanceDao.updateById(his);
        if (updated) {
            notifyInstance(eventTypeOf(targetState), instanceId, null, creator);
        }
        return updated;
    }

    private void assertInstanceOperationAllowed(Long instanceId, FlowCreator creator, FlowInstanceOperateEnum operateType,
                                                String message) {
        FlowInstanceAccessStrategy strategy = context.getInstanceAccessStrategy();
        if (strategy == null || instanceId == null) {
            return;
        }
        FlowInstance instance = instanceDao.selectById(instanceId);
        FlowHisInstance hisInstance = hisInstanceDao.selectById(instanceId);
        if (instance == null && hisInstance == null) {
            return;
        }
        if (!strategy.isAllowed(creator, instance, hisInstance, operateType)) {
            throw new IllegalStateException(message + "，instanceId=" + instanceId);
        }
    }

    @Override
    public void updateCurrentNode(FlowTask task) {
        FlowInstance instance = new FlowInstance();
        instance.setId(task.getInstanceId());
        instance.setCurrentNodeName(task.getTaskName());
        instance.setCurrentNodeKey(task.getTaskKey());
        instance.setLastUpdateBy(task.getCreateBy());
        instance.setLastUpdateTime(new Date());
        instanceDao.updateById(instance);

        FlowHisInstance hisInstance = new FlowHisInstance();
        hisInstance.setId(task.getInstanceId());
        hisInstance.setCurrentNodeName(task.getTaskName());
        hisInstance.setCurrentNodeKey(task.getTaskKey());
        hisInstance.setLastUpdateBy(task.getCreateBy());
        hisInstance.setLastUpdateTime(instance.getLastUpdateTime());
        hisInstanceDao.updateById(hisInstance);
    }

    @Override
    public FlowInstance resume(Long instanceId, FlowCreator creator, Map<String, Object> args) {
        FlowInstance active = instanceDao.selectById(instanceId);
        FlowHisInstance his = hisInstanceDao.selectById(instanceId);
        if (his == null) {
            throw new IllegalArgumentException("历史流程实例不存在，instanceId=" + instanceId);
        }
        if (active != null) {
            active.putAllVariable(args);
            active.setLastUpdateBy(creator.getCreateBy());
            active.setLastUpdateTime(new Date());
            instanceDao.updateById(active);

            if (InstanceState.DRAFT.value().equals(his.getInstanceState())) {
                FlowHisInstance update = new FlowHisInstance();
                update.setId(instanceId);
                update.setVariable(active.getVariable());
                update.setInstanceState(InstanceState.ACTIVE.value());
                update.setEndTime(null);
                update.setLastUpdateBy(creator.getCreateBy());
                update.setLastUpdateTime(active.getLastUpdateTime());
                hisInstanceDao.updateById(update);
                notifyInstance(FlowEventTypeEnum.INSTANCE_RESUMED, update, null, creator);
            }
            return active;
        }
        FlowInstance instance = new FlowInstance();
        instance.setId(his.getId());
        instance.setTenantId(his.getTenantId());
        instance.setCreateId(his.getCreateId());
        instance.setCreateBy(his.getCreateBy());
        instance.setCreateTime(his.getCreateTime());
        instance.setProcessId(his.getProcessId());
        instance.setParentInstanceId(his.getParentInstanceId());
        instance.setPriority(his.getPriority());
        instance.setInstanceNo(his.getInstanceNo());
        instance.setBusinessKey(his.getBusinessKey());
        instance.setVariable(his.getVariable());
        instance.putAllVariable(args);
        instance.setCurrentNodeName(his.getCurrentNodeName());
        instance.setCurrentNodeKey(his.getCurrentNodeKey());
        instance.setExpireTime(his.getExpireTime());
        instance.setLastUpdateBy(creator.getCreateBy());
        instance.setLastUpdateTime(new Date());
        instanceDao.insert(instance);

        FlowHisInstance update = new FlowHisInstance();
        update.setId(instanceId);
        update.setInstanceState(InstanceState.ACTIVE.value());
        update.setEndTime(null);
        update.setLastUpdateBy(creator.getCreateBy());
        update.setLastUpdateTime(instance.getLastUpdateTime());
        hisInstanceDao.updateById(update);
        notifyInstance(FlowEventTypeEnum.INSTANCE_RESUMED, update, null, creator);
        return instance;
    }

    private FlowEventTypeEnum eventTypeOf(InstanceState state) {
        if (state == null) {
            return FlowEventTypeEnum.INSTANCE_ENDED;
        }
        switch (state) {
            case ACTIVE:
                return FlowEventTypeEnum.INSTANCE_ACTIVATED;
            case SUSPENDED:
                return FlowEventTypeEnum.INSTANCE_SUSPENDED;
            case COMPLETED:
            case AUTO_PASS:
                return FlowEventTypeEnum.INSTANCE_ENDED;
            case REJECTED:
            case AUTO_REJECT:
                return FlowEventTypeEnum.INSTANCE_REJECTED;
            case REVOKED:
                return FlowEventTypeEnum.INSTANCE_REVOKED;
            case TIMEOUT:
                return FlowEventTypeEnum.INSTANCE_TIMEOUT;
            case TERMINATED:
                return FlowEventTypeEnum.INSTANCE_TERMINATED;
            case DESTROYED:
                return FlowEventTypeEnum.INSTANCE_DESTROYED;
            default:
                return FlowEventTypeEnum.INSTANCE_ENDED;
        }
    }

    private void notifyInstance(FlowEventTypeEnum eventType, Long instanceId, FlowNodeModel nodeModel,
                                FlowCreator creator) {
        FlowHisInstance his = instanceId == null ? null : hisInstanceDao.selectById(instanceId);
        notifyInstance(eventType, his, nodeModel, creator);
    }

    private void notifyInstance(FlowEventTypeEnum eventType, FlowHisInstance instance, FlowNodeModel nodeModel,
                                FlowCreator creator) {
        if (context.getInstanceListener() != null && eventType != null) {
            context.getInstanceListener().notify(eventType, instance, nodeModel,
                    creator == null ? context.getSystemCreator() : creator);
        }
    }

    private String modelCacheKey(Long instanceId) {
        return "nkk:flow:instance:model:" + instanceId;
    }

    private void finishParentCallProcess(FlowInstance childInstance, FlowHisInstance childHis,
                                         InstanceState state) {
        if (childInstance.getParentInstanceId() == null) {
            return;
        }
        if (context.getSubProcessHandler() != null) {
            context.getSubProcessHandler().onChildFinished(context, childInstance, childHis, state);
        }
        if (context.getTaskListener() != null) {
            context.getTaskListener().notify(FlowEventTypeEnum.SUB_PROCESS_ENDED, null, null, null, context.getSystemCreator());
        }
    }

    private FlowInstance getTopParentInstance(FlowInstance instance) {
        FlowInstance parent = instance;
        while (parent.getParentInstanceId() != null) {
            FlowInstance next = instanceDao.selectById(parent.getParentInstanceId());
            if (next == null) {
                break;
            }
            parent = next;
        }
        return parent;
    }

    private List<FlowInstance> collectInstances(FlowInstance instance) {
        List<FlowInstance> instances = new java.util.ArrayList<>();
        instances.add(instance);
        List<FlowInstance> children = instanceDao.selectListByParentInstanceId(instance.getId());
        if (children != null) {
            for (FlowInstance child : children) {
                instances.addAll(collectInstances(child));
            }
        }
        return instances;
    }

    private java.util.List<Long> collectInstanceIds(Long instanceId) {
        java.util.Set<Long> idSet = new java.util.LinkedHashSet<>();
        collectInstanceIds(instanceId, idSet);
        return new java.util.ArrayList<>(idSet);
    }

    private void collectInstanceIds(Long instanceId, java.util.Set<Long> ids) {
        if (!ids.add(instanceId)) {
            return;
        }
        List<FlowInstance> children = instanceDao.selectListByParentInstanceId(instanceId);
        if (children != null) {
            for (FlowInstance child : children) {
                collectInstanceIds(child.getId(), ids);
            }
        }
        List<FlowHisInstance> hisChildren = hisInstanceDao.selectListByParentInstanceId(instanceId);
        if (hisChildren != null) {
            for (FlowHisInstance child : hisChildren) {
                collectInstanceIds(child.getId(), ids);
            }
        }
    }
}

