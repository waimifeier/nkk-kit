package org.nkk.flow.model.node.endpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.nkk.flow.enums.node.FlowNodeTypeEnum;
import org.nkk.flow.model.node.FlowNodeModel;
import org.nkk.flow.model.node.FlowNodeType;

/**
 * 自动拒绝节点（type=31），执行到该节点时实例直接以自动拒绝状态结束。
 */
@Data
@FlowNodeType(FlowNodeTypeEnum.AUTO_REJECT)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AutoRejectNodeModel extends FlowNodeModel {

    private static final long serialVersionUID = 1L;
}
