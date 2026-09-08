package org.nkk.flow.core.extension.task;

import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.core.context.FlowExecution;

/**
 * 任务创建拦截器。
 */
public interface FlowTaskCreateInterceptor {

    default void before(FlowContext context, FlowExecution execution) {
    }

    default void after(FlowContext context, FlowExecution execution) {
    }
}


