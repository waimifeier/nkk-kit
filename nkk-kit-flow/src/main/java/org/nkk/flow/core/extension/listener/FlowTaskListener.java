package org.nkk.flow.core.extension.listener;

import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.entity.FlowTaskActor;
import org.nkk.flow.enums.runtime.FlowEventTypeEnum;
import org.nkk.flow.model.FlowNodeModel;

import java.util.List;

/**
 * 任务事件监听器。
 */
public interface FlowTaskListener {

    void notify(FlowEventTypeEnum eventType, FlowTask task, List<FlowTaskActor> actors, FlowNodeModel nodeModel, FlowCreator creator);
}

