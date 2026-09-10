package org.nkk.flow.model.node.endpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.nkk.flow.enums.node.FlowNodeTypeEnum;
import org.nkk.flow.model.node.FlowNodeModel;
import org.nkk.flow.model.node.FlowNodeType;

/**
 * 自动通过节点（type=30），执行到该节点时实例直接以自动通过状态结束。
 */
@Data
@FlowNodeType(FlowNodeTypeEnum.AUTO_PASS)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AutoPassNodeModel extends FlowNodeModel {

    private static final long serialVersionUID = 1L;
}
