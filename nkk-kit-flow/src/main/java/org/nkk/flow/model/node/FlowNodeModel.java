package org.nkk.flow.model.node;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.core.context.FlowExecution;
import org.nkk.flow.core.extension.listener.FlowNodeEvent;
import org.nkk.flow.enums.core.FlowInstanceEnum.InstanceState;
import org.nkk.flow.enums.node.FlowNodeTypeEnum;
import org.nkk.flow.model.FlowAiConfig;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.nkk.flow.model.node.endpoint.AutoPassNodeModel;
import org.nkk.flow.model.node.endpoint.AutoRejectNodeModel;
import org.nkk.flow.model.node.endpoint.EndNodeModel;
import org.nkk.flow.model.node.router.ConditionRouterNodeModel;
import org.nkk.flow.model.node.router.FlowConditionNode;
import org.nkk.flow.model.node.router.InclusiveRouterNodeModel;
import org.nkk.flow.model.node.router.ParallelRouterNodeModel;
import org.nkk.flow.model.node.router.RouteRouterNodeModel;
import org.nkk.flow.model.node.router.RouterNodeModel;
import org.nkk.flow.model.node.task.ApprovalNodeModel;
import org.nkk.flow.model.node.task.CallProcessNodeModel;
import org.nkk.flow.model.node.task.CopyNodeModel;
import org.nkk.flow.model.node.task.StartNodeModel;
import org.nkk.flow.model.node.task.TaskNodeModel;
import org.nkk.flow.model.node.task.TimerNodeModel;
import org.nkk.flow.model.node.task.TriggerNodeModel;

/**
 * 流程节点模型基类。
 *
 * <p>JSON 的 {@code type} 字段作为多态判别字段，反序列化时按类型实例化对应子类。
 * 子类型不在本类维护清单，而是由各子类上的 {@link FlowNodeType} 注解自我声明，
 * JSON 处理器启动时扫描 {@code org.nkk.flow.model} 包自动注册：
 * 任务节点见 {@link TaskNodeModel}（审批/抄送/发起/延迟/触发），
 * 路由容器见 {@link RouterNodeModel}（条件/并行/包容/路由），
 * 分支项见 {@link FlowConditionNode}，子流程见 {@link CallProcessNodeModel}。</p>
 *
 * <p>新增节点类型：新建继承本类（或 {@link TaskNodeModel}/{@link RouterNodeModel}）的子类，
 * 贴上 {@link FlowNodeType} 注解即可，无需修改本类。</p>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
public abstract class FlowNodeModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 节点名称，用于展示和任务名称生成。
     */
    private String nodeName;

    /**
     * 节点唯一编码，同一个流程模型内不可重复。
     */
    private String nodeKey;

    /**
     * 节点类型，取值见 {@link FlowNodeTypeEnum}，同时作为多态反序列化的判别字段。
     */
    private Integer type;

    /**
     * AI 处理器编码，配置后条件分支、包容分支的路由决策交给流程 AI 扩展点处理。
     */
    private String callAi;

    /**
     * 扩展配置，票签策略、提醒配置、AI 配置等非固定字段可放在这里。
     */
    private Map<String, Object> extendConfig;

    /**
     * 下一个顺序节点。
     */
    private FlowNodeModel childNode;

    /**
     * 父节点，仅运行时构建流程模型时使用，不参与 JSON 序列化。
     */
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private FlowNodeModel parentNode;

    /**
     * 当前节点的分支列表，仅路由容器子类有值，其他节点类型返回 null。
     *
     * @return 分支节点列表
     */
    @JsonIgnore
    public List<FlowConditionNode> getBranchNodes() {
        return null;
    }

    /**
     * 执行当前节点，并按节点类型决定是否创建任务、进入分支、启动子流程或结束实例。
     *
     * @param context 流程上下文
     * @param execution 当前执行对象
     * @return 当前节点是否执行成功
     */
    public boolean execute(FlowContext context, FlowExecution execution) {
        if (context.getNodeListener() != null) {
            context.getNodeListener().beforeExecute(FlowNodeEvent.of(context, execution, this));
        }
        try {
            return executeInternal(context, execution);
        } finally {
            if (context.getNodeListener() != null) {
                context.getNodeListener().afterExecute(FlowNodeEvent.of(context, execution, this));
            }
        }
    }

    private boolean executeInternal(FlowContext context, FlowExecution execution) {
        if (this instanceof ConditionRouterNodeModel) {
            return context.getConditionHandler().getConditionNode(context, execution, this)
                    .map(node -> executeConditionNode(context, execution, node))
                    .orElse(false);
        }
        if (this instanceof ParallelRouterNodeModel) {
            return executeParallelNode(context, execution);
        }
        if (this instanceof InclusiveRouterNodeModel) {
            return executeInclusiveNode(context, execution);
        }
        if (this instanceof RouteRouterNodeModel) {
            return executeRouteNode(context, execution);
        }
        if (this instanceof StartNodeModel || this instanceof CopyNodeModel) {
            context.createTask(execution, (TaskNodeModel) this);
            return nextNode().map(next -> next.execute(context, execution)).orElse(true);
        }
        if (this instanceof TimerNodeModel || this instanceof TriggerNodeModel || this instanceof ApprovalNodeModel) {
            context.createTask(execution, (TaskNodeModel) this);
            return true;
        }
        if (this instanceof CallProcessNodeModel) {
            CallProcessNodeModel callNode = (CallProcessNodeModel) this;
            if (context.getSubProcessHandler() == null) {
                throw new IllegalStateException("未配置子流程处理器，nodeKey=" + nodeKey);
            }
            boolean started = context.getSubProcessHandler().start(context, execution, callNode);
            if (Boolean.TRUE.equals(callNode.getCallAsync())) {
                return nextNode().map(next -> next.execute(context, execution)).orElse(started);
            }
            return started;
        }
        if (this instanceof AutoPassNodeModel) {
            return execution.endInstance(this, InstanceState.AUTO_PASS);
        }
        if (this instanceof AutoRejectNodeModel) {
            return execution.endInstance(this, InstanceState.AUTO_REJECT);
        }
        if (this instanceof EndNodeModel) {
            return execution.endInstance(this, InstanceState.COMPLETED);
        }
        return nextNode().map(next -> next.execute(context, execution)).orElseGet(() -> execution.endInstance(this, InstanceState.COMPLETED));
    }

    /**
     * 从当前节点开始递归查找指定节点编码的节点。
     *
     * @param nodeKey 节点编码
     * @return 匹配的节点，未找到时返回 null
     */
    public FlowNodeModel getNode(String nodeKey) {
        if (nodeKey == null) {
            return null;
        }
        if (nodeKey.equals(this.nodeKey)) {
            return this;
        }
        FlowNodeModel found = findFromBranchNodes(nodeKey, getBranchNodes());
        if (found != null) {
            return found;
        }
        return childNode == null ? null : childNode.getNode(nodeKey);
    }

    /**
     * 获取当前节点的下一个可执行节点。
     *
     * @return 下一个节点
     */
    public Optional<FlowNodeModel> nextNode() {
        FlowNodeModel nextNode = childNode;
        if (nextNode == null) {
            nextNode = findNextParentChild(this);
        }
        return Optional.ofNullable(nextNode);
    }

    /**
     * 从当前节点子树中移除指定节点。
     *
     * @param targetNodeKey 目标节点编码
     * @return 是否移除成功
     */
    public boolean removeNode(String targetNodeKey) {
        if (targetNodeKey == null) {
            return false;
        }
        if (childNode != null && targetNodeKey.equals(childNode.getNodeKey())) {
            childNode = childNode.getChildNode();
            if (childNode != null) {
                childNode.setParentNode(this);
            }
            return true;
        }
        if (removeFromBranchNodes(targetNodeKey, getBranchNodes())) {
            return true;
        }
        return childNode != null && childNode.removeNode(targetNodeKey);
    }

    /**
     * 在指定节点之前插入新节点。
     *
     * @param targetNodeKey 目标节点编码
     * @param newNode 待插入节点
     * @return 是否插入成功
     */
    public boolean insertBefore(String targetNodeKey, FlowNodeModel newNode) {
        if (targetNodeKey == null || newNode == null) {
            return false;
        }
        if (childNode != null && targetNodeKey.equals(childNode.getNodeKey())) {
            newNode.setParentNode(this);
            newNode.setChildNode(childNode);
            childNode.setParentNode(newNode);
            childNode = newNode;
            return true;
        }
        return childNode != null && childNode.insertBefore(targetNodeKey, newNode);
    }

    /**
     * 在指定节点之后插入新节点。
     *
     * @param targetNodeKey 目标节点编码
     * @param newNode 待插入节点
     * @return 是否插入成功
     */
    public boolean insertAfter(String targetNodeKey, FlowNodeModel newNode) {
        FlowNodeModel target = getNode(targetNodeKey);
        if (target == null || newNode == null) {
            return false;
        }
        newNode.setParentNode(target);
        newNode.setChildNode(target.getChildNode());
        if (newNode.getChildNode() != null) {
            newNode.getChildNode().setParentNode(newNode);
        }
        target.setChildNode(newNode);
        return true;
    }

    /**
     * 从扩展配置中读取 AI 配置。
     *
     * @return AI 配置，未配置时返回空配置对象
     */
    @JsonIgnore
    public FlowAiConfig getAiConfig() {
        if (extendConfig == null || !extendConfig.containsKey("aiConfig")) {
            return new FlowAiConfig();
        }
        Object value = extendConfig.get("aiConfig");
        if (value instanceof FlowAiConfig) {
            return (FlowAiConfig) value;
        }
        if (value instanceof String) {
            return FlowContext.fromJson((String) value, FlowAiConfig.class);
        }
        return FlowContext.fromJson(FlowContext.toJson(value), FlowAiConfig.class);
    }

    private FlowNodeModel findFromBranchNodes(String nodeKey, List<FlowConditionNode> nodes) {
        if (nodes == null) {
            return null;
        }
        for (FlowConditionNode node : nodes) {
            if (node.getChildNode() != null) {
                FlowNodeModel found = node.getChildNode().getNode(nodeKey);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private boolean removeFromBranchNodes(String targetNodeKey, List<FlowConditionNode> nodes) {
        if (nodes == null) {
            return false;
        }
        for (FlowConditionNode node : nodes) {
            FlowNodeModel child = node.getChildNode();
            if (child == null) {
                continue;
            }
            if (targetNodeKey.equals(child.getNodeKey())) {
                node.setChildNode(child.getChildNode());
                if (node.getChildNode() != null) {
                    node.getChildNode().setParentNode(this);
                }
                return true;
            }
            if (child.removeNode(targetNodeKey)) {
                return true;
            }
        }
        return false;
    }

    private FlowNodeModel findNextParentChild(FlowNodeModel node) {
        FlowNodeModel parent = node.getParentNode();
        while (parent != null) {
            if (parent.getChildNode() != null && parent.getChildNode() != node) {
                return parent.getChildNode();
            }
            node = parent;
            parent = parent.getParentNode();
        }
        return null;
    }

    private Boolean executeConditionNode(FlowContext context, FlowExecution execution, FlowConditionNode conditionNode) {
        if (conditionNode.getChildNode() != null) {
            return conditionNode.getChildNode().execute(context, execution);
        }
        return nextNode().map(next -> next.execute(context, execution)).orElse(true);
    }

    private boolean executeParallelNode(FlowContext context, FlowExecution execution) {
        List<FlowConditionNode> parallelNodes = ((ParallelRouterNodeModel) this).getParallelNodes();
        if (parallelNodes == null || parallelNodes.isEmpty()) {
            return nextNode().map(next -> next.execute(context, execution)).orElse(true);
        }
        for (FlowConditionNode node : parallelNodes) {
            if (node.getChildNode() != null) {
                node.getChildNode().execute(context, execution);
            }
        }
        return true;
    }

    private boolean executeInclusiveNode(FlowContext context, FlowExecution execution) {
        return context.getConditionHandler().getInclusiveNodes(context, execution, this)
                .map(nodes -> {
                    for (FlowConditionNode node : nodes) {
                        executeConditionNode(context, execution, node);
                    }
                    return true;
                })
                .orElse(false);
    }

    private boolean executeRouteNode(FlowContext context, FlowExecution execution) {
        return context.getConditionHandler().getRouteNode(context, execution, this)
                .map(node -> {
                    FlowNodeModel target = getNode(routeTargetNodeKey(node.getNodeKey()));
                    if (target == null) {
                        target = node.getChildNode();
                    }
                    if (target == null) {
                        return nextNode().map(next -> next.execute(context, execution)).orElse(true);
                    }
                    return execution.routeJump(this, target);
                })
                .orElseGet(() -> nextNode().map(next -> next.execute(context, execution)).orElse(true));
    }

    private String routeTargetNodeKey(String nodeKey) {
        if (nodeKey == null) {
            return null;
        }
        return nodeKey.startsWith("route:") ? nodeKey.substring("route:".length()) : nodeKey;
    }
}
