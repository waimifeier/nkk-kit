package org.nkk.flow.web.model;

import lombok.Data;
import org.nkk.flow.enums.core.FlowInstanceEnum.InstanceState;
import org.nkk.flow.enums.core.FlowTaskEnum.TaskType;

import java.io.Serializable;
import java.util.Date;

/**
 * 待办中心流程记录（扁平结构）。
 */
@Data
public class FlowTodoRecordResponse implements Serializable {

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
     * 流程分类（flow_process.process_type）。
     */
    private String processType;

    /**
     * 流程详情页路由模板（flow_process.instance_url），宿主可直接使用或自行替换 {instanceId}。
     */
    private String instanceUrl;

    /**
     * 表单来源类型（flow_process.form_source_type），示例值：{@code form}（自定义表单）、{@code business}（业务表单）。
     */
    private String sourceType;

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

    /**
     * 审批记录时间（flow_his_task.finish_time）。
     * 仅"审批记录"接口填充，其他接口为 null。
     */
    private Date approveTime;

    /**
     * 当前流程正在执行的活动任务信息；流程已结束时为空。
     */
    private Task task;

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
