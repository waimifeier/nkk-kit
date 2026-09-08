package org.nkk.flow.core.extension.subprocess;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.core.context.FlowExecution;
import org.nkk.flow.entity.FlowHisInstance;
import org.nkk.flow.entity.FlowInstance;
import org.nkk.flow.entity.FlowProcess;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.enums.runtime.FlowEventTypeEnum;
import org.nkk.flow.enums.core.FlowInstanceEnum.InstanceState;
import org.nkk.flow.enums.core.FlowTaskEnum.TaskState;
import org.nkk.flow.model.FlowNodeModel;
import org.nkk.flow.model.FlowProcessModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认子流程处理器。
 *
 * <p>异步子流程启动后父流程继续向下执行；同步子流程会在父流程生成一个等待任务，
 * 外围模块可在子流程结束事件里完成该等待任务，从而恢复父流程。</p>
 */
public class DefaultFlowSubProcessHandler implements FlowSubProcessHandler {

    @Override
    public boolean start(FlowContext context, FlowExecution execution, FlowNodeModel nodeModel) {
        if (StrUtil.isBlank(nodeModel.getCallProcess())) {
            throw new IllegalArgumentException("子流程定义 key 不能为空，nodeKey=" + nodeModel.getNodeKey());
        }
        FlowProcess process = resolveProcess(context, execution, nodeModel);
        FlowProcessModel model = process.model();
        FlowNodeModel startNode = model.getNodeConfig();
        Map<String, Object> variables = new HashMap<>(execution.getFlowInstance().variableToMap());
        if (execution.getArgs() != null) {
            variables.putAll(execution.getArgs());
        }

        FlowInstance childInstance = context.getRuntimeService().createInstance(
                process, execution.getFlowCreator(), variables, startNode, execution.getFlowInstance().getId());

        if (!Boolean.TRUE.equals(nodeModel.getCallAsync())) {
            context.createTask(execution, nodeModel);
            for (FlowTask task : execution.getFlowTasks()) {
                if (nodeModel.getNodeKey().equals(task.getTaskKey())) {
                    task.setCallProcessId(process.getId());
                    task.setCallInstanceId(childInstance.getId());
                    context.getTaskService().updateTask(task);
                }
            }
        }
        FlowExecution childExecution = new FlowExecution(context, model, execution.getFlowCreator(), childInstance, variables);
        boolean started = startNode.execute(context, childExecution);
        if (context.getTaskListener() != null) {
            context.getTaskListener().notify(FlowEventTypeEnum.SUB_PROCESS_STARTED,
                    execution.getFlowTask(), null, nodeModel, execution.getFlowCreator());
        }
        return started;
    }

    @Override
    public boolean onChildFinished(FlowContext context, FlowInstance childInstance, FlowHisInstance childHis,
                                   InstanceState state) {
        if (childInstance.getParentInstanceId() == null) {
            return true;
        }
        TaskState taskState = resolveParentTaskState(state);
        List<FlowTask> parentTasks = context.getTaskService().finishCallProcessTask(
                childInstance.getProcessId(), childInstance.getId(), FlowCreator.ADMIN, taskState);
        if (CollUtil.isEmpty(parentTasks)) {
            return true;
        }
        for (FlowTask parentTask : parentTasks) {
            FlowInstance parentInstance = context.getQueryService().getInstance(parentTask.getInstanceId());
            if (parentInstance == null) {
                continue;
            }
            if (isSuccess(state)) {
                Map<String, Object> args = new HashMap<>(parentInstance.variableToMap());
                args.putAll(parentTask.variableToMap());
                args.putAll(childInstance.variableToMap());
                context.getRuntimeService().addVariable(parentInstance.getId(), args, null);
                FlowProcessModel parentModel = context.getRuntimeService().getProcessModelByInstanceId(parentInstance.getId());
                FlowExecution parentExecution = new FlowExecution(context, parentModel, FlowCreator.ADMIN, parentInstance, args);
                parentExecution.setFlowTask(parentTask);
                parentExecution.executeNodeModel(parentTask.getTaskKey());
            } else {
                finishParent(context, parentTask, state);
            }
        }
        return true;
    }

    protected boolean isSuccess(InstanceState state) {
        return InstanceState.COMPLETED == state || InstanceState.AUTO_PASS == state;
    }

    protected TaskState resolveParentTaskState(InstanceState state) {
        if (InstanceState.REJECTED == state || InstanceState.AUTO_REJECT == state) {
            return TaskState.REJECTED;
        }
        if (InstanceState.REVOKED == state) {
            return TaskState.REVOKED;
        }
        if (InstanceState.TIMEOUT == state) {
            return TaskState.TIMEOUT;
        }
        if (InstanceState.TERMINATED == state) {
            return TaskState.TERMINATED;
        }
        if (InstanceState.DESTROYED == state) {
            return TaskState.DESTROYED;
        }
        return TaskState.AUTO_COMPLETED;
    }

    protected void finishParent(FlowContext context, FlowTask parentTask, InstanceState state) {
        if (InstanceState.REJECTED == state || InstanceState.AUTO_REJECT == state) {
            context.getRuntimeService().reject(parentTask.getInstanceId(), parentTask, FlowCreator.ADMIN);
            return;
        }
        if (InstanceState.REVOKED == state) {
            context.getRuntimeService().revoke(parentTask.getInstanceId(), parentTask, FlowCreator.ADMIN);
            return;
        }
        if (InstanceState.TIMEOUT == state) {
            context.getRuntimeService().timeout(parentTask.getInstanceId(), parentTask, FlowCreator.ADMIN);
            return;
        }
        if (InstanceState.DESTROYED == state) {
            context.getRuntimeService().destroyByInstanceId(parentTask.getInstanceId(), parentTask.variableToMap());
            return;
        }
        context.getRuntimeService().terminate(parentTask.getInstanceId(), FlowCreator.ADMIN);
    }
}

