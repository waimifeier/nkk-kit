package org.nkk.flow.web.model;

import lombok.Data;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.entity.FlowTaskActor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 流程运行时响应。
 */
@Data
public class FlowRuntimeResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 流程实例 ID。
     */
    private Long instanceId;

    /**
     * 租户 ID。
     */
    private String tenantId;

    /**
     * 业务主键。
     */
    private String businessKey;

    /**
     * 当前节点名称。
     */
    private String currentNodeName;

    /**
     * 当前节点编码。
     */
    private String currentNodeKey;

    /**
     * 流程实例状态。
     */
    private Integer instanceState;

    /**
     * 当前活动任务。
     */
    private List<Task> task = new ArrayList<>();

    /**
     * 当前活动任务。
     */
    @Data
    public static class Task implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 任务 ID。
         */
        private Long taskId;

        /**
         * 任务名称。
         */
        private String taskName;

        /**
         * 任务节点编码。
         */
        private String taskKey;

        /**
         * 任务变量 JSON。
         */
        private String variable;

        /**
         * 任务类型。
         */
        private Integer taskType;

        /**
         * 任务参与人列表。
         */
        private List<FlowTaskActor> actors = new ArrayList<>();

        /**
         * 根据活动任务和参与人创建响应对象。
         *
         * @param task 活动任务
         * @param actors 任务参与人
         * @return 下一批活动任务响应
         */
        public static Task of(FlowTask task, List<FlowTaskActor> actors) {
            Task test = new Task();
            test.setTaskId(task.getId());
            test.setTaskName(task.getTaskName());
            test.setTaskKey(task.getTaskKey());
            test.setVariable(task.getVariable());
            test.setTaskType(task.getTaskType());
            test.setActors(actors == null ? new ArrayList<>() : actors);
            return test;
        }
    }
}
