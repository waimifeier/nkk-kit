package org.nkk.flow.autoconfigure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.entity.FlowHisInstance;
import org.nkk.flow.enums.runtime.FlowEventTypeEnum;
import org.nkk.flow.model.node.FlowNodeModel;

/**
 * Spring 实例事件。
 */
@Getter
@AllArgsConstructor
public class NkkFlowInstanceEvent {

    private final FlowEventTypeEnum eventType;

    private final FlowHisInstance instance;

    private final FlowNodeModel nodeModel;

    private final FlowCreator creator;
}


