package org.nkk.flow.core.extension.task;

import org.nkk.flow.core.context.FlowExecution;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.model.FlowNodeModel;

/**
 * 任务创建处理器。
 */
public interface FlowCreateTaskHandler {

    FlowTask handle(FlowTask task, FlowNodeModel nodeModel, FlowExecution execution);
}


