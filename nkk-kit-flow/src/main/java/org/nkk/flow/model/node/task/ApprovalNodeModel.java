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
     * 驳回策略，取值见 {@link org.nkk.flow.enums.node.FlowRejectStrategyEnum}。
     */
    private Integer rejectStrategy;

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
     * 是否开启任务提醒。
     */
    private Boolean remind;

    /**
     * 节点任务办理页地址。
     */
    private String actionUrl;

    /**
     * 审批人自审：1 表示审批人与发起人相同时自动跳过该审批，其他值正常创建审批任务。
     */
    private Integer approveSelf;

    /**
     * 部门领导层级。setType=2（部门领导，指定级别）或 setType=6（连续多级审批，起始级别）时使用。
     */
    private Integer examineLevel;

    /**
     * 是否允许审批转交。
     *
     * <p>false 表示当前节点禁止调用转办、委派、代理等转交类操作。</p>
     */
    private Boolean allowTransfer;

    /**
     * 是否允许审批加签或减签。
     *
     * <p>false 表示当前节点禁止调用加签、减签操作。</p>
     */
    private Boolean allowAppendNode;

    /**
     * 是否允许审批回退。
     *
     * <p>false 表示当前节点禁止选择目标节点回退。</p>
     */
    private Boolean allowRollback;

    /**
     * 是否允许审批节点手动抄送。
     *
     * <p>false 表示当前节点禁止调用手动抄送操作。</p>
     */
    private Boolean allowCc;

    /**
     * 判断当前节点是否需要保存办理人权重。
     *
     * @return true 表示当前节点是票签模式，需要保存权重
     */
    public boolean saveWeight() {
        return Integer.valueOf(4).equals(examineMode);
    }
}
