package org.nkk.flow.core.extension.listener;

import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.entity.FlowHisInstance;
import org.nkk.flow.enums.runtime.FlowEventTypeEnum;
import org.nkk.flow.model.node.FlowNodeModel;

/**
 * 实例事件监听器。
 */
public interface FlowInstanceListener {

    void notify(FlowEventTypeEnum eventType, FlowHisInstance instance, FlowNodeModel nodeModel, FlowCreator creator);
}

