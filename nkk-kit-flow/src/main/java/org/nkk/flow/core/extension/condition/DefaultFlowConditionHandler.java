package org.nkk.flow.core.extension.condition;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.core.context.FlowDataTransfer;
import org.nkk.flow.core.context.FlowExecution;
import org.nkk.flow.core.extension.ai.FlowAiHandler;
import org.nkk.flow.model.FlowConditionNode;
import org.nkk.flow.model.FlowNodeModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 默认条件分支处理器。
 */
public class DefaultFlowConditionHandler implements FlowConditionHandler {

    @Override
    public Optional<FlowConditionNode> getConditionNode(FlowContext context, FlowExecution execution, FlowNodeModel nodeModel) {
        return getConditionNode(context, execution, nodeModel, nodeModel.getConditionNodes());
    }

    @Override
    public Optional<FlowConditionNode> getRouteNode(FlowContext context, FlowExecution execution, FlowNodeModel nodeModel) {
        return getConditionNode(context, execution, nodeModel, nodeModel.getRouteNodes());
    }

    private Optional<FlowConditionNode> getConditionNode(FlowContext context, FlowExecution execution,
                                                        FlowNodeModel nodeModel, List<FlowConditionNode> nodes) {
        if (CollUtil.isEmpty(nodes)) {
            return Optional.empty();
        }
        String specifiedNodeKey = FlowDataTransfer.get(FlowDataTransfer.CONDITION_NODE_KEY);
        if (StrUtil.isNotBlank(specifiedNodeKey)) {
            FlowDataTransfer.removeByKey(FlowDataTransfer.CONDITION_NODE_KEY);
            return nodes.stream()
                    .filter(node -> specifiedNodeKey.equals(node.getNodeKey()))
                    .findFirst();
        }
        FlowAiHandler flowAiHandler = context.getFlowAiHandler();
        if (flowAiHandler != null && StrUtil.isNotBlank(nodeModel.getCallAi())) {
            String nodeKey = flowAiHandler.decideRoute(context, execution, nodeModel, args(execution));
            if (StrUtil.isNotBlank(nodeKey)) {
                Optional<FlowConditionNode> aiNode = nodes.stream()
                        .filter(node -> Objects.equals(nodeKey, node.getNodeKey()))
                        .findFirst();
                if (aiNode.isPresent()) {
                    return aiNode;
                }
            }
        }
        return nodes.stream()
                .sorted(Comparator.comparing(node -> node.getPriorityLevel() == null ? 0 : node.getPriorityLevel()))
                .filter(node -> context.getExpression().eval(node.getConditionList(), execution.getArgs()))
                .findFirst()
                .map(Optional::of)
                .orElseGet(() -> nodes.stream().filter(node -> CollUtil.isEmpty(node.getConditionList())).findFirst());
    }

    @Override
    public Optional<List<FlowConditionNode>> getInclusiveNodes(FlowContext context, FlowExecution execution, FlowNodeModel nodeModel) {
        List<FlowConditionNode> nodes = nodeModel.getInclusiveNodes();
        if (CollUtil.isEmpty(nodes)) {
            return Optional.empty();
        }
        List<FlowConditionNode> matched = new ArrayList<>();
        FlowAiHandler flowAiHandler = context.getFlowAiHandler();
        if (flowAiHandler != null && StrUtil.isNotBlank(nodeModel.getCallAi())) {
            List<String> nodeKeys = flowAiHandler.decideInclusiveRoutes(context, execution, nodeModel, args(execution));
            if (CollUtil.isNotEmpty(nodeKeys)) {
                matched = nodes.stream()
                        .filter(node -> nodeKeys.contains(node.getNodeKey()))
                        .collect(Collectors.toList());
                if (CollUtil.isNotEmpty(matched)) {
                    return Optional.of(matched);
                }
            }
        }
        for (FlowConditionNode node : nodes) {
            if (context.getExpression().eval(node.getConditionList(), execution.getArgs())) {
                matched.add(node);
            }
        }
        if (matched.isEmpty()) {
            for (FlowConditionNode node : nodes) {
                if (CollUtil.isEmpty(node.getConditionList())) {
                    matched.add(node);
                    break;
                }
            }
        }
        return matched.isEmpty() ? Optional.empty() : Optional.of(matched);
    }

    private Map<String, Object> args(FlowExecution execution) {
        return execution.getArgs() == null ? java.util.Collections.emptyMap() : execution.getArgs();
    }
}

