package org.nkk.flow.web.model;

import lombok.Data;
import org.nkk.flow.enums.core.FlowInstanceEnum.InstanceState;
import org.nkk.flow.enums.core.FlowTaskEnum.TaskType;

import java.io.Serializable;
import java.util.Date;

/**
 * 待办中心流程记录。
 */
@Data
public class FlowTodoRecordResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前用户与该流程记录的关系类型，取值见 {@link org.nkk.flow.enums.core.FlowTodoTypeEnum}。
     */
    private Integer type;

    /**
     * 当前用户与该流程记录的关系类型名称。
     */
    private String typeName;

    /**
     * 当前流程实例信息。
     */
    private Instance instance;

    /**
     * 当前流程正在执行的活动任务信息。
     *
     * <p>不管 {@link #type} 是哪种关系，均返回当前流程实例的活动任务；流程已结束时为空。</p>
     */
    private Task task;

    /**
     * 当前流程实例信息。
     */
    @Data
    public static class Instance implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 流程实例 ID。
         */
        private Long instanceId;

        /**
         * 业务主键。
         */
        private String businessKey;

        /**
         * 流程定义 ID。
         */
        private Long processId;

        /**
         * 流程定义 key。
         */
        private String processKey;

        /**
         * 流程定义名称，即审批名称。
         */
        private String processName;

        /**
         * 流程定义版本。
         */
        private Integer processVersion;

        /**
         * 流程实例状态，取值见 {@link InstanceState}。
         */
        private Integer instanceState;

        /**
         * 流程实例状态名称。
         */
        private String instanceStateName;

        /**
         * 流程发起人 ID。
         */
        private String startUserId;

        /**
         * 流程发起人名称。
         */
        private String startUserName;

        /**
         * 流程发起时间。
         */
        private Date startTime;

        /**
         * 流程当前节点编码。
         */
        private String currentNodeKey;

        /**
         * 流程当前节点名称。
         */
        private String currentNodeName;

        /**
         * 流程实例耗时，单位毫秒。未结束时按当前时间动态计算。
         */
        private Long duration;
    }

    /**
     * 当前流程正在执行的活动任务信息。
     */
    @Data
    public static class Task implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 当前活动任务 ID。
         */
        private Long taskId;

        /**
         * 任务节点编码。
         */
        private String taskKey;

        /**
         * 任务节点名称。
         */
        private String taskName;

        /**
         * 任务类型，取值见 {@link TaskType}。
         */
        private Integer taskType;

        /**
         * 任务创建时间。
         */
        private Date taskCreateTime;

        /**
         * 抄送任务是否已阅，0 未阅，1 已阅。
         */
        private Integer viewed;
    }
}
