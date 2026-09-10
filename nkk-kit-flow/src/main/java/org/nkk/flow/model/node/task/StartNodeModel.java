package org.nkk.flow.model.node.task;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.nkk.flow.enums.node.FlowNodeTypeEnum;
import org.nkk.flow.model.node.FlowNodeType;

/**
 * 发起节点（type=0）。
 */
@Data
@FlowNodeType(FlowNodeTypeEnum.START)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StartNodeModel extends TaskNodeModel {

    private static final long serialVersionUID = 1L;
}
