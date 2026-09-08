package org.nkk.flow.core.extension.listener;

import lombok.Getter;
import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.core.context.FlowExecution;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.model.FlowNodeModel;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 节点生命周期事件。
 */
@Getter
public class FlowNodeEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 流程上下文。
     */
    private final transient FlowContext context;

    /**
     * 当前执行对象。
     */
    private final FlowExecution execution;

    /**
     * 当前节点模型。
     */
    private final FlowNodeModel nodeModel;

    /**
     * 当前节点创建出的任务列表。
     */
    private final List<FlowTask> tasks;

    private FlowNodeEvent(FlowContext context, FlowExecution execution, FlowNodeModel nodeModel, List<FlowTask> tasks) {
        this.context = context;
        this.execution = execution;
        this.nodeModel = nodeModel;
        this.tasks = tasks == null ? Collections.emptyList() : tasks;
    }

    public static FlowNodeEvent of(FlowContext context, FlowExecution execution, FlowNodeModel nodeModel) {
        return new FlowNodeEvent(context, execution, nodeModel, null);
    }

    public static FlowNodeEvent of(FlowContext context, FlowExecution execution, FlowNodeModel nodeModel, List<FlowTask> tasks) {
        return new FlowNodeEvent(context, execution, nodeModel, tasks);
    }
}
