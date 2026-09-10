package org.nkk.flow.service;

import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.entity.FlowInstance;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.entity.FlowTaskActor;
import org.nkk.flow.enums.core.FlowTaskEnum.PerformType;
import org.nkk.flow.model.node.FlowNodeModel;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * NKK 审批流引擎门面。
 */
public interface NkkFlowEngine {

    NkkFlowEngine configure(FlowContext context);

    FlowContext getContext();

    default FlowProcessService processService() {
        return getContext().getProcessService();
    }

    default FlowRuntimeService runtimeService() {
        return getContext().getRuntimeService();
    }

    default FlowTaskService taskService() {
        return getContext().getTaskService();
    }

    default FlowQueryService queryService() {
        return getContext().getQueryService();
    }

    Optional<FlowInstance> startInstanceByProcessKey(String processKey, Integer version, FlowCreator creator, Map<String, Object> args);

    default Optional<FlowInstance> startInstanceByProcessKey(String processKey, Integer version, Map<String, Object> args) {
        return startInstanceByProcessKey(processKey, version, null, args);
    }

    Optional<FlowInstance> startInstanceByProcessKey(String processKey, Integer version, FlowCreator creator,
                                                     Map<String, Object> args, boolean saveAsDraft,
                                                     Consumer<FlowNodeModel> checkNodeModel,
                                                     Supplier<FlowInstance> supplier);

    default Optional<FlowInstance> startInstanceByProcessKey(String processKey, Integer version, FlowCreator creator, String businessKey) {
        return startInstanceByProcessKey(processKey, version, creator, null, false, null, () -> FlowInstance.of(businessKey));
    }

    Optional<FlowInstance> startInstanceById(Long processId, FlowCreator creator, Map<String, Object> args);

    default Optional<FlowInstance> startInstanceById(Long processId, Map<String, Object> args) {
        return startInstanceById(processId, null, args);
    }

    Optional<FlowInstance> startInstanceById(Long processId, FlowCreator creator, Map<String, Object> args,
                                             boolean saveAsDraft,
                                             Consumer<FlowNodeModel> checkNodeModel,
                                             Supplier<FlowInstance> supplier);

    default Optional<FlowInstance> startInstanceById(Long processId, FlowCreator creator, String businessKey) {
        return startInstanceById(processId, creator, null, false, null, () -> FlowInstance.of(businessKey));
    }

    boolean executeTask(Long taskId, FlowCreator creator, Map<String, Object> args);

    boolean executeTask(Long taskId, FlowCreator creator, Map<String, Object> args, String opinion);

    default boolean executeTask(Long taskId, Map<String, Object> args) {
        return executeTask(taskId, null, args);
    }

    default boolean executeTask(Long taskId, FlowCreator creator) {
        return executeTask(taskId, creator, null);
    }

    default boolean executeTask(Long taskId) {
        return executeTask(taskId, null, null);
    }

    boolean executeFinishTrigger(Long taskId, FlowCreator creator, Map<String, Object> args);

    default boolean executeFinishTrigger(Long taskId, FlowCreator creator) {
        return executeFinishTrigger(taskId, creator, null);
    }

    boolean autoCompleteTask(Long taskId, Map<String, Object> args, FlowCreator creator);

    default boolean autoCompleteTask(Long taskId, FlowCreator creator) {
        return autoCompleteTask(taskId, null, creator);
    }

    default boolean autoCompleteTask(Long taskId) {
        return autoCompleteTask(taskId, (FlowCreator) null);
    }

    boolean autoRejectTask(FlowTask task, Map<String, Object> args, FlowCreator creator);

    default boolean autoRejectTask(FlowTask task, FlowCreator creator) {
        return autoRejectTask(task, null, creator);
    }

    boolean voteRejectTask(Long taskId, FlowCreator creator, Map<String, Object> args);

    default boolean voteRejectTask(Long taskId, Map<String, Object> args) {
        return voteRejectTask(taskId, null, args);
    }

    default boolean voteRejectTask(Long taskId, FlowCreator creator) {
        return voteRejectTask(taskId, creator, null);
    }

    boolean abstainTask(Long taskId, FlowCreator creator, Map<String, Object> args);

    default boolean abstainTask(Long taskId, Map<String, Object> args) {
        return abstainTask(taskId, null, args);
    }

    default boolean abstainTask(Long taskId, FlowCreator creator) {
        return abstainTask(taskId, creator, null);
    }

    Optional<java.util.List<FlowTask>> executeJumpTask(Long taskId, String nodeKey, FlowCreator creator, Map<String, Object> args);

    default Optional<java.util.List<FlowTask>> executeJumpTask(Long taskId, String nodeKey, Map<String, Object> args) {
        return executeJumpTask(taskId, nodeKey, null, args);
    }

    default boolean executeJumpTask(Long taskId, String nodeKey, FlowCreator creator) {
        return executeJumpTask(taskId, nodeKey, creator, null).isPresent();
    }

    boolean redeployProcessModel(Long instanceId, Function<org.nkk.flow.model.FlowProcessModel, org.nkk.flow.model.FlowProcessModel> function);

    boolean executeAppendNodeModel(Long taskId, FlowNodeModel nodeModel,
                                   FlowCreator creator, Map<String, Object> args, boolean beforeAfter);

    default boolean executeAppendNodeModel(Long taskId, FlowNodeModel nodeModel,
                                           FlowCreator creator, boolean beforeAfter) {
        return executeAppendNodeModel(taskId, nodeModel, creator, null, beforeAfter);
    }

    boolean executeRemoveNodeModel(Long instanceId, String nodeKey);

    Optional<java.util.List<FlowTask>> rejectTask(Long taskId, FlowCreator creator, Map<String, Object> args);

    Optional<java.util.List<FlowTask>> rejectTask(Long taskId, FlowCreator creator, Map<String, Object> args, String opinion);

    default Optional<java.util.List<FlowTask>> rejectTask(Long taskId, Map<String, Object> args) {
        return rejectTask(taskId, (FlowCreator) null, args);
    }

    Optional<java.util.List<FlowTask>> rejectTask(Long taskId, String nodeKey, FlowCreator creator, Map<String, Object> args);

    Optional<java.util.List<FlowTask>> rejectTask(Long taskId, String nodeKey, FlowCreator creator, Map<String, Object> args, String opinion);

    default Optional<java.util.List<FlowTask>> rejectTask(Long taskId, String nodeKey, Map<String, Object> args) {
        return rejectTask(taskId, nodeKey, null, args);
    }

    Optional<java.util.List<FlowTask>> terminateByTask(Long taskId, FlowCreator creator, Map<String, Object> args);

    boolean suspendInstanceById(Long instanceId, FlowCreator creator);

    boolean activeInstanceById(Long instanceId, FlowCreator creator);

    boolean revokeInstanceById(Long instanceId, FlowCreator creator);

    boolean timeoutInstanceById(Long instanceId, FlowCreator creator);

    default boolean destroyByInstanceId(Long instanceId, Map<String, Object> args) {
        return destroyByInstanceId(instanceId, null, args);
    }

    boolean destroyByInstanceId(Long instanceId, FlowCreator creator, Map<String, Object> args);

    boolean addVariable(Long instanceId, Map<String, Object> args);

    void cascadeRemoveByInstanceId(Long instanceId, FlowCreator creator);

    void cascadeRemoveByProcessId(Long processId);

    boolean transferTask(Long taskId, FlowCreator creator, FlowCreator assignee, Map<String, Object> args,
                         String opinion);

    default boolean transferTask(Long taskId, FlowCreator creator, FlowCreator assignee, Map<String, Object> args) {
        return transferTask(taskId, creator, assignee, args, null);
    }

    default boolean transferTask(Long taskId, FlowCreator assignee, Map<String, Object> args) {
        return transferTask(taskId, null, assignee, args);
    }

    boolean delegateTask(Long taskId, FlowCreator creator, FlowCreator assignee, Map<String, Object> args,
                         String opinion);

    default boolean delegateTask(Long taskId, FlowCreator creator, FlowCreator assignee, Map<String, Object> args) {
        return delegateTask(taskId, creator, assignee, args, null);
    }

    default boolean delegateTask(Long taskId, FlowCreator assignee, Map<String, Object> args) {
        return delegateTask(taskId, null, assignee, args);
    }

    boolean resolveTask(Long taskId, FlowCreator creator);

    default boolean resolveTask(Long taskId) {
        return resolveTask(taskId, null);
    }

    boolean agentTask(Long taskId, FlowCreator creator, java.util.List<FlowCreator> agents, Map<String, Object> args,
                      String opinion);

    default boolean agentTask(Long taskId, FlowCreator creator, java.util.List<FlowCreator> agents,
                              Map<String, Object> args) {
        return agentTask(taskId, creator, agents, args, null);
    }

    default boolean agentTask(Long taskId, java.util.List<FlowCreator> agents, Map<String, Object> args) {
        return agentTask(taskId, null, agents, args);
    }

    boolean claimTask(Long taskId, FlowCreator creator);

    default boolean claimTask(Long taskId) {
        return claimTask(taskId, null);
    }

    FlowTask claimRole(Long taskId, FlowCreator creator);

    default FlowTask claimRole(Long taskId) {
        return claimRole(taskId, null);
    }

    FlowTask claimDepartment(Long taskId, FlowCreator creator);

    default FlowTask claimDepartment(Long taskId) {
        return claimDepartment(taskId, null);
    }

    boolean viewTask(Long taskId, FlowCreator creator);

    default boolean viewTask(Long taskId) {
        return viewTask(taskId, null);
    }

    boolean copyTask(Long taskId, java.util.List<FlowTaskActor> actors, FlowCreator creator, Map<String, Object> args,
                     String opinion);

    default boolean copyTask(Long taskId, java.util.List<FlowTaskActor> actors, Map<String, Object> args,
                             String opinion) {
        return copyTask(taskId, actors, null, args, opinion);
    }

    boolean changeTaskActor(Long taskId, FlowTaskActor actor);

    boolean addTaskActor(Long taskId, PerformType performType, java.util.List<FlowTaskActor> actors, FlowCreator creator);

    default boolean addTaskActor(Long taskId, PerformType performType, java.util.List<FlowTaskActor> actors) {
        return addTaskActor(taskId, performType, actors, null);
    }

    boolean removeTaskActor(Long taskId, java.util.List<String> actorIds, FlowCreator creator);

    default boolean removeTaskActor(Long taskId, java.util.List<String> actorIds) {
        return removeTaskActor(taskId, actorIds, null);
    }

    Optional<java.util.List<FlowTask>> withdrawTask(Long hisTaskId, FlowCreator creator, Map<String, Object> args);

    default Optional<java.util.List<FlowTask>> withdrawTask(Long hisTaskId, Map<String, Object> args) {
        return withdrawTask(hisTaskId, null, args);
    }

    Optional<java.util.List<FlowTask>> reclaimTask(Long hisTaskId, FlowCreator creator, Map<String, Object> args);

    default Optional<java.util.List<FlowTask>> reclaimTask(Long hisTaskId, Map<String, Object> args) {
        return reclaimTask(hisTaskId, null, args);
    }

    Optional<java.util.List<FlowTask>> resumeInstance(Long instanceId, String nodeKey, FlowCreator creator, Map<String, Object> args);

    default Optional<java.util.List<FlowTask>> resumeInstance(Long instanceId, String nodeKey, Map<String, Object> args) {
        return resumeInstance(instanceId, nodeKey, null, args);
    }

    void processTimeoutOrRemind(Date now);
}

