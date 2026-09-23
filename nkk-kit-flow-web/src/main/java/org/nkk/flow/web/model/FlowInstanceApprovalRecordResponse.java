package org.nkk.flow.web.model;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 流程实例审批轨迹响应。
 *
 * <p>展示流程实例执行过程中经过的每个节点（历史 + 当前活动），
 * 按到达时间升序排列。前端按 {@code nodeStatus} 决定节点圆点颜色和标签文案。</p>
 */
@Data
public class FlowInstanceApprovalRecordResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 节点编码（FlowNodeModel.nodeKey），前端可用于和 FlowNodeMap 对齐。
     */
    private String nodeKey;

    /**
     * 节点名称。
     */
    private String nodeName;

    /**
     * 节点类型，取值见 {@link org.nkk.flow.enums.node.FlowNodeTypeEnum}，
     * 与 FlowNodeMap 组件使用的 type 一致：0=发起、1=审批、2=抄送、4=条件路由、-1=结束 等。
     */
    private Integer type;

    /**
     * 审批人设置类型，取值见 {@link org.nkk.flow.enums.node.FlowNodeSetTypeEnum}：
     * 1=指定成员、2=主管、3=角色、4=发起人自选、5=发起人本人、7=部门、8=候选人。
     * 仅任务节点（审批/发起/抄送等）有值，路由/结束等节点为 null。
     */
    private Integer setType;

    /**
     * 审批人设置类型中文名（指定成员、主管、角色…），前端直接展示。
     */
    private String setTypeName;

    /**
     * 审批方式（执行类型），取值见 {@link org.nkk.flow.enums.core.FlowTaskEnum.PerformType}：
     * 1=顺序审批、2=会签、3=或签、4=票签。
     * 路由/结束等非任务节点为 null。
     */
    private Integer performType;

    /**
     * 审批方式中文名（顺序审批、会签、或签、票签…），前端直接展示。
     */
    private String performTypeName;

    /**
     * 节点状态（统一枚举，前端直接映射颜色和标签）：
     * <ul>
     *     <li>0 — 已完成（正常走完）</li>
     *     <li>1 — 审批中（当前活动节点）</li>
     *     <li>2 — 已驳回</li>
     *     <li>3 — 已撤回</li>
     *     <li>4 — 已终止/已作废</li>
     *     <li>5 — 已跳过（跳转、自动通过等）</li>
     * </ul>
     */
    private Integer nodeStatus;

    /**
     * 状态中文标签（审批中、已通过、已驳回…），前端直接显示。
     */
    private String nodeStatusText;

    /**
     * 节点副标题（仅结束节点使用，描述流程终结原因）：
     * 如 "审批通过，流程结束" / "流程已撤回" / "审批被驳回，流程结束"。
     * 普通节点为 null，由后端按实例终态在合成结束节点时填充。
     */
    private String subtitle;

    /**
     * 任务到达时间（create_time）。发起节点即发起时间。
     */
    private Date createTime;

    /**
     * 任务完成时间（finish_time）。审批中和活动节点为 null。
     */
    private Date finishTime;

    /**
     * 最后办理人 ID（历史节点）或当前待办人 ID（活动节点）。
     */
    private String actorId;

    /**
     * 最后办理人名称或当前待办人名称。
     */
    private String actorName;

    /**
     * 审批意见（history 节点有值，活动节点为 null）。
     */
    private String opinion;

    /**
     * 节点办理人列表（展示完整候选人/参与人）。
     */
    private List<Assignee> assignees = new ArrayList<>();

    /**
     * 节点办理人/候选人，合并同 nodeKey 后每人携带各自的任务详情。
     */
    @Data
    public static class Assignee implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 参与人 ID。 */
        private String id;

        /** 参与人名称。 */
        private String name;

        /** 参与人类型：0=用户，1=角色，2=部门。 */
        private Integer actorType;

        /** 关联的任务 ID（FlowTask.id / FlowHisTask.id），用于定位该人的办理详情。 */
        private Long taskId;

        /** 该参与人的任务状态（引擎 TaskState：2=已完成，3=已驳回，null=待处理）。 */
        private Integer taskStatus;

        /** 该参与人的状态中文名（待处理 / 已通过 / 已驳回）。 */
        private String taskStatusText;

        /** 该参与人的审批意见（待处理时为 null）。 */
        private String opinion;

        /** 该参与人的任务完成时间（待处理时为 null）。 */
        private Date finishTime;
    }
}
