package org.nkk.flow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Data;
import lombok.ToString;
import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.core.context.FlowExecution;
import org.nkk.flow.core.extension.listener.FlowNodeEvent;
import org.nkk.flow.enums.core.FlowInstanceEnum.InstanceState;
import org.nkk.flow.enums.node.FlowNodeTypeEnum;
import org.nkk.flow.enums.node.FlowNodeSetTypeEnum;
import org.nkk.flow.enums.core.FlowTaskEnum.PerformType;
import org.nkk.flow.enums.node.FlowRejectStrategyEnum;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 流程节点模型。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlowNodeModel implements Serializable {

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
     * 节点类型，取值见 {@link FlowNodeTypeEnum}。
     */
    private Integer type;

    /**
     * 审批人设置类型，取值见 {@link FlowNodeSetTypeEnum}。
     */
    private Integer setType;

    /**
     * 审批参与方式，取值见 {@link PerformType}。
     */
    private Integer examineMode;

    /**
     * 分支汇聚策略，1 表示所有分支汇聚后再继续流转。
     */
    private Integer groupStrategy;

    /**
     * 票签通过权重，票签场景下达到该权重后视为通过。
     */
    private Integer passWeight;

    /**
     * 驳回策略，取值见 {@link FlowRejectStrategyEnum}。
     */
    private Integer rejectStrategy;

    /**
     * 是否允许驳回到发起节点，1 表示允许。
     */
    private Integer rejectStart;

    /**
     * 是否开启任务到期自动处理。
     */
    private Boolean termAuto;

    /**
     * 任务期限，单位小时。
     */
    private Integer term;

    /**
     * 任务到期处理模式，1 表示超时自动拒绝，其他值按自动通过处理。
     */
    private Integer termMode;

    /**
     * 是否开启任务提醒。
     */
    private Boolean remind;

    /**
     * 是否允许审批转交。
     *
     * <p>false 表示当前节点禁止调用转办、委派、代理等转交类操作。</p>
     */
    private Boolean allowTransfer;

    /**
     * 是否允许审批加签或减签。
     *
     * <p>false 表示当前节点禁止调用加签、减签操作。</p>
     */
    private Boolean allowAppendNode;

    /**
     * 是否允许审批回退。
     *
     * <p>false 表示当前节点禁止选择目标节点回退。</p>
     */
    private Boolean allowRollback;

    /**
     * 是否允许审批节点手动抄送。
     *
     * <p>false 表示当前节点禁止调用手动抄送操作。</p>
     */
    private Boolean allowCc;

    /**
     * 触发器类型，预留给业务侧或扩展点识别触发方式。
     */
    private Integer triggerType;

    /**
     * 子流程引用。默认支持 processKey、processKey:version、processId 三种写法。
     */
    private String callProcess;

    /**
     * 子流程是否异步执行，true 表示启动子流程后父流程继续向下流转。
     */
    private Boolean callAsync;

    /**
     * AI 处理器编码，配置后会交给流程 AI 扩展点处理。
     */
    private String callAi;

    /**
     * 节点任务办理页地址。
     */
    private String actionUrl;

    /**
     * 节点办理人配置。
     */
    private List<FlowNodeAssignee> nodeAssigneeList;

    /**
     * 条件分支列表。
     */
    private List<FlowConditionNode> conditionNodes;

    /**
     * 并行分支列表。
     */
    private List<FlowConditionNode> parallelNodes;

    /**
     * 包容分支列表。
     */
    private List<FlowConditionNode> inclusiveNodes;

    /**
     * 路由分支列表。
     */
    private List<FlowConditionNode> routeNodes;

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
        if (FlowNodeTypeEnum.CONDITION_APPROVAL.eq(type) || FlowNodeTypeEnum.CONDITION_BRANCH.eq(type)) {
            return context.getConditionHandler().getConditionNode(context, execution, this)
                    .map(node -> executeConditionNode(context, execution, node))
                    .orElse(false);
        }
        if (FlowNodeTypeEnum.PARALLEL_BRANCH.eq(type)) {
            return executeParallelNode(context, execution);
        }
        if (FlowNodeTypeEnum.INCLUSIVE_BRANCH.eq(type)) {
            return executeInclusiveNode(context, execution);
        }
        if (FlowNodeTypeEnum.ROUTE_BRANCH.eq(type)) {
            return executeRouteNode(context, execution);
        }
        if (FlowNodeTypeEnum.START.eq(type) || FlowNodeTypeEnum.COPY.eq(type)) {
            context.createTask(execution, this);
            return nextNode().map(next -> next.execute(context, execution)).orElse(true);
        }
        if (FlowNodeTypeEnum.TIMER.eq(type) || FlowNodeTypeEnum.TRIGGER.eq(type)) {
            context.createTask(execution, this);
            return true;
        }
        if (FlowNodeTypeEnum.CALL_PROCESS.eq(type)) {
            if (context.getSubProcessHandler() == null) {
                throw new IllegalStateException("未配置子流程处理器，nodeKey=" + nodeKey);
            }
            boolean started = context.getSubProcessHandler().start(context, execution, this);
            if (Boolean.TRUE.equals(callAsync)) {
                return nextNode().map(next -> next.execute(context, execution)).orElse(started);
            }
            return started;
        }
        if (FlowNodeTypeEnum.APPROVAL.eq(type)) {
            context.createTask(execution, this);
            return true;
        }
        if (FlowNodeTypeEnum.AUTO_PASS.eq(type)) {
            return execution.endInstance(this, InstanceState.AUTO_PASS);
        }
        if (FlowNodeTypeEnum.AUTO_REJECT.eq(type)) {
            return execution.endInstance(this, InstanceState.AUTO_REJECT);
        }
        if (FlowNodeTypeEnum.END.eq(type)) {
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
        FlowNodeModel found = findFromConditionNodes(nodeKey, conditionNodes);
        if (found != null) {
            return found;
        }
        found = findFromConditionNodes(nodeKey, parallelNodes);
        if (found != null) {
            return found;
        }
        found = findFromConditionNodes(nodeKey, inclusiveNodes);
        if (found != null) {
            return found;
        }
        found = findFromConditionNodes(nodeKey, routeNodes);
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
        if (removeFromConditionNodes(targetNodeKey, conditionNodes)) {
            return true;
        }
        if (removeFromConditionNodes(targetNodeKey, parallelNodes)) {
            return true;
        }
        if (removeFromConditionNodes(targetNodeKey, inclusiveNodes)) {
            return true;
        }
        if (removeFromConditionNodes(targetNodeKey, routeNodes)) {
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
     * 判断当前节点是否需要等待所有分支汇聚。
     *
     * @return true 表示所有分支汇聚后再继续流转
     */
    public boolean allJoinGroupStrategy() {
        return Integer.valueOf(1).equals(groupStrategy);
    }

    /**
     * 判断当前节点是否需要保存办理人权重。
     *
     * @return true 表示当前节点是票签模式，需要保存权重
     */
    public boolean saveWeight() {
        return Integer.valueOf(4).equals(examineMode);
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

    private FlowNodeModel findFromConditionNodes(String nodeKey, List<FlowConditionNode> nodes) {
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

    private boolean removeFromConditionNodes(String targetNodeKey, List<FlowConditionNode> nodes) {
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

