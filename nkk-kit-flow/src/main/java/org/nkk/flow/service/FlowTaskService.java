package org.nkk.flow.service;

import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.core.context.FlowExecution;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.entity.FlowTaskActor;
import org.nkk.flow.enums.core.FlowTaskEnum.PerformType;
import org.nkk.flow.enums.core.FlowTaskEnum.TaskState;
import org.nkk.flow.model.node.FlowNodeModel;
import org.nkk.flow.model.node.task.TaskNodeModel;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 流程任务服务。
 */
public interface FlowTaskService {

    List<FlowTask> createTask(TaskNodeModel nodeModel, FlowExecution execution);

    FlowTask createSequentialTask(TaskNodeModel nodeModel, FlowExecution execution, FlowTaskActor actor);

    FlowTask complete(Long taskId, FlowCreator creator, Map<String, Object> args);

    default FlowTask complete(Long taskId, FlowCreator creator, Map<String, Object> args, String opinion) {
        return complete(taskId, creator, args);
    }

    FlowTask executeTask(Long taskId, FlowCreator creator, Map<String, Object> args, TaskState state);

    default FlowTask executeTask(Long taskId, FlowCreator creator, Map<String, Object> args,
                                 TaskState state, String opinion) {
        return executeTask(taskId, creator, args, state);
    }

    boolean updateHisTaskState(Long hisTaskId, TaskState state, Map<String, Object> args);

    void updateTask(FlowTask task);

    Optional<List<FlowTask>> rejectTask(FlowTask task, FlowCreator creator, Map<String, Object> args);

    default Optional<List<FlowTask>> rejectTask(FlowTask task, FlowCreator creator, Map<String, Object> args,
                                                String opinion) {
        return rejectTask(task, creator, args);
    }

    void completeActiveTasksForJump(Long instanceId, FlowTask currentTask, FlowCreator creator, Map<String, Object> args, TaskState state);

    default void completeActiveTasksForJump(Long instanceId, FlowTask currentTask, FlowCreator creator,
                                            Map<String, Object> args, TaskState state, String opinion) {
        completeActiveTasksForJump(instanceId, currentTask, creator, args, state);
    }

    boolean completeActiveTasksByInstanceId(Long instanceId, FlowCreator creator, TaskState state);

    boolean completeActiveTasksByInstanceIdAndTaskKey(Long instanceId, String taskKey, FlowCreator creator,
                                                      TaskState state);

    boolean transferTask(Long taskId, FlowCreator creator, FlowCreator assignee, Map<String, Object> args,
                         String opinion);

    default boolean transferTask(Long taskId, FlowCreator creator, FlowCreator assignee, Map<String, Object> args) {
        return transferTask(taskId, creator, assignee, args, null);
    }

    boolean delegateTask(Long taskId, FlowCreator creator, FlowCreator assignee, Map<String, Object> args,
                         String opinion);

    default boolean delegateTask(Long taskId, FlowCreator creator, FlowCreator assignee, Map<String, Object> args) {
        return delegateTask(taskId, creator, assignee, args, null);
    }

    boolean resolveTask(Long taskId, FlowCreator creator);

    boolean agentTask(Long taskId, FlowCreator creator, List<FlowCreator> agents, Map<String, Object> args,
                      String opinion);

    default boolean agentTask(Long taskId, FlowCreator creator, List<FlowCreator> agents, Map<String, Object> args) {
        return agentTask(taskId, creator, agents, args, null);
    }

    boolean claimTask(Long taskId, FlowCreator creator);

    FlowTask claimRole(Long taskId, FlowCreator creator);

    FlowTask claimDepartment(Long taskId, FlowCreator creator);

    boolean viewTask(Long taskId, FlowCreator creator);

    boolean copyTask(Long taskId, List<FlowTaskActor> actors, FlowCreator creator, Map<String, Object> args,
                     String opinion);

    boolean addTaskActor(Long taskId, PerformType performType, List<FlowTaskActor> actors, FlowCreator creator);

    boolean removeTaskActor(Long taskId, List<String> actorIds, FlowCreator creator);

    boolean changeTaskActor(Long taskId, FlowTaskActor actor);

    List<FlowTask> endCallProcessTask(Long callProcessId, Long callInstanceId);

    List<FlowTask> finishCallProcessTask(Long callProcessId, Long callInstanceId, FlowCreator creator,
                                         TaskState state);

    boolean cascadeRemoveByInstanceIds(List<Long> instanceIds);

    FlowTaskActor isAllowed(FlowTask task, String userId);
}

