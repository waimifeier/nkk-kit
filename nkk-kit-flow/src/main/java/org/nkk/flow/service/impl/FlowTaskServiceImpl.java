package org.nkk.flow.service.impl;

import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.core.context.FlowExecution;
import org.nkk.flow.core.extension.id.FlowIdGenerator;
import org.nkk.flow.dao.FlowHisTaskActorDao;
import org.nkk.flow.dao.FlowHisTaskDao;
import org.nkk.flow.dao.FlowTaskActorDao;
import org.nkk.flow.dao.FlowTaskDao;
import org.nkk.flow.entity.FlowHisTask;
import org.nkk.flow.entity.FlowHisTaskActor;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.entity.FlowTaskActor;
import org.nkk.flow.enums.core.FlowTaskActorEnum.AgentType;
import org.nkk.flow.enums.core.FlowTaskActorEnum.ActorType;
import org.nkk.flow.enums.runtime.FlowEventTypeEnum;
import org.nkk.flow.enums.runtime.FlowExecuteTypeEnum;
import org.nkk.flow.enums.node.FlowNodeTypeEnum;
import org.nkk.flow.enums.core.FlowTaskEnum.PerformType;
import org.nkk.flow.enums.core.FlowTaskEnum.TaskState;
import org.nkk.flow.enums.core.FlowTaskEnum.TaskType;
import org.nkk.flow.model.node.task.FlowNodeAssignee;
import org.nkk.flow.model.node.FlowNodeModel;
import org.nkk.flow.model.node.task.TaskNodeModel;
import org.nkk.flow.service.FlowTaskService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 默认任务服务。
 */
public class FlowTaskServiceImpl implements FlowTaskService {

    private final FlowContext context;
    private final FlowIdGenerator idGenerator;
    private final FlowTaskDao taskDao;
    private final FlowTaskActorDao taskActorDao;
    private final FlowHisTaskDao hisTaskDao;
    private final FlowHisTaskActorDao hisTaskActorDao;

    public FlowTaskServiceImpl(FlowContext context, FlowIdGenerator idGenerator, FlowTaskDao taskDao,
                               FlowTaskActorDao taskActorDao, FlowHisTaskDao hisTaskDao,
                               FlowHisTaskActorDao hisTaskActorDao) {
        this.context = context;
        this.idGenerator = idGenerator;
        this.taskDao = taskDao;
        this.taskActorDao = taskActorDao;
        this.hisTaskDao = hisTaskDao;
        this.hisTaskActorDao = hisTaskActorDao;
    }

    @Override
    public List<FlowTask> createTask(TaskNodeModel nodeModel, FlowExecution execution) {
        FlowTask task = createTaskBase(nodeModel, execution);
        PerformType performType = resolvePerformType(nodeModel);

        if (PerformType.START == performType) {
            taskDao.insert(task);
            completeTaskAndNotify(task, TaskState.COMPLETED, execution.getFlowCreator(), null);
            execution.setFlowTask(task);
            return Collections.singletonList(task);
        }

        if (PerformType.TIMER == performType || PerformType.TRIGGER == performType) {
            task.setPerformType(performType.value());
            taskDao.insert(task);
            context.getRuntimeService().updateCurrentNode(task);
            notifyTaskCreated(task, Collections.emptyList(), nodeModel, execution.getFlowCreator());
            return Collections.singletonList(task);
        }

        if (PerformType.CALL_PROCESS == performType) {
            task.setPerformType(performType.value());
            taskDao.insert(task);
            List<FlowTaskActor> actors = execution.getProviderTaskActors(nodeModel);
            if (actors == null || actors.isEmpty()) {
                actors = Collections.singletonList(toUserActor(execution.getFlowCreator()));
            }
            for (FlowTaskActor actor : actors) {
                assignTask(task, actor);
            }
            context.getRuntimeService().updateCurrentNode(task);
            notifyTaskCreated(task, actors, nodeModel, execution.getFlowCreator());
            return Collections.singletonList(task);
        }

        List<FlowTaskActor> actors = execution.getProviderTaskActors(nodeModel);
        if ((actors == null || actors.isEmpty()) && context.getTaskActorProvider().abnormal(task, actors, execution, nodeModel)) {
            return Collections.emptyList();
        }

        List<FlowTask> tasks = new ArrayList<>();
        if (PerformType.OR_SIGN == performType || PerformType.SEQUENTIAL == performType) {
            task.setPerformType(performType.value());
            taskDao.insert(task);
            tasks.add(task);
            List<FlowTaskActor> assignActors = PerformType.SEQUENTIAL == performType
                    ? Collections.singletonList(actors.get(0))
                    : actors;
            for (FlowTaskActor actor : assignActors) {
                assignTask(task, actor);
            }
            context.getRuntimeService().updateCurrentNode(task);
            notifyTaskCreated(task, assignActors, nodeModel, execution.getFlowCreator());
            return tasks;
        }

        for (FlowTaskActor actor : actors) {
            FlowTask branchTask = cloneTask(task);
            branchTask.setPerformType(performType.value());
            taskDao.insert(branchTask);
            assignTask(branchTask, actor);
            tasks.add(branchTask);
            context.getRuntimeService().updateCurrentNode(branchTask);
            notifyTaskCreated(branchTask, Collections.singletonList(actor), nodeModel, execution.getFlowCreator());
        }
        return tasks;
    }

    @Override
    public FlowTask createSequentialTask(TaskNodeModel nodeModel, FlowExecution execution, FlowTaskActor actor) {
        FlowTask task = createTaskBase(nodeModel, execution);
        task.setPerformType(PerformType.SEQUENTIAL.value());
        taskDao.insert(task);
        assignTask(task, actor);
        context.getRuntimeService().updateCurrentNode(task);
        notifyTaskCreated(task, Collections.singletonList(actor), nodeModel, execution.getFlowCreator());
        execution.addTasks(Collections.singletonList(task));
        return task;
    }

    @Override
    public FlowTask complete(Long taskId, FlowCreator creator, Map<String, Object> args) {
        return complete(taskId, creator, args, null);
    }

    @Override
    public FlowTask complete(Long taskId, FlowCreator creator, Map<String, Object> args, String opinion) {
        return executeTask(taskId, creator, args, TaskState.COMPLETED, opinion);
    }

    @Override
    public FlowTask executeTask(Long taskId, FlowCreator creator, Map<String, Object> args, TaskState state) {
        return executeTask(taskId, creator, args, state, null);
    }

    @Override
    public FlowTask executeTask(Long taskId, FlowCreator creator, Map<String, Object> args, TaskState state,
                                String opinion) {
        FlowTask task = taskDao.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在，taskId=" + taskId);
        }
        FlowCreator actualCreator = creator == null ? context.getSystemCreator() : creator;
        if (state == TaskState.COMPLETED && isAllowed(task, actualCreator.getCreateId()) == null) {
            throw new IllegalStateException("当前用户无权审批任务，taskId=" + taskId);
        }
        task.putAllVariable(args);
        task.setOpinion(opinion);
        completeTaskAndNotify(task, state, actualCreator, null);
        return task;
    }

    @Override
    public boolean updateHisTaskState(Long hisTaskId, TaskState state, Map<String, Object> args) {
        FlowHisTask hisTask = hisTaskDao.selectById(hisTaskId);
        if (hisTask == null || state == null) {
            return false;
        }
        hisTask.setTaskState(state.value());
        hisTask.putAllVariable(args);
        return hisTaskDao.updateById(hisTask);
    }

    @Override
    public void updateTask(FlowTask task) {
        taskDao.updateById(task);
    }

    @Override
    public Optional<List<FlowTask>> rejectTask(FlowTask task, FlowCreator creator, Map<String, Object> args) {
        return rejectTask(task, creator, args, null);
    }

    @Override
    public Optional<List<FlowTask>> rejectTask(FlowTask task, FlowCreator creator, Map<String, Object> args,
                                               String opinion) {
        FlowTask completed = executeTask(task.getId(), creator, args, TaskState.REJECTED, opinion);
        return Optional.of(Collections.singletonList(completed));
    }

    @Override
    public void completeActiveTasksForJump(Long instanceId, FlowTask currentTask, FlowCreator creator, Map<String, Object> args, TaskState state) {
        completeActiveTasksForJump(instanceId, currentTask, creator, args, state, null);
    }

    @Override
    public void completeActiveTasksForJump(Long instanceId, FlowTask currentTask, FlowCreator creator,
                                           Map<String, Object> args, TaskState state, String opinion) {
        List<FlowTask> tasks = taskDao.selectListByInstanceId(instanceId);
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        for (FlowTask task : tasks) {
            if (currentTask != null && task.getId().equals(currentTask.getId())) {
                task.putAllVariable(args);
                task.setOpinion(opinion);
            }
            completeTaskAndNotify(task, state, creator == null ? context.getSystemCreator() : creator, null);
        }
    }

    @Override
    public boolean completeActiveTasksByInstanceId(Long instanceId, FlowCreator creator, TaskState state) {
        List<FlowTask> tasks = taskDao.selectListByInstanceId(instanceId);
        if (tasks == null || tasks.isEmpty()) {
            return true;
        }
        for (FlowTask task : tasks) {
            completeTaskAndNotify(task, state, creator == null ? context.getSystemCreator() : creator, null);
        }
        return true;
    }

    @Override
    public boolean completeActiveTasksByInstanceIdAndTaskKey(Long instanceId, String taskKey, FlowCreator creator,
                                                             TaskState state) {
        List<FlowTask> tasks = taskDao.selectListByInstanceIdAndTaskKey(instanceId, taskKey);
        if (tasks == null || tasks.isEmpty()) {
            return true;
        }
        for (FlowTask task : tasks) {
            completeTaskAndNotify(task, state, creator == null ? context.getSystemCreator() : creator, null);
        }
        return true;
    }

    @Override
    public boolean transferTask(Long taskId, FlowCreator creator, FlowCreator assignee, Map<String, Object> args,
                                String opinion) {
        FlowTask task = getAllowedTask(taskId, creator);
        task.setTaskType(TaskType.TRANSFER.value());
        task.setAssignorId(creator.getCreateId());
        task.setAssignor(creator.getCreateBy());
        task.putAllVariable(args);
        task.setOpinion(opinion);
        replaceTaskActors(task, Collections.singletonList(toUserActor(assignee)));
        taskDao.updateById(task);
        notifyTaskChanged(FlowEventTypeEnum.TASK_TRANSFERRED, task, creator);
        return true;
    }

    @Override
    public boolean delegateTask(Long taskId, FlowCreator creator, FlowCreator assignee, Map<String, Object> args,
                                String opinion) {
        FlowTask task = getAllowedTask(taskId, creator);
        task.setTaskType(TaskType.DELEGATE.value());
        task.setAssignorId(creator.getCreateId());
        task.setAssignor(creator.getCreateBy());
        task.putAllVariable(args);
        task.setOpinion(opinion);
        replaceTaskActors(task, Collections.singletonList(toUserActor(assignee)));
        taskDao.updateById(task);
        notifyTaskChanged(FlowEventTypeEnum.TASK_DELEGATED, task, creator);
        return true;
    }

    @Override
    public boolean resolveTask(Long taskId, FlowCreator creator) {
        FlowTask task = getAllowedTask(taskId, creator);
        if (!TaskType.DELEGATE.value().equals(task.getTaskType()) || task.getAssignorId() == null) {
            throw new IllegalStateException("当前任务不是可归还的委派任务，taskId=" + taskId);
        }
        FlowTaskActor owner = new FlowTaskActor();
        owner.setActorId(task.getAssignorId());
        owner.setActorName(task.getAssignor());
        owner.setActorType(ActorType.USER.value());
        task.setTaskType(TaskType.DELEGATE_RETURN.value());
        replaceTaskActors(task, Collections.singletonList(owner));
        taskDao.updateById(task);
        notifyTaskChanged(FlowEventTypeEnum.TASK_RESOLVED, task, creator);
        return true;
    }

    @Override
    public boolean agentTask(Long taskId, FlowCreator creator, List<FlowCreator> agents, Map<String, Object> args,
                             String opinion) {
        FlowTask task = getAllowedTask(taskId, creator);
        if (agents == null || agents.isEmpty()) {
            throw new IllegalArgumentException("代理人不能为空");
        }
        task.setTaskType(TaskType.AGENT.value());
        task.putAllVariable(args);
        task.setOpinion(opinion);
        List<FlowTaskActor> actors = taskActorDao.selectListByTaskId(taskId);
        if (actors == null) {
            actors = new ArrayList<>();
        }
        for (FlowCreator agent : agents) {
            FlowTaskActor actor = toUserActor(agent);
            actor.setAgentId(creator.getCreateId());
            actor.setAgentType(AgentType.AGENT.value());
            actors.add(actor);
        }
        replaceTaskActors(task, actors);
        taskDao.updateById(task);
        notifyTaskChanged(FlowEventTypeEnum.TASK_AGENTED, task, creator);
        return true;
    }

    @Override
    public boolean claimTask(Long taskId, FlowCreator creator) {
        FlowTask task = taskDao.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在，taskId=" + taskId);
        }
        FlowTaskActor actor = toUserActor(creator);
        replaceTaskActors(task, Collections.singletonList(actor));
        taskDao.updateById(task);
        notifyTaskChanged(FlowEventTypeEnum.TASK_CLAIMED, task, creator);
        return true;
    }

    @Override
    public FlowTask claimRole(Long taskId, FlowCreator creator) {
        return claimGroupTask(taskId, creator, AgentType.CLAIM_ROLE);
    }

    @Override
    public FlowTask claimDepartment(Long taskId, FlowCreator creator) {
        return claimGroupTask(taskId, creator, AgentType.CLAIM_DEPARTMENT);
    }

    @Override
    public boolean viewTask(Long taskId, FlowCreator creator) {
        FlowTask task = getAllowedTask(taskId, creator);
        task.setViewed(1);
        taskDao.updateById(task);
        notifyTaskChanged(FlowEventTypeEnum.TASK_VIEWED, task, creator);
        return true;
    }

    @Override
    public boolean copyTask(Long taskId, List<FlowTaskActor> actors, FlowCreator creator, Map<String, Object> args,
                            String opinion) {
        FlowTask task = getAllowedTask(taskId, creator);
        if (actors == null || actors.isEmpty()) {
            throw new IllegalArgumentException("抄送人员不能为空");
        }
        FlowTask copyTask = cloneTask(task);
        copyTask.setId(idGenerator.nextId(null));
        copyTask.setCreateId(creator.getCreateId());
        copyTask.setCreateBy(creator.getCreateBy());
        copyTask.setCreateTime(new Date());
        copyTask.setTaskType(TaskType.COPY.value());
        copyTask.setPerformType(PerformType.COPY.value());
        copyTask.setOpinion(opinion);
        copyTask.putAllVariable(args);

        FlowHisTask hisTask = FlowHisTask.of(copyTask, TaskState.COMPLETED);
        hisTaskDao.insert(hisTask);

        List<FlowTaskActor> hisActors = new ArrayList<>();
        for (FlowTaskActor actor : actors) {
            FlowHisTaskActor hisActor = FlowHisTaskActor.of(actor);
            hisActor.setId(idGenerator.nextId(actor.getId()));
            hisActor.setTenantId(copyTask.getTenantId());
            hisActor.setInstanceId(copyTask.getInstanceId());
            hisActor.setTaskId(hisTask.getId());
            if (hisActor.getActorType() == null) {
                hisActor.setActorType(ActorType.USER.value());
            }
            hisTaskActorDao.insert(hisActor);
            hisActors.add(hisActor);
        }
        notifyTask(FlowEventTypeEnum.TASK_COPIED, hisTask, hisActors, null, creator);
        return true;
    }

    @Override
    public boolean addTaskActor(Long taskId, PerformType performType, List<FlowTaskActor> actors, FlowCreator creator) {
        FlowTask task = getAllowedTask(taskId, creator);
        if (actors == null || actors.isEmpty()) {
            throw new IllegalArgumentException("加签参与者不能为空");
        }
        PerformType targetPerformType = performType == null ? PerformType.of(task.getPerformType()) : performType;
        if (PerformType.COUNTERSIGN == targetPerformType || PerformType.VOTE_SIGN == targetPerformType) {
            for (FlowTaskActor actor : actors) {
                FlowTask newTask = cloneTask(task);
                newTask.setPerformType(targetPerformType.value());
                newTask.setCreateId(creator.getCreateId());
                newTask.setCreateBy(creator.getCreateBy());
                newTask.setCreateTime(new Date());
                taskDao.insert(newTask);
                assignTask(newTask, actor);
                notifyTaskCreated(newTask, Collections.singletonList(actor), null, creator);
            }
        } else {
            for (FlowTaskActor actor : actors) {
                assignTask(task, actor);
            }
        }
        if (!targetPerformType.value().equals(task.getPerformType())) {
            task.setPerformType(targetPerformType.value());
            taskDao.updateById(task);
        }
        notifyTaskChanged(FlowEventTypeEnum.TASK_ACTOR_ADDED, task, creator);
        return true;
    }

    @Override
    public boolean removeTaskActor(Long taskId, List<String> actorIds, FlowCreator creator) {
        FlowTask task = getAllowedTask(taskId, creator);
        if (actorIds == null || actorIds.isEmpty()) {
            return false;
        }
        List<FlowTaskActor> actors = taskActorDao.selectListByTaskId(taskId);
        if (actors == null || actors.isEmpty()) {
            return false;
        }
        int removeCount = 0;
        for (FlowTaskActor actor : actors) {
            if (actorIds.contains(actor.getActorId())) {
                removeCount++;
            }
        }
        if (removeCount == 0) {
            return false;
        }
        if (removeCount >= actors.size()) {
            throw new IllegalStateException("不允许移除任务全部参与者，taskId=" + taskId);
        }
        for (FlowTaskActor actor : actors) {
            if (actorIds.contains(actor.getActorId())) {
                taskActorDao.deleteById(actor.getId());
            }
        }
        notifyTaskChanged(FlowEventTypeEnum.TASK_ACTOR_REMOVED, task, creator);
        return true;
    }

    @Override
    public boolean changeTaskActor(Long taskId, FlowTaskActor actor) {
        FlowTask task = taskDao.selectById(taskId);
        if (task == null || actor == null) {
            return false;
        }
        replaceTaskActors(task, Collections.singletonList(actor));
        notifyTaskChanged(FlowEventTypeEnum.TASK_ACTOR_CHANGED, task, context.getSystemCreator());
        return true;
    }

    @Override
    public List<FlowTask> endCallProcessTask(Long callProcessId, Long callInstanceId) {
        return finishCallProcessTask(callProcessId, callInstanceId, context.getSystemCreator(), TaskState.AUTO_COMPLETED);
    }

    @Override
    public List<FlowTask> finishCallProcessTask(Long callProcessId, Long callInstanceId, FlowCreator creator,
                                                TaskState state) {
        List<FlowTask> tasks = taskDao.selectListByCallProcessIdAndCallInstanceId(callProcessId, callInstanceId);
        if (tasks == null || tasks.isEmpty()) {
            return Collections.emptyList();
        }
        List<FlowTask> completed = new ArrayList<>();
        for (FlowTask task : tasks) {
            completed.add(executeTask(task.getId(), creator == null ? context.getSystemCreator() : creator, null,
                    state == null ? TaskState.AUTO_COMPLETED : state));
        }
        return completed;
    }

    @Override
    public boolean cascadeRemoveByInstanceIds(List<Long> instanceIds) {
        if (instanceIds == null || instanceIds.isEmpty()) {
            return true;
        }
        taskActorDao.deleteByInstanceIds(instanceIds);
        hisTaskActorDao.deleteByInstanceIds(instanceIds);
        taskDao.deleteByInstanceIds(instanceIds);
        hisTaskDao.deleteByInstanceIds(instanceIds);
        return true;
    }

    @Override
    public FlowTaskActor isAllowed(FlowTask task, String userId) {
        return context.getTaskAccessStrategy().isAllowed(userId, taskActorDao.selectListByTaskId(task.getId()));
    }

    private FlowTask getAllowedTask(Long taskId, FlowCreator creator) {
        FlowTask task = taskDao.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在，taskId=" + taskId);
        }
        if (isAllowed(task, creator.getCreateId()) == null) {
            throw new IllegalStateException("当前用户无权操作任务，taskId=" + taskId);
        }
        return task;
    }

    protected FlowTask createTaskBase(TaskNodeModel nodeModel, FlowExecution execution) {
        FlowTask task = new FlowTask();
        task.setId(idGenerator.nextId(null));
        task.setTenantId(execution.getFlowCreator().getTenantId());
        task.setCreateId(execution.getFlowCreator().getCreateId());
        task.setCreateBy(execution.getFlowCreator().getCreateBy());
        task.setCreateTime(context.getCreateTimeHandler().getCurrentTime(
                FlowExecuteTypeEnum.CREATE_TASK, execution.getFlowInstance().getId(), null));
        task.setInstanceId(execution.getFlowInstance().getId());
        task.setParentTaskId(execution.getFlowTask() == null ? 0L : execution.getFlowTask().getId());
        task.setTaskName(nodeModel.getNodeName());
        task.setTaskKey(nodeModel.getNodeKey());
        Integer taskType = execution.consumeNextTaskType();
        task.setTaskType(taskType == null ? nodeModel.getType() : taskType);
        task.setPerformType(resolvePerformType(nodeModel).value());
        task.setActionUrl(nodeModel.getActionUrl());
        task.setVariable(FlowContext.toJson(execution.getArgs()));
        loadScheduleConfig(task, nodeModel);
        return context.getCreateTaskHandler().handle(task, nodeModel, execution);
    }

    protected List<FlowTaskActor> moveToHisTask(FlowTask task, TaskState state, FlowCreator creator) {
        List<FlowTaskActor> actors = taskActorDao.selectListByTaskId(task.getId());
        FlowHisTask hisTask = FlowHisTask.of(task, state);
        hisTask.setCreateId(creator.getCreateId());
        hisTask.setCreateBy(creator.getCreateBy());
        hisTaskDao.insert(hisTask);
        if (actors != null) {
            for (FlowTaskActor actor : actors) {
                hisTaskActorDao.insert(FlowHisTaskActor.of(actor));
            }
        }
        taskActorDao.deleteByTaskId(task.getId());
        taskDao.deleteById(task.getId());
        return actors == null ? Collections.emptyList() : actors;
    }

    private void completeTaskAndNotify(FlowTask task, TaskState state, FlowCreator creator, FlowNodeModel nodeModel) {
        FlowCreator actualCreator = creator == null ? context.getSystemCreator() : creator;
        TaskState actualState = state == null ? TaskState.COMPLETED : state;
        List<FlowTaskActor> actors = moveToHisTask(task, actualState, actualCreator);
        notifyTask(eventTypeOf(actualState), task, actors, nodeModel, actualCreator);
    }

    private FlowEventTypeEnum eventTypeOf(TaskState state) {
        if (state == null) {
            return FlowEventTypeEnum.TASK_COMPLETED;
        }
        switch (state) {
            case COMPLETED:
                return FlowEventTypeEnum.TASK_COMPLETED;
            case REJECTED:
                return FlowEventTypeEnum.TASK_REJECTED;
            case REVOKED:
                return FlowEventTypeEnum.TASK_REVOKED;
            case TIMEOUT:
                return FlowEventTypeEnum.TASK_TIMEOUT;
            case TERMINATED:
                return FlowEventTypeEnum.TASK_TERMINATED;
            case REJECT_END:
                return FlowEventTypeEnum.TASK_REJECT_ENDED;
            case AUTO_COMPLETED:
                return FlowEventTypeEnum.TASK_AUTO_COMPLETED;
            case AUTO_REJECTED:
                return FlowEventTypeEnum.TASK_AUTO_REJECTED;
            case JUMP:
            case AUTO_JUMP:
                return FlowEventTypeEnum.TASK_JUMPED;
            case REJECT_JUMP:
                return FlowEventTypeEnum.TASK_REJECT_JUMPED;
            case RE_APPROVE_JUMP:
                return FlowEventTypeEnum.TASK_RE_APPROVE_JUMPED;
            case ROUTE_JUMP:
                return FlowEventTypeEnum.TASK_ROUTE_JUMPED;
            case TRIGGER_JUMP:
                return FlowEventTypeEnum.TASK_TRIGGER_JUMPED;
            case RECLAIMED:
                return FlowEventTypeEnum.TASK_RECLAIMED;
            case WITHDRAWN:
                return FlowEventTypeEnum.TASK_WITHDRAWN;
            case RESUMED:
                return FlowEventTypeEnum.TASK_RESUMED;
            case DESTROYED:
                return FlowEventTypeEnum.TASK_DESTROYED;
            case ABSTAINED:
                return FlowEventTypeEnum.TASK_ABSTAINED;
            default:
                return FlowEventTypeEnum.TASK_COMPLETED;
        }
    }

    private PerformType resolvePerformType(TaskNodeModel nodeModel) {
        if (FlowNodeTypeEnum.START.eq(nodeModel.getType())) {
            return PerformType.START;
        }
        if (FlowNodeTypeEnum.COPY.eq(nodeModel.getType())) {
            return PerformType.COPY;
        }
        if (FlowNodeTypeEnum.TIMER.eq(nodeModel.getType())) {
            return PerformType.TIMER;
        }
        if (FlowNodeTypeEnum.TRIGGER.eq(nodeModel.getType())) {
            return PerformType.TRIGGER;
        }
        if (FlowNodeTypeEnum.CALL_PROCESS.eq(nodeModel.getType())) {
            return PerformType.CALL_PROCESS;
        }
        return PerformType.of(nodeModel.getExamineMode());
    }

    private void loadScheduleConfig(FlowTask task, TaskNodeModel nodeModel) {
        task.setTermMode(nodeModel.getTermMode());
        if (Boolean.TRUE.equals(nodeModel.getTermAuto()) && nodeModel.getTerm() != null && nodeModel.getTerm() > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR, nodeModel.getTerm());
            task.setExpireTime(calendar.getTime());
        }
        if (Boolean.TRUE.equals(nodeModel.getRemind()) && nodeModel.getExtendConfig() != null) {
            Object remindMinutes = nodeModel.getExtendConfig().get("remindMinutes");
            if (remindMinutes instanceof Number) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MINUTE, ((Number) remindMinutes).intValue());
                task.setRemindTime(calendar.getTime());
            }
        }
        if ((FlowNodeTypeEnum.TIMER.eq(nodeModel.getType()) || FlowNodeTypeEnum.TRIGGER.eq(nodeModel.getType()))
                && nodeModel.getExtendConfig() != null) {
            Object delayMinutes = nodeModel.getExtendConfig().get("delayMinutes");
            if (delayMinutes instanceof Number) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MINUTE, ((Number) delayMinutes).intValue());
                task.setExpireTime(calendar.getTime());
            }
        }
    }

    private void assignTask(FlowTask task, FlowTaskActor actor) {
        actor.setId(idGenerator.nextId(actor.getId()));
        actor.setTenantId(task.getTenantId());
        actor.setInstanceId(task.getInstanceId());
        actor.setTaskId(task.getId());
        taskActorDao.insert(actor);
    }

    private void replaceTaskActors(FlowTask task, List<FlowTaskActor> actors) {
        taskActorDao.deleteByTaskId(task.getId());
        for (FlowTaskActor actor : actors) {
            assignTask(task, actor);
        }
    }

    private FlowTaskActor toUserActor(FlowCreator creator) {
        FlowTaskActor actor = new FlowTaskActor();
        actor.setActorId(creator.getCreateId());
        actor.setActorName(creator.getCreateBy());
        actor.setActorType(ActorType.USER.value());
        return actor;
    }

    private FlowTask claimGroupTask(Long taskId, FlowCreator creator, AgentType agentType) {
        FlowTask task = taskDao.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在，taskId=" + taskId);
        }
        FlowTaskActor actor = toUserActor(creator);
        actor.setAgentType(agentType.value());
        actor.setActorType(ActorType.USER.value());
        replaceTaskActors(task, Collections.singletonList(actor));
        taskDao.updateById(task);
        notifyTaskChanged(FlowEventTypeEnum.TASK_CLAIMED, task, creator);
        return task;
    }

    private FlowTask cloneTask(FlowTask source) {
        FlowTask task = new FlowTask();
        task.setId(idGenerator.nextId(null));
        task.setTenantId(source.getTenantId());
        task.setCreateId(source.getCreateId());
        task.setCreateBy(source.getCreateBy());
        task.setCreateTime(source.getCreateTime());
        task.setInstanceId(source.getInstanceId());
        task.setParentTaskId(source.getParentTaskId());
        task.setCallProcessId(source.getCallProcessId());
        task.setCallInstanceId(source.getCallInstanceId());
        task.setTaskName(source.getTaskName());
        task.setTaskKey(source.getTaskKey());
        task.setTaskType(source.getTaskType());
        task.setPerformType(source.getPerformType());
        task.setActionUrl(source.getActionUrl());
        task.setVariable(source.getVariable());
        return task;
    }

    private void notifyTaskCreated(FlowTask task, List<FlowTaskActor> actors, FlowNodeModel nodeModel, FlowCreator creator) {
        notifyTask(FlowEventTypeEnum.TASK_CREATED, task, actors, nodeModel, creator);
    }

    private void notifyTaskChanged(FlowEventTypeEnum eventType, FlowTask task, FlowCreator creator) {
        List<FlowTaskActor> actors = task == null || task.getId() == null
                ? Collections.emptyList()
                : taskActorDao.selectListByTaskId(task.getId());
        notifyTask(eventType, task, actors, null, creator);
    }

    private void notifyTask(FlowEventTypeEnum eventType, FlowTask task, List<FlowTaskActor> actors,
                            FlowNodeModel nodeModel, FlowCreator creator) {
        if (context.getTaskListener() != null && eventType != null) {
            context.getTaskListener().notify(eventType, task,
                    actors == null ? Collections.emptyList() : actors, nodeModel, creator);
        }
    }

    public FlowNodeAssignee nextSequentialAssignee(TaskNodeModel nodeModel, FlowExecution execution, String currentActorId) {
        List<FlowTaskActor> actors = execution.getProviderTaskActors(nodeModel);
        if (actors == null || actors.isEmpty()) {
            return null;
        }
        List<String> actorIds = actors.stream().map(FlowTaskActor::getActorId).collect(Collectors.toList());
        int index = actorIds.indexOf(currentActorId);
        if (index < 0 || index + 1 >= actors.size()) {
            return null;
        }
        return FlowNodeAssignee.of(actors.get(index + 1));
    }
}

