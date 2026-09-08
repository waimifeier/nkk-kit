package org.nkk.flow.core.extension.condition;

import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.core.context.FlowExecution;
import org.nkk.flow.model.FlowConditionNode;
import org.nkk.flow.model.FlowNodeModel;

import java.util.List;
import java.util.Optional;

/**
 * 条件分支处理器。
 */
public interface FlowConditionHandler {

    Optional<FlowConditionNode> getConditionNode(FlowContext context, FlowExecution execution, FlowNodeModel nodeModel);

    Optional<List<FlowConditionNode>> getInclusiveNodes(FlowContext context, FlowExecution execution, FlowNodeModel nodeModel);

    default Optional<FlowConditionNode> getRouteNode(FlowContext context, FlowExecution execution, FlowNodeModel nodeModel) {
        return getConditionNode(context, execution, nodeModel);
    }
}

