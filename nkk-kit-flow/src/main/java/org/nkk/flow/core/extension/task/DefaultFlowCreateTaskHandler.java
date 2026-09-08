package org.nkk.flow.core.extension.task;

import org.nkk.flow.core.context.FlowExecution;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.model.FlowNodeModel;

/**
 * 默认任务创建处理器。
 */
public class DefaultFlowCreateTaskHandler implements FlowCreateTaskHandler {

    @Override
    public FlowTask handle(FlowTask task, FlowNodeModel nodeModel, FlowExecution execution) {
        return task;
    }
}


