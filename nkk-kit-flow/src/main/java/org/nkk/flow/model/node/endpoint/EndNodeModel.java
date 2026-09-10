package org.nkk.flow.model.node.endpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.nkk.flow.enums.node.FlowNodeTypeEnum;
import org.nkk.flow.model.node.FlowNodeModel;
import org.nkk.flow.model.node.FlowNodeType;

/**
 * 结束节点（type=-1），执行到该节点时实例正常完成。
 */
@Data
@FlowNodeType(FlowNodeTypeEnum.END)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EndNodeModel extends FlowNodeModel {

    private static final long serialVersionUID = 1L;
}
