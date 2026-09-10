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
}
