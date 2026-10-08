package org.nkk.flow.model.node.task;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.nkk.flow.enums.node.FlowNodeTypeEnum;
import org.nkk.flow.model.node.FlowNodeType;

/**
 * 审批节点（type=1）。
 */
@Data
@FlowNodeType(FlowNodeTypeEnum.APPROVAL)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ApprovalNodeModel extends TaskNodeModel {

    private static final long serialVersionUID = 1L;

    /**
     * 审批参与方式，取值见 {@link org.nkk.flow.enums.core.FlowTaskEnum.PerformType}。
     */
    private Integer examineMode;

    /**
     * 票签通过权重，票签场景下达到该权重后视为通过。
     */
    private Integer passWeight;

    /**
     * 驳回策略，取值见 {@link org.nkk.flow.enums.node.FlowRejectStrategyEnum}，默认 4=终止流程。
     */
    private Integer rejectStrategy = 4;

    /**
     * 是否开启任务到期自动处理。
     */
    private Boolean termAuto;

    /**
     * 任务期限，单位小时。
     */
    private Integer term;

    /**
     * 任务到期处理模式，1 表示超时自动拒绝，其他值按自动通过处理。
     */
    private Integer termMode;

    /**
     * 是否开启任务提醒（默认开启，仅显式关闭时为 false）。
     */
    private Boolean remind = true;

    /**
     * 超时时间（小时）：开启提醒后，审批提交超过该时长未处理即高亮预警并首次催办。
     */
    private Integer remindTerm;

    /**
     * 催办频率（小时）：超时催办后，该时长内不能再次催办，即两次催办之间的最小间隔。
     */
    private Integer remindInterval;

    /**
     * 节点任务办理页地址。
     */
    private String actionUrl;

    /**
     * 审批人自审：0=自己审批（默认，提交人自己处理），1=自动跳过，2=转交给上级。
     */
    private Integer approveSelf = 0;

    /**
     * 部门领导层级。setType=2（部门领导，指定级别）或 setType=6（连续多级审批，起始级别）时使用。
     */
    private Integer examineLevel;

    /**
     * 是否允许审批转交，默认 false（禁止转办、委派、代理等转交类操作）。
     */
    private Boolean allowTransfer = false;

    /**
     * 是否允许审批加签或减签，默认 false（禁止加签、减签操作）。
     */
    private Boolean allowAppendNode = false;

    /**
     * 是否允许审批回退，默认 false（禁止选择目标节点回退）。
     */
    private Boolean allowRollback = false;

    /**
     * 是否允许审批节点手动抄送，默认 true。
     *
     * <p>false 表示当前节点禁止调用手动抄送操作。</p>
     */
    private Boolean allowCc = true;

    /**
     * 是否允许发起人撤回流程，默认 true。
     *
     * <p>审批进行中发起人可撤回（撤销）整个流程实例；置 false 时当前节点禁止撤回。</p>
     */
    private Boolean allowRevoke = true;

    /**
     * 判断当前节点是否需要保存办理人权重。
     *
     * @return true 表示当前节点是票签模式，需要保存权重
     */
    public boolean saveWeight() {
        return Integer.valueOf(4).equals(examineMode);
    }
}
