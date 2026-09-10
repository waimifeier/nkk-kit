package org.nkk.flow.autoconfigure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.entity.FlowTaskActor;
import org.nkk.flow.enums.runtime.FlowEventTypeEnum;
import org.nkk.flow.model.node.FlowNodeModel;

import java.util.List;

/**
 * Spring 任务事件。
 */
@Getter
@AllArgsConstructor
public class NkkFlowTaskEvent {

    private final FlowEventTypeEnum eventType;

    private final FlowTask task;

    private final List<FlowTaskActor> actors;

    private final FlowNodeModel nodeModel;

    private final FlowCreator creator;
}


