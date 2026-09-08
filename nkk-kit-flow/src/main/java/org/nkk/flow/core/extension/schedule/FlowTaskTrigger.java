package org.nkk.flow.core.extension.schedule;

import org.nkk.flow.core.context.FlowExecution;
import org.nkk.flow.model.FlowNodeModel;

/**
 * 触发器任务扩展。
 */
public interface FlowTaskTrigger {

    boolean execute(FlowNodeModel nodeModel, FlowExecution execution);
}

