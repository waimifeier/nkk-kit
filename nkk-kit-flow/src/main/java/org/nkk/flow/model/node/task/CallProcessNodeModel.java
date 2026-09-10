package org.nkk.flow.model.node.task;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.nkk.flow.enums.node.FlowNodeTypeEnum;
import org.nkk.flow.model.node.FlowNodeType;

/**
 * 子流程节点（type=5）。
 *
 * <p>同步子流程会在父流程生成一个等待任务，因此挂载在任务节点基类下。</p>
 */
@Data
@FlowNodeType(FlowNodeTypeEnum.CALL_PROCESS)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CallProcessNodeModel extends TaskNodeModel {

    private static final long serialVersionUID = 1L;

    /**
     * 子流程引用。默认支持 processKey、processKey:version、processId 三种写法。
     */
    private String callProcess;

    /**
     * 子流程是否异步执行，true 表示启动子流程后父流程继续向下流转。
     */
    private Boolean callAsync;
}
