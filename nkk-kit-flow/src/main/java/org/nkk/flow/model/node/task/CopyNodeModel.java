package org.nkk.flow.model.node.task;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.nkk.flow.enums.node.FlowNodeTypeEnum;
import org.nkk.flow.model.node.FlowNodeType;

/**
 * 抄送节点（type=2）。
 */
@Data
@FlowNodeType(FlowNodeTypeEnum.COPY)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CopyNodeModel extends TaskNodeModel {

    private static final long serialVersionUID = 1L;
}
