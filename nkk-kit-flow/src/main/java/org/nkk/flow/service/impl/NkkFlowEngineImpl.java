package org.nkk.flow.service.impl;

import cn.hutool.core.util.StrUtil;
import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.core.context.FlowExecution;
import org.nkk.flow.entity.FlowInstance;
import org.nkk.flow.entity.FlowProcess;
import org.nkk.flow.entity.FlowHisTask;
import org.nkk.flow.entity.FlowHisTaskActor;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.entity.FlowTaskActor;
import org.nkk.flow.enums.runtime.FlowEventTypeEnum;
import org.nkk.flow.enums.node.FlowNodeTypeEnum;
import org.nkk.flow.enums.core.FlowTaskEnum.PerformType;
import org.nkk.flow.enums.node.FlowRejectStrategyEnum;
import org.nkk.flow.enums.core.FlowTaskEnum.TaskState;
import org.nkk.flow.enums.core.FlowTaskEnum.TaskType;
import org.nkk.flow.model.FlowNodeAssignee;
import org.nkk.flow.model.FlowNodeModel;
import org.nkk.flow.model.FlowProcessModel;
import org.nkk.flow.model.FlowSignPolicy;
import org.nkk.flow.service.NkkFlowEngine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 默认 NKK 审批流引擎。
 */
public class NkkFlowEngineImpl implements NkkFlowEngine {

    private FlowContext context;

    @Override
    public NkkFlowEngine configure(FlowContext context) {
        this.context = context;
        return this;
    }

    @Override
    public FlowContext getContext() {
        return context;
    }

    private FlowCreator resolveCreator(FlowCreator creator) {
        return context.resolveCreator(creator);
    }

    @Override
    public Optional<FlowInstance> startInstanceByProcessKey(String processKey, Integer version, FlowCreator creator, Map<String, Object> args) {
        return startInstanceByProcessKey(processKey, version, creator, args, false, null, null);
    }

    @Override
    public Optional<FlowInstance> startInstanceByProcessKey(String processKey, Integer version, FlowCreator creator,
                                                             Map<String, Object> args, boolean saveAsDraft,
                                                            Consumer<FlowNodeModel> checkNodeModel,
                                                            Supplier<FlowInstance> supplier) {
        FlowCreator actualCreator = resolveCreator(creator);
        FlowProcess process = processService().getProcessByVersion(actualCreator.getTenantId(), processKey, version).checkState();
        return startProcessInstance(process, actualCreator, args, saveAsDraft, checkNodeModel, supplier);
    }

    @Override
    public Optional<FlowInstance> startInstanceById(Long processId, FlowCreator creator, Map<String, Object> args) {
        return startInstanceById(processId, creator, args, false, null, null);
    }

    @Override
    public Optional<FlowInstance> startInstanceById(Long processId, FlowCreator creator, Map<String, Object> args,
                                                    boolean saveAsDraft, Consumer<FlowNodeModel> checkNodeModel,
                                                    Supplier<FlowInstance> supplier) {
        FlowCreator actualCreator = resolveCreator(creator);
        FlowProcess process = processService().getProcessById(processId).checkState();
        return startProcessInstance(process, actualCreator, args, saveAsDraft, checkNodeModel, supplier);
    }

    private Optional<FlowInstance> startProcessInstance(FlowProcess process, FlowCreator creator, Map<String, Object> args,
                                                        boolean saveAsDraft, Consumer<FlowNodeModel> checkNodeModel,
                                                        Supplier<FlowInstance> supplier) {
        FlowProcessModel model = process.model();
        FlowNodeModel startNode = model.getNodeConfig();
        if (context.getStartAccessStrategy() != null
                && !context.getStartAccessStrategy().isAllowed(creator, startNode.getNodeAssigneeList())) {
            throw new IllegalStateException("当前用户无权发起该流程，processKey=" + process.getProcessKey());
        }
        if (checkNodeModel != null) {
            checkNodeModel.accept(startNode);
        }
        FlowInstance instance = runtimeService().createInstance(process, creator, args, startNode, null, saveAsDraft, supplier);
        if (saveAsDraft) {
            return Optional.of(instance);
        }
        FlowExecution execution = new FlowExecution(context, model, creator, instance, args);
        startNode.execute(context, execution);
        return Optional.of(instance);
    }

    @Override
    public boolean executeTask(Long taskId, FlowCreator creator, Map<String, Object> args) {
        return executeTask(taskId, creator, args, null);
    }

    @Override
    public boolean executeTask(Long taskId, FlowCreator creator, Map<String, Object> args, String opinion) {
        FlowCreator actualCreator = resolveCreator(creator);
        FlowTask task = taskService().complete(taskId, actualCreator, args, opinion);
        return continueAfterTask(task, actualCreator, args);
    }

    @Override
    public boolean executeFinishTrigger(Long taskId, FlowCreator creator, Map<String, Object> args) {
        FlowCreator actualCreator = resolveCreator(creator);
        FlowTask task = taskService().executeTask(taskId, actualCreator, args, TaskState.AUTO_COMPLETED);
        return continueAfterTask(task, actualCreator, args);
    }

    @Override
    public boolean autoCompleteTask(Long taskId, Map<String, Object> args, FlowCreator creator) {
        FlowTask task = taskService().executeTask(taskId, creator == null ? FlowCreator.ADMIN : creator, args,
                TaskState.AUTO_COMPLETED, null);
        return continueAfterTask(task, creator == null ? FlowCreator.ADMIN : creator, args);
    }

    @Override
    public boolean autoRejectTask(FlowTask task, Map<String, Object> args, FlowCreator creator) {
        if (task == null) {
            return false;
        }
        return rejectTask(task.getId(), creator == null ? FlowCreator.ADMIN : creator, args, null).isPresent();
    }

    @Override
    public boolean voteRejectTask(Long taskId, FlowCreator creator, Map<String, Object> args) {
        return executeSignTask(taskId, resolveCreator(creator), args, TaskState.REJECTED);
    }

    @Override
    public boolean abstainTask(Long taskId, FlowCreator creator, Map<String, Object> args) {
        return executeSignTask(taskId, resolveCreator(creator), args, TaskState.ABSTAINED);
    }

    @Override
    public Optional<List<FlowTask>> executeJumpTask(Long taskId, String nodeKey, FlowCreator creator, Map<String, Object> args) {
        FlowCreator actualCreator = resolveCreator(creator);
        FlowTask task = queryService().getTask(taskId);
        if (task == null) {
            return Optional.empty();
        }
        FlowInstance instance = queryService().getInstance(task.getInstanceId());
        if (instance == null) {
            return Optional.empty();
        }
        FlowProcessModel model = runtimeService().getProcessModelByInstanceId(instance.getId());
        FlowNodeModel targetNode = nodeKey == null ? null : model.getNode(nodeKey);
        if (targetNode == null) {
            targetNode = model.getNode(task.getTaskKey());
        }
        if (targetNode == null) {
            return Optional.empty();
        }
        taskService().completeActiveTasksForJump(instance.getId(), task, actualCreator, args, TaskState.JUMP);
        Map<String, Object> variables = new HashMap<>(instance.variableToMap());
        if (args != null) {
            variables.putAll(args);
        }
        FlowExecution execution = new FlowExecution(context, model, actualCreator, instance, variables);
        execution.setFlowTask(task.putAllVariable(args));
        targetNode.execute(context, execution);
        return Optional.of(execution.getFlowTasks());
    }

    @Override
    public boolean redeployProcessModel(Long instanceId, Function<FlowProcessModel, FlowProcessModel> function) {
        FlowProcessModel model = runtimeService().getProcessModelByInstanceId(instanceId);
        FlowProcessModel target = function == null ? model : function.apply(model);
        return runtimeService().updateInstanceModelById(instanceId, target);
    }

    @Override
    public boolean executeAppendNodeModel(Long taskId, FlowNodeModel nodeModel, FlowCreator creator, Map<String, Object> args, boolean beforeAfter) {
        FlowCreator actualCreator = resolveCreator(creator);
        runtimeService().appendNodeModel(taskId, nodeModel, beforeAfter);
        if (beforeAfter && nodeModel != null) {
            return executeJumpTask(taskId, nodeModel.getNodeKey(), actualCreator, args).isPresent();
        }
        return true;
    }

    @Override
    public boolean executeRemoveNodeModel(Long instanceId, String nodeKey) {
        return runtimeService().removeNodeModel(instanceId, nodeKey, node -> {
            List<FlowTask> tasks = queryService().getTasksByInstanceIdAndTaskKey(instanceId, node.getNodeKey());
            return tasks == null || tasks.isEmpty();
        });
    }

    @Override
    public void processTimeoutOrRemind(Date now) {
        Date current = now == null ? new Date() : now;
        List<FlowTask> tasks = queryService().getTimeoutOrRemindTasks(current);
        for (FlowTask task : tasks) {
            if (task.getRemindTime() != null && !task.getRemindTime().after(current)) {
                if (context.getTaskReminder() != null) {
                    context.getTaskReminder().remind(task);
                }
                task.setRemindRepeat(task.getRemindRepeat() == null ? 1 : task.getRemindRepeat() + 1);
                task.setRemindTime(null);
                taskService().updateTask(task);
                if (context.getTaskListener() != null) {
                    context.getTaskListener().notify(FlowEventTypeEnum.TASK_REMINDED, task, null, null, FlowCreator.ADMIN);
                }
            }
            if (task.getExpireTime() != null && !task.getExpireTime().after(current)) {
                processExpiredTask(task);
            }
        }
    }

    private void processExpiredTask(FlowTask task) {
        FlowInstance instance = queryService().getInstance(task.getInstanceId());
        if (instance == null) {
            return;
        }
        FlowProcessModel model = runtimeService().getProcessModelByInstanceId(instance.getId());
        FlowNodeModel nodeModel = model.getNode(task.getTaskKey());
        FlowExecution execution = new FlowExecution(context, model, FlowCreator.ADMIN, instance, task.variableToMap());
        execution.setFlowTask(task);
        if (TaskType.TRIGGER.eq(task.getTaskType()) && context.getTaskTrigger() != null) {
            if (!context.getTaskTrigger().execute(nodeModel, execution)) {
                return;
            }
            if (context.getTaskListener() != null) {
                context.getTaskListener().notify(FlowEventTypeEnum.TASK_TRIGGERED, task, null, nodeModel, FlowCreator.ADMIN);
            }
        }
        TaskState state = Integer.valueOf(1).equals(task.getTermMode())
                ? TaskState.AUTO_REJECTED
                : TaskState.AUTO_COMPLETED;
        FlowTask completed = taskService().executeTask(task.getId(), FlowCreator.ADMIN, null, state);
        if (context.getTaskListener() != null) {
            context.getTaskListener().notify(FlowEventTypeEnum.TASK_TIMEOUT, completed, null, nodeModel, FlowCreator.ADMIN);
        }
        continueAfterTask(completed, FlowCreator.ADMIN, completed.variableToMap());
    }

    private boolean continueAfterTask(FlowTask task, FlowCreator creator, Map<String, Object> args) {
        FlowInstance instance = queryService().getInstance(task.getInstanceId());
        if (instance == null) {
            return true;
        }
        FlowProcessModel model = runtimeService().getProcessModelByInstanceId(instance.getId());
        FlowNodeModel nodeModel = model.getNode(task.getTaskKey());
        if (nodeModel == null) {
            throw new IllegalStateException("流程模型中不存在任务节点，taskKey=" + task.getTaskKey());
        }
        FlowExecution execution = new FlowExecution(context, model, creator, instance, task.variableToMap());
        execution.setFlowTask(task);
        PerformType performType = PerformType.of(task.getPerformType());
        if (performType == PerformType.SEQUENTIAL && createNextSequentialTask(nodeModel, execution, task)) {
            return true;
        }
        if (performType == PerformType.COUNTERSIGN) {
            return handleSignDecision(resolveCountersignResult(nodeModel, task), task, creator, execution);
        }
        if (performType == PerformType.OR_SIGN) {
            return handleSignDecision(resolveOrSignResult(nodeModel, task), task, creator, execution);
        }
        if (performType == PerformType.VOTE_SIGN) {
            VoteResult voteResult = resolveVoteResult(nodeModel, task);
            return handleSignDecision(voteResult, task, creator, execution);
        }
        return execution.executeNodeModel(task.getTaskKey());
    }

    private boolean executeSignTask(Long taskId, FlowCreator creator, Map<String, Object> args, TaskState state) {
        FlowCreator actualCreator = creator == null ? FlowCreator.ADMIN : creator;
        FlowTask task = queryService().getTask(taskId);
        if (task == null) {
            return false;
        }
        if (taskService().isAllowed(task, actualCreator.getCreateId()) == null) {
            throw new IllegalStateException("当前用户无权操作任务，taskId=" + taskId);
        }
        FlowInstance instance = queryService().getInstance(task.getInstanceId());
        if (instance == null) {
            return false;
        }
        FlowProcessModel model = runtimeService().getProcessModelByInstanceId(instance.getId());
        FlowNodeModel nodeModel = model.getNode(task.getTaskKey());
        PerformType performType = PerformType.of(task.getPerformType());
        if (performType != PerformType.COUNTERSIGN
                && performType != PerformType.OR_SIGN
                && performType != PerformType.VOTE_SIGN) {
            throw new IllegalStateException("当前任务不是多人审批任务，taskId=" + taskId);
        }
        if (state == TaskState.ABSTAINED && !FlowSignPolicy.of(nodeModel).isAllowAbstain()) {
            throw new IllegalStateException("当前节点未开启弃权，nodeKey=" + task.getTaskKey());
        }
        FlowTask completed = taskService().executeTask(taskId, actualCreator, args, state, null);
        return continueAfterTask(completed, actualCreator, args);
    }

    private boolean handleSignDecision(VoteResult voteResult, FlowTask task, FlowCreator creator,
                                       FlowExecution execution) {
        if (voteResult == VoteResult.WAIT) {
            return true;
        }
        if (voteResult == VoteResult.REJECT) {
            completeActiveSameNodeTasks(task, creator, TaskState.AUTO_REJECTED);
            runtimeService().reject(task.getInstanceId(), task, creator == null ? FlowCreator.ADMIN : creator);
            return true;
        }
        completeActiveSameNodeTasks(task, creator, TaskState.AUTO_COMPLETED);
        return execution.executeNodeModel(task.getTaskKey());
    }

    @Override
    public Optional<List<FlowTask>> rejectTask(Long taskId, FlowCreator creator, Map<String, Object> args) {
        return rejectTask(taskId, creator, args, null);
    }

    @Override
    public Optional<List<FlowTask>> rejectTask(Long taskId, FlowCreator creator, Map<String, Object> args, String opinion) {
        return rejectTask(taskId, null, creator, args, opinion);
    }

    @Override
    public Optional<List<FlowTask>> rejectTask(Long taskId, String nodeKey, FlowCreator creator, Map<String, Object> args) {
        return rejectTask(taskId, nodeKey, creator, args, null);
    }

    @Override
    public Optional<List<FlowTask>> rejectTask(Long taskId, String nodeKey, FlowCreator creator, Map<String, Object> args,
                                               String opinion) {
        FlowCreator actualCreator = resolveCreator(creator);
        FlowTask task = queryService().getTask(taskId);
        if (task == null) {
            return Optional.empty();
        }
        if (taskService().isAllowed(task, actualCreator.getCreateId()) == null) {
            throw new IllegalStateException("当前用户无权驳回任务，taskId=" + taskId);
        }

        FlowInstance instance = queryService().getInstance(task.getInstanceId());
        if (instance == null) {
            return Optional.empty();
        }
        FlowProcessModel model = runtimeService().getProcessModelByInstanceId(instance.getId());
        FlowNodeModel currentNode = model.getNode(task.getTaskKey());
        FlowNodeModel targetNode = resolveRejectTarget(model, currentNode, nodeKey, task.getInstanceId());
        if (targetNode == null) {
            task.setOpinion(opinion);
            task.putAllVariable(args);
            runtimeService().terminate(task.getInstanceId(), task, actualCreator);
            return Optional.empty();
        }

        taskService().completeActiveTasksForJump(task.getInstanceId(), task, actualCreator, args,
                TaskState.REJECTED, opinion);
        Map<String, Object> variables = new HashMap<>(instance.variableToMap());
        if (args != null) {
            variables.putAll(args);
        }
        FlowExecution execution = new FlowExecution(context, model, actualCreator, instance, variables);
        execution.setFlowTask(task.putAllVariable(args));
        targetNode.execute(context, execution);
        return Optional.of(execution.getFlowTasks());
    }

    @Override
    public Optional<List<FlowTask>> terminateByTask(Long taskId, FlowCreator creator, Map<String, Object> args) {
        FlowCreator actualCreator = resolveCreator(creator);
        FlowTask task = queryService().getTask(taskId);
        if (task == null) {
            return Optional.empty();
        }
        if (taskService().isAllowed(task, actualCreator.getCreateId()) == null) {
            throw new IllegalStateException("当前用户无权终止任务，taskId=" + taskId);
        }
        if (!runtimeService().terminate(task.getInstanceId(), actualCreator)) {
            throw new IllegalStateException("终止流程失败，instanceId=" + task.getInstanceId());
        }
        return Optional.empty();
    }

    @Override
    public boolean suspendInstanceById(Long instanceId, FlowCreator creator) {
        return runtimeService().suspendInstanceById(instanceId, resolveCreator(creator));
    }

    @Override
    public boolean activeInstanceById(Long instanceId, FlowCreator creator) {
        return runtimeService().activeInstanceById(instanceId, resolveCreator(creator));
    }

    @Override
    public boolean revokeInstanceById(Long instanceId, FlowCreator creator) {
        return runtimeService().revoke(instanceId, null, resolveCreator(creator));
    }

    @Override
    public boolean timeoutInstanceById(Long instanceId, FlowCreator creator) {
        return runtimeService().timeout(instanceId, null, resolveCreator(creator));
    }

    @Override
    public boolean destroyByInstanceId(Long instanceId, FlowCreator creator, Map<String, Object> args) {
        return runtimeService().destroyByInstanceId(instanceId, resolveCreator(creator), args);
    }

    @Override
    public boolean addVariable(Long instanceId, Map<String, Object> args) {
        return runtimeService().addVariable(instanceId, args, null);
    }

    @Override
    public void cascadeRemoveByInstanceId(Long instanceId, FlowCreator creator) {
        runtimeService().cascadeRemoveByInstanceId(instanceId, resolveCreator(creator));
    }

    @Override
    public void cascadeRemoveByProcessId(Long processId) {
        runtimeService().cascadeRemoveByProcessId(processId);
    }

    @Override
    public boolean transferTask(Long taskId, FlowCreator creator, FlowCreator assignee, Map<String, Object> args,
                                String opinion) {
        return taskService().transferTask(taskId, resolveCreator(creator), assignee, args, opinion);
    }

    @Override
    public boolean delegateTask(Long taskId, FlowCreator creator, FlowCreator assignee, Map<String, Object> args,
                                String opinion) {
        return taskService().delegateTask(taskId, resolveCreator(creator), assignee, args, opinion);
    }

    @Override
    public boolean resolveTask(Long taskId, FlowCreator creator) {
        return taskService().resolveTask(taskId, resolveCreator(creator));
    }

    @Override
    public boolean agentTask(Long taskId, FlowCreator creator, List<FlowCreator> agents, Map<String, Object> args,
                             String opinion) {
        return taskService().agentTask(taskId, resolveCreator(creator), agents, args, opinion);
    }

    @Override
    public boolean claimTask(Long taskId, FlowCreator creator) {
        return taskService().claimTask(taskId, resolveCreator(creator));
    }

    @Override
    public FlowTask claimRole(Long taskId, FlowCreator creator) {
        return taskService().claimRole(taskId, resolveCreator(creator));
    }

    @Override
    public FlowTask claimDepartment(Long taskId, FlowCreator creator) {
        return taskService().claimDepartment(taskId, resolveCreator(creator));
    }

    @Override
    public boolean viewTask(Long taskId, FlowCreator creator) {
        return taskService().viewTask(taskId, resolveCreator(creator));
    }

    @Override
    public boolean copyTask(Long taskId, List<FlowTaskActor> actors, FlowCreator creator, Map<String, Object> args,
                            String opinion) {
        return taskService().copyTask(taskId, actors, resolveCreator(creator), args, opinion);
    }

    @Override
    public boolean changeTaskActor(Long taskId, FlowTaskActor actor) {
        return taskService().changeTaskActor(taskId, actor);
    }

    @Override
    public boolean addTaskActor(Long taskId, PerformType performType, List<FlowTaskActor> actors, FlowCreator creator) {
        return taskService().addTaskActor(taskId, performType, actors, resolveCreator(creator));
    }

    @Override
    public boolean removeTaskActor(Long taskId, List<String> actorIds, FlowCreator creator) {
        return taskService().removeTaskActor(taskId, actorIds, resolveCreator(creator));
    }

    @Override
    public Optional<List<FlowTask>> withdrawTask(Long hisTaskId, FlowCreator creator, Map<String, Object> args) {
        return rollbackToHisTask(hisTaskId, resolveCreator(creator), args, TaskState.WITHDRAWN);
    }

    @Override
    public Optional<List<FlowTask>> reclaimTask(Long hisTaskId, FlowCreator creator, Map<String, Object> args) {
        return rollbackToHisTask(hisTaskId, resolveCreator(creator), args, TaskState.RECLAIMED);
    }

    @Override
    public Optional<List<FlowTask>> resumeInstance(Long instanceId, String nodeKey, FlowCreator creator, Map<String, Object> args) {
        FlowCreator actualCreator = resolveCreator(creator);
        FlowInstance instance = runtimeService().resume(instanceId, actualCreator, args);
        FlowProcessModel model = runtimeService().getProcessModelByInstanceId(instanceId);
        FlowNodeModel targetNode = nodeKey == null ? model.getNode(instance.getCurrentNodeKey()) : model.getNode(nodeKey);
        if (targetNode == null) {
            targetNode = model.getNodeConfig();
        }
        Map<String, Object> variables = new HashMap<>(instance.variableToMap());
        if (args != null) {
            variables.putAll(args);
        }
        FlowExecution execution = new FlowExecution(context, model, actualCreator, instance, variables);
        targetNode.execute(context, execution);
        return Optional.of(execution.getFlowTasks());
    }

    private boolean createNextSequentialTask(FlowNodeModel nodeModel, FlowExecution execution, FlowTask completedTask) {
        List<FlowHisTaskActor> hisActors = queryService().getHisTaskActorsByTaskId(completedTask.getId());
        if (hisActors == null || hisActors.isEmpty()) {
            return false;
        }
        String currentActorId = hisActors.get(0).getActorId();
        List<FlowTaskActor> configuredActors = execution.getProviderTaskActors(nodeModel);
        if (configuredActors == null || configuredActors.isEmpty()) {
            return false;
        }
        for (int i = 0; i < configuredActors.size(); i++) {
            FlowTaskActor actor = configuredActors.get(i);
            if (currentActorId != null && currentActorId.equals(actor.getActorId()) && i + 1 < configuredActors.size()) {
                taskService().createSequentialTask(nodeModel, execution, configuredActors.get(i + 1));
                return true;
            }
        }
        return false;
    }

    private VoteResult resolveCountersignResult(FlowNodeModel nodeModel, FlowTask task) {
        FlowSignPolicy policy = FlowSignPolicy.of(nodeModel);
        int defaultWeight = defaultVoteWeight(nodeModel, task);
        VoteStats stats = collectVoteStats(task, defaultWeight, policy);
        if (stats.vetoRejected) {
            return VoteResult.REJECT;
        }
        if (policy.getRejectWeight() == null && stats.rejectedCount > 0) {
            return VoteResult.REJECT;
        }
        if (policy.getRejectWeight() != null && stats.rejectedWeight >= policy.getRejectWeight()) {
            return VoteResult.REJECT;
        }
        return stats.activeCount > 0 ? VoteResult.WAIT : VoteResult.PASS;
    }

    private VoteResult resolveOrSignResult(FlowNodeModel nodeModel, FlowTask task) {
        FlowSignPolicy policy = FlowSignPolicy.of(nodeModel);
        int defaultWeight = defaultVoteWeight(nodeModel, task);
        VoteStats stats = collectVoteStats(task, defaultWeight, policy);
        if (stats.passedCount > 0) {
            return VoteResult.PASS;
        }
        if (stats.vetoRejected || (policy.getRejectWeight() != null && stats.rejectedWeight >= policy.getRejectWeight())) {
            return VoteResult.REJECT;
        }
        return stats.activeCount > 0 ? VoteResult.WAIT : VoteResult.REJECT;
    }

    private VoteResult resolveVoteResult(FlowNodeModel nodeModel, FlowTask task) {
        FlowSignPolicy policy = FlowSignPolicy.of(nodeModel);
        int defaultWeight = defaultVoteWeight(nodeModel, task);
        VoteStats stats = collectVoteStats(task, defaultWeight, policy);
        if (stats.vetoRejected) {
            return VoteResult.REJECT;
        }
        if (stats.passedWeight >= policy.getPassWeight()) {
            return VoteResult.PASS;
        }
        if (policy.getRejectWeight() != null && stats.rejectedWeight >= policy.getRejectWeight()) {
            return VoteResult.REJECT;
        }
        return stats.passedWeight + stats.activeWeight >= policy.getPassWeight() ? VoteResult.WAIT : VoteResult.REJECT;
    }

    private VoteStats collectVoteStats(FlowTask task, int defaultWeight, FlowSignPolicy policy) {
        VoteStats stats = new VoteStats();
        List<FlowHisTask> hisTasks = queryService().getHisTasksByInstanceId(task.getInstanceId());
        for (FlowHisTask hisTask : hisTasks) {
            if (!task.getTaskKey().equals(hisTask.getTaskKey())) {
                continue;
            }
            int weight = hisTaskWeight(hisTask, defaultWeight);
            if (isPassedState(hisTask.getTaskState())
                    || (TaskState.ABSTAINED.value().equals(hisTask.getTaskState()) && policy.isAbstainAsPass())) {
                stats.passedCount++;
                stats.passedWeight += weight;
            } else if (isRejectedState(hisTask.getTaskState())) {
                stats.rejectedCount++;
                stats.rejectedWeight += weight;
                stats.vetoRejected = stats.vetoRejected || hasVetoActor(hisTask, policy);
            } else if (TaskState.ABSTAINED.value().equals(hisTask.getTaskState())) {
                stats.abstainedCount++;
                stats.abstainedWeight += weight;
            }
        }
        List<FlowTask> activeTasks = queryService().getTasksByInstanceIdAndTaskKey(task.getInstanceId(), task.getTaskKey());
        if (activeTasks != null) {
            for (FlowTask activeTask : activeTasks) {
                stats.activeCount++;
                stats.activeWeight += activeTaskWeight(activeTask, defaultWeight);
            }
        }
        return stats;
    }

    private int activeTaskWeight(FlowTask task, int defaultWeight) {
        int weight = 0;
        List<FlowTaskActor> actors = queryService().getTaskActorsByTaskId(task.getId());
        if (actors == null || actors.isEmpty()) {
            return defaultWeight;
        }
        for (FlowTaskActor actor : actors) {
            weight += actor.getWeight() == null ? defaultWeight : actor.getWeight();
        }
        return weight;
    }

    private int hisTaskWeight(FlowHisTask hisTask, int defaultWeight) {
        int weight = 0;
        List<FlowHisTaskActor> actors = queryService().getHisTaskActorsByTaskId(hisTask.getId());
        if (actors == null || actors.isEmpty()) {
            return defaultWeight;
        }
        for (FlowHisTaskActor actor : actors) {
            weight += actor.getWeight() == null ? defaultWeight : actor.getWeight();
        }
        return weight;
    }

    private boolean hasVetoActor(FlowHisTask hisTask, FlowSignPolicy policy) {
        if (policy.getVetoWeight() == null) {
            return false;
        }
        List<FlowHisTaskActor> actors = queryService().getHisTaskActorsByTaskId(hisTask.getId());
        if (actors == null || actors.isEmpty()) {
            return false;
        }
        for (FlowHisTaskActor actor : actors) {
            if (actor.getWeight() != null && actor.getWeight() >= policy.getVetoWeight()) {
                return true;
            }
        }
        return false;
    }

    private int defaultVoteWeight(FlowNodeModel nodeModel, FlowTask task) {
        int actorCount = voteActorCount(task);
        if (actorCount > 0) {
            return (100 + actorCount - 1) / actorCount;
        }
        List<FlowNodeAssignee> assignees = nodeModel.getNodeAssigneeList();
        if (assignees == null || assignees.isEmpty()) {
            return 0;
        }
        return (100 + assignees.size() - 1) / assignees.size();
    }

    private int voteActorCount(FlowTask task) {
        int count = 0;
        List<FlowHisTask> hisTasks = queryService().getHisTasksByInstanceId(task.getInstanceId());
        for (FlowHisTask hisTask : hisTasks) {
            if (!task.getTaskKey().equals(hisTask.getTaskKey())) {
                continue;
            }
            List<FlowHisTaskActor> actors = queryService().getHisTaskActorsByTaskId(hisTask.getId());
            count += actors == null || actors.isEmpty() ? 1 : actors.size();
        }
        List<FlowTask> activeTasks = queryService().getTasksByInstanceIdAndTaskKey(task.getInstanceId(), task.getTaskKey());
        if (activeTasks != null) {
            for (FlowTask activeTask : activeTasks) {
                List<FlowTaskActor> actors = queryService().getTaskActorsByTaskId(activeTask.getId());
                count += actors == null || actors.isEmpty() ? 1 : actors.size();
            }
        }
        return count;
    }

    private boolean isPassedState(Integer taskState) {
        return TaskState.COMPLETED.value().equals(taskState)
                || TaskState.AUTO_COMPLETED.value().equals(taskState);
    }

    private boolean isRejectedState(Integer taskState) {
        return TaskState.REJECTED.value().equals(taskState)
                || TaskState.AUTO_REJECTED.value().equals(taskState)
                || TaskState.REJECT_END.value().equals(taskState);
    }

    private void completeActiveSameNodeTasks(FlowTask task, FlowCreator creator, TaskState state) {
        taskService().completeActiveTasksByInstanceIdAndTaskKey(task.getInstanceId(), task.getTaskKey(),
                creator == null ? FlowCreator.ADMIN : creator, state);
    }

    private enum VoteResult {
        PASS,
        WAIT,
        REJECT
    }

    private static class VoteStats {

        private int passedWeight;

        private int rejectedWeight;

        private int abstainedWeight;

        private int activeWeight;

        private int passedCount;

        private int rejectedCount;

        private int abstainedCount;

        private int activeCount;

        private boolean vetoRejected;
    }

    private Optional<List<FlowTask>> rollbackToHisTask(Long hisTaskId, FlowCreator creator, Map<String, Object> args, TaskState state) {
        FlowHisTask hisTask = queryService().getHisTask(hisTaskId);
        if (hisTask == null) {
            return Optional.empty();
        }
        if (hisTask.getCreateId() != null && !hisTask.getCreateId().equals(creator.getCreateId())) {
            throw new IllegalStateException("当前用户无权回滚该历史任务，hisTaskId=" + hisTaskId);
        }
        FlowInstance instance = queryService().getInstance(hisTask.getInstanceId());
        if (instance == null) {
            return Optional.empty();
        }
        FlowProcessModel model = runtimeService().getProcessModelByInstanceId(instance.getId());
        FlowNodeModel targetNode = model.getNode(hisTask.getTaskKey());
        if (targetNode == null) {
            return Optional.empty();
        }
        taskService().completeActiveTasksForJump(instance.getId(), null, creator, args, state);
        Map<String, Object> variables = new HashMap<>(instance.variableToMap());
        variables.putAll(hisTask.variableToMap());
        if (args != null) {
            variables.putAll(args);
        }
        FlowExecution execution = new FlowExecution(context, model, creator, instance, variables);
        execution.setFlowTask(hisTask);
        targetNode.execute(context, execution);
        return Optional.of(execution.getFlowTasks());
    }

    private FlowNodeModel resolveRejectTarget(FlowProcessModel model, FlowNodeModel currentNode, String specifiedNodeKey, Long instanceId) {
        if (StrUtil.isNotBlank(specifiedNodeKey)) {
            FlowNodeModel specifiedNode = model.getNode(specifiedNodeKey);
            if (specifiedNode == null) {
                throw new IllegalArgumentException("指定驳回节点不存在，nodeKey=" + specifiedNodeKey);
            }
            return assertRejectableNode(specifiedNode);
        }
        FlowRejectStrategyEnum strategy = FlowRejectStrategyEnum.of(currentNode == null ? null : currentNode.getRejectStrategy());
        if (strategy == FlowRejectStrategyEnum.TERMINATE_APPROVAL) {
            return null;
        }
        if (strategy == FlowRejectStrategyEnum.TO_INITIATOR) {
            return assertRejectableNode(model.getNodeConfig());
        }
        if (strategy == FlowRejectStrategyEnum.TO_PARENT_NODE && currentNode != null) {
            FlowNodeModel parent = findParentApprovalNode(currentNode);
            if (parent != null) {
                return assertRejectableNode(parent);
            }
        }
        return assertRejectableNode(findPreviousApprovalNode(model, instanceId));
    }

    private FlowNodeModel assertRejectableNode(FlowNodeModel node) {
        if (node == null) {
            return null;
        }
        if (!FlowNodeTypeEnum.START.eq(node.getType()) && !FlowNodeTypeEnum.APPROVAL.eq(node.getType())) {
            throw new IllegalArgumentException("只能驳回到发起节点或审批节点，nodeKey=" + node.getNodeKey());
        }
        return node;
    }

    private FlowNodeModel findParentApprovalNode(FlowNodeModel currentNode) {
        FlowNodeModel parent = currentNode.getParentNode();
        while (parent != null) {
            if (FlowNodeTypeEnum.START.eq(parent.getType()) || FlowNodeTypeEnum.APPROVAL.eq(parent.getType())) {
                return parent;
            }
            parent = parent.getParentNode();
        }
        return null;
    }

    private FlowNodeModel findPreviousApprovalNode(FlowProcessModel model, Long instanceId) {
        List<FlowHisTask> hisTasks = new ArrayList<>(queryService().getHisTasksByInstanceId(instanceId));
        hisTasks.sort(Comparator
                .comparing(FlowHisTask::getFinishTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(FlowHisTask::getId, Comparator.nullsLast(Comparator.reverseOrder())));
        for (FlowHisTask hisTask : hisTasks) {
            FlowNodeModel node = model.getNode(hisTask.getTaskKey());
            if (node != null && (FlowNodeTypeEnum.START.eq(node.getType()) || FlowNodeTypeEnum.APPROVAL.eq(node.getType()))) {
                return node;
            }
        }
        return model.getNodeConfig();
    }
}

