package org.nkk.flow.model.node.task;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.nkk.flow.enums.node.FlowNodeTypeEnum;
import org.nkk.flow.model.node.FlowNodeType;

/**
 * 延迟等待节点（type=6）。
 */
@Data
@FlowNodeType(FlowNodeTypeEnum.TIMER)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TimerNodeModel extends TaskNodeModel {

    private static final long serialVersionUID = 1L;

    /**
     * 延迟等待时长，配合 delayUnit 表示流程流转到该节点后等待的时间。
     */
    private Integer delayTime;

    /**
     * 延迟等待时间单位，取值 minute（分钟）、hour（小时）、day（天）。
     */
    private String delayUnit;
}
