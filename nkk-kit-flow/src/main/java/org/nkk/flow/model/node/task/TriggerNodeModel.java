package org.nkk.flow.model.node.task;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.nkk.flow.enums.node.FlowNodeTypeEnum;
import org.nkk.flow.model.node.FlowNodeType;

/**
 * 触发器节点（type=7）。
 */
@Data
@FlowNodeType(FlowNodeTypeEnum.TRIGGER)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TriggerNodeModel extends TaskNodeModel {

    private static final long serialVersionUID = 1L;

    /**
     * 触发器类型，预留给业务侧或扩展点识别触发方式。
     */
    private Integer triggerType;
}
