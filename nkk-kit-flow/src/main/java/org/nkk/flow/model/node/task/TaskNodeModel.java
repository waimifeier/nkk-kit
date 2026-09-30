package org.nkk.flow.model.node.task;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import org.nkk.flow.model.node.FlowNodeModel;

/**
 * 任务节点基类：发起、审批、抄送等会创建办理任务的节点类型。
 *
 * <p>仅持有这三类节点创建任务所需的公共配置：参与人设置类型、节点办理人。
 * 审批专属字段（参与方式、票签权重、驳回策略、期限、提醒、办理页地址等）见 {@link ApprovalNodeModel}。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class TaskNodeModel extends FlowNodeModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 审批人设置类型，取值见 {@link org.nkk.flow.enums.node.FlowNodeSetTypeEnum}。
     */
    private Integer setType;

    /**
     * 节点办理人配置。
     */
    private List<FlowNodeAssignee> nodeAssigneeList;
}
