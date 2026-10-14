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
     * 流程定义信息。
     */
    private Process process;

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
     * 创建人 ID。
     */
    private String createId;

    /**
     * 发起人。
     */
    private String createBy;

    /**
     * 发起时间。
     */
    private java.util.Date createTime;

    /**
     * 流程结束时间（审批中为 null）。
     */
    private java.util.Date endTime;

    /**
     * 审批流程是否实际发起。
     *
     * <p>true=已创建审批实例；false=业务审批触发条件（triggerExpression）不满足，
     * 未发起审批流程，引擎已直接派发「审批通过」业务回调。
     * 此时 instanceId 为 null、instanceState=1（审批通过），业务方应直接按通过处理。</p>
     */
    private Boolean started = true;

    /**
     * 当前活动任务。
     */
    private List<Task> task = new ArrayList<>();

    /**
     * 当前请求人对该实例的操作权限（由后端根据当前用户 + 节点模型计算）。
     */
    private Permissions permissions;

    /**
     * 流程定义信息。
     */
    @Data
    public static class Process implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 流程定义 ID。 */
        private Long processId;

        /** 流程定义名称。 */
        private String processName;

        /** 流程图标。 */
        private String processIcon;

        /** 流程类型。 */
        private String processType;

        /** 流程版本。 */
        private Integer processVersion;

        /** 实例跳转地址模板。 */
        private String instanceUrl;

        /** 流程备注。 */
        private String remark;

        /** 表单来源类型。 */
        private String formSourceType;
    }

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

    /**
     * 当前请求人对该实例的操作权限标志。
     *
     * <p>由后端在 {@link FlowDesignerRuntimeService#detail(Long)} 中统一计算，
     * 前端直接据此控制按钮显隐，无需前端猜测权限。</p>
     */
    @Data
    public static class Permissions implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 当前用户是否为流程发起人（createId 匹配） */
        private Boolean isInitiator;

        /** 可撤回：审批中 + 发起人（accessStrategy REVOKE 校验通过） */
        private Boolean canRevoke;

        /** 可催办：审批中 + 发起人 + 任务已超时 + 催办间隔已过 */
        private Boolean canUrge;

        /** 可审批：审批中 + 当前用户是活动任务参与人 */
        private Boolean canApprove;

        /** 可转交：canApprove + 节点 allowTransfer */
        private Boolean canTransfer;

        /** 可加签/减签：canApprove + 节点 allowAppendNode */
        private Boolean canAppendNode;

        /** 可回退：canApprove + 节点 allowRollback */
        private Boolean canRollback;

        /** 可手动抄送：审批中 + 节点 allowCc 不为 false */
        private Boolean canCc;
    }
}
