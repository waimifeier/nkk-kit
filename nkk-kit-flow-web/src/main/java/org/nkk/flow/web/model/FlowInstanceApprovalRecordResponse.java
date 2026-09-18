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
     * 节点状态中文标签（审批中、已通过、已驳回…），前端直接显示。
     */
    private String nodeStatusText;

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
     * 节点办理人/候选人。
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
    }
}
