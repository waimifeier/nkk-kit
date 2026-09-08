package org.nkk.flow.core.extension.schedule;

import org.nkk.flow.entity.FlowTask;

/**
 * 任务提醒扩展。
 */
public interface FlowTaskReminder {

    void remind(FlowTask task);
}

