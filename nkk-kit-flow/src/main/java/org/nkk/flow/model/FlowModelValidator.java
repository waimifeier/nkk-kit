package org.nkk.flow.model;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.nkk.core.enums.common.IEnum;
import org.nkk.flow.enums.node.FlowNodeSetTypeEnum;
import org.nkk.flow.enums.node.FlowNodeTypeEnum;
import org.nkk.flow.enums.core.FlowTaskEnum.PerformType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.nkk.flow.model.node.FlowNodeModel;
import org.nkk.flow.model.node.router.ConditionRouterNodeModel;
import org.nkk.flow.model.node.router.FlowCondition;
import org.nkk.flow.model.node.router.FlowConditionNode;
import org.nkk.flow.model.node.router.InclusiveRouterNodeModel;
import org.nkk.flow.model.node.router.ParallelRouterNodeModel;
import org.nkk.flow.model.node.router.RouteRouterNodeModel;
import org.nkk.flow.model.node.task.ApprovalNodeModel;
import org.nkk.flow.model.node.task.CallProcessNodeModel;
import org.nkk.flow.model.node.task.CopyNodeModel;
import org.nkk.flow.model.node.task.FlowNodeAssignee;
import org.nkk.flow.model.node.task.FlowSignPolicy;
import org.nkk.flow.model.node.task.TaskNodeModel;

/**
 * 流程模型校验工具。
 */
public final class FlowModelValidator {

    private FlowModelValidator() {
    }

    /**
     * 校验流程模型，存在错误时直接抛出异常。
     *
     * @param model 流程模型
     */
    public static void validate(FlowProcessModel model) {
        List<String> errors = check(model);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("流程模型校验失败：" + String.join("；", errors));
        }
    }

    /**
     * 检查流程模型并返回全部错误信息。
     *
     * @param model 流程模型
     * @return 校验错误列表
     */
    public static List<String> check(FlowProcessModel model) {
        List<String> errors = new ArrayList<>();
        if (model == null) {
            errors.add("流程模型不能为空");
            return errors;
        }
        if (StrUtil.isBlank(model.getKey())) {
            errors.add("流程 key 不能为空");
        }
        if (model.getNodeConfig() == null) {
            errors.add("流程根节点 nodeConfig 不能为空");
            return errors;
        }

        ValidationContext context = new ValidationContext();
        collectNode(model.getNodeConfig(), "nodeConfig", context, errors);
        validateGlobal(model, context, errors);
        return errors;
    }

    /**
     * 校验流程全局约束，例如根节点、审批节点和结束路径。
     *
     * @param model 流程模型
     * @param context 校验上下文
     * @param errors 错误列表
     */
    private static void validateGlobal(FlowProcessModel model, ValidationContext context, List<String> errors) {
        FlowNodeModel root = model.getNodeConfig();
        if (!FlowNodeTypeEnum.START.eq(root.getType())) {
            errors.add("流程根节点必须是发起节点，nodeKey=" + value(root.getNodeKey()));
        }
        if (!context.hasApprovalNode) {
            errors.add("流程至少需要配置一个审批节点");
        }
        if (!context.hasEndPath) {
            errors.add("流程缺少可结束路径，请配置结束节点、自动通过、自动拒绝或无后续节点的可结束节点");
        }
        for (Map.Entry<String, String> entry : context.routeTargets.entrySet()) {
            if (!context.nodeKeys.contains(entry.getValue())) {
                errors.add("路由目标节点不存在，routeNodeKey=" + entry.getKey() + "，targetNodeKey=" + entry.getValue());
            }
        }
    }

    /**
     * 递归收集并校验流程节点。
     *
     * @param node 当前节点
     * @param path 当前节点路径
     * @param context 校验上下文
     * @param errors 错误列表
     */
    private static void collectNode(FlowNodeModel node, String path, ValidationContext context, List<String> errors) {
        if (node == null) {
            return;
        }
        if (StrUtil.isBlank(node.getNodeKey())) {
            errors.add(path + " 节点 nodeKey 不能为空");
        } else if (!context.nodeKeys.add(node.getNodeKey())) {
            errors.add("节点 nodeKey 重复，nodeKey=" + node.getNodeKey());
        } else if (node.getNodeKey().length() > 100) {
            errors.add(path + " 节点 nodeKey 长度不能超过 100，nodeKey=" + node.getNodeKey());
        } else if (containsWhitespace(node.getNodeKey())) {
            errors.add(path + " 节点 nodeKey 不允许包含空白字符，nodeKey=" + node.getNodeKey());
        }
        validateDesignerFields(node, path, errors);
        if (node.getType() == null) {
            errors.add(path + " 节点 type 不能为空，nodeKey=" + value(node.getNodeKey()));
        } else if (IEnum.resolveKeyOfNullable(FlowNodeTypeEnum.class, node.getType()) == null) {
            errors.add(path + " 节点 type 不合法，nodeKey=" + value(node.getNodeKey()) + "，type=" + node.getType());
        }

        validateNodeByType(node, path, context, errors);
        markEndPath(node, context);
        collectRouterBranches(node, path, context, errors);
        collectNode(node.getChildNode(), path + ".childNode", context, errors);
    }

    /**
     * 按路由容器子类型收集并校验分支列表。
     *
     * @param node 当前节点
     * @param path 当前节点路径
     * @param context 校验上下文
     * @param errors 错误列表
     */
    private static void collectRouterBranches(FlowNodeModel node, String path, ValidationContext context,
                                              List<String> errors) {
        if (node instanceof ConditionRouterNodeModel) {
            collectBranches(((ConditionRouterNodeModel) node).getConditionNodes(), path + ".conditionNodes",
                    node, context, errors, BranchType.CONDITION);
        } else if (node instanceof ParallelRouterNodeModel) {
            collectBranches(((ParallelRouterNodeModel) node).getParallelNodes(), path + ".parallelNodes",
                    node, context, errors, BranchType.PARALLEL);
        } else if (node instanceof InclusiveRouterNodeModel) {
            collectBranches(((InclusiveRouterNodeModel) node).getInclusiveNodes(), path + ".inclusiveNodes",
                    node, context, errors, BranchType.INCLUSIVE);
        } else if (node instanceof RouteRouterNodeModel) {
            collectBranches(((RouteRouterNodeModel) node).getRouteNodes(), path + ".routeNodes",
                    node, context, errors, BranchType.ROUTE);
        }
    }

    /**
     * 按节点类型执行差异化校验。
     *
     * @param node 当前节点
     * @param path 当前节点路径
     * @param context 校验上下文
     * @param errors 错误列表
     */
    private static void validateNodeByType(FlowNodeModel node, String path, ValidationContext context,
                                           List<String> errors) {
        if (FlowNodeTypeEnum.APPROVAL.eq(node.getType())) {
            context.hasApprovalNode = true;
            validateApprovalNode((ApprovalNodeModel) node, path, errors);
            return;
        }
        if (FlowNodeTypeEnum.COPY.eq(node.getType())) {
            validateCopyNode((CopyNodeModel) node, path, errors);
            return;
        }
        if (node instanceof ConditionRouterNodeModel) {
            validateBranchContainer(node, ((ConditionRouterNodeModel) node).getConditionNodes(), path, "conditionNodes", errors);
            return;
        }
        if (node instanceof ParallelRouterNodeModel) {
            validateBranchContainer(node, ((ParallelRouterNodeModel) node).getParallelNodes(), path, "parallelNodes", errors);
            return;
        }
        if (node instanceof InclusiveRouterNodeModel) {
            validateBranchContainer(node, ((InclusiveRouterNodeModel) node).getInclusiveNodes(), path, "inclusiveNodes", errors);
            return;
        }
        if (node instanceof RouteRouterNodeModel) {
            List<FlowConditionNode> routeNodes = ((RouteRouterNodeModel) node).getRouteNodes();
            validateBranchContainer(node, routeNodes, path, "routeNodes", errors);
            if (node.getChildNode() != null && hasDefaultCondition(routeNodes)) {
                errors.add(path + " 路由分支配置公共后续节点时，routeNodes 不允许存在空条件默认路由，nodeKey=" + value(node.getNodeKey()));
            }
            return;
        }
        if (node instanceof CallProcessNodeModel && StrUtil.isBlank(((CallProcessNodeModel) node).getCallProcess())) {
            errors.add(path + " 子流程节点 callProcess 不能为空，nodeKey=" + value(node.getNodeKey()));
            return;
        }
        if (FlowNodeTypeEnum.END.eq(node.getType()) || FlowNodeTypeEnum.AUTO_PASS.eq(node.getType())
                || FlowNodeTypeEnum.AUTO_REJECT.eq(node.getType())) {
            if (node.getChildNode() != null) {
                errors.add(path + " 终止类节点不允许配置 childNode，nodeKey=" + value(node.getNodeKey()));
            }
        }
    }

    /**
     * 标记流程是否存在可结束路径。
     *
     * @param node 当前节点
     * @param context 校验上下文
     */
    private static void markEndPath(FlowNodeModel node, ValidationContext context) {
        if (FlowNodeTypeEnum.END.eq(node.getType()) || FlowNodeTypeEnum.AUTO_PASS.eq(node.getType())
                || FlowNodeTypeEnum.AUTO_REJECT.eq(node.getType())) {
            context.hasEndPath = true;
            return;
        }
        if (node.getChildNode() != null) {
            return;
        }
        if (FlowNodeTypeEnum.APPROVAL.eq(node.getType()) || FlowNodeTypeEnum.CALL_PROCESS.eq(node.getType())
                || FlowNodeTypeEnum.TIMER.eq(node.getType()) || FlowNodeTypeEnum.TRIGGER.eq(node.getType())) {
            context.hasEndPath = true;
        }
    }

    /**
     * 校验审批节点配置。
     *
     * @param node 审批节点
     * @param path 当前节点路径
     * @param errors 错误列表
     */
    private static void validateApprovalNode(ApprovalNodeModel node, String path, List<String> errors) {
        PerformType performType = PerformType.of(node.getExamineMode());
        if (PerformType.START == performType || PerformType.CALL_PROCESS == performType
                || PerformType.TIMER == performType || PerformType.TRIGGER == performType
                || PerformType.COPY == performType) {
            errors.add(path + " 审批节点 examineMode 不合法，nodeKey=" + value(node.getNodeKey())
                    + "，examineMode=" + node.getExamineMode());
        }
        if (PerformType.VOTE_SIGN == performType) {
            Integer extendPassWeight = node.getExtendConfig() == null
                    ? null
                    : integerValue(node.getExtendConfig().get(FlowSignPolicy.PASS_WEIGHT));
            if ((node.getPassWeight() == null || node.getPassWeight() <= 0 || node.getPassWeight() > 100)
                    && (extendPassWeight == null || extendPassWeight <= 0 || extendPassWeight > 100)) {
                errors.add(path + " 票签 passWeight 必须在 1-100 之间，nodeKey=" + value(node.getNodeKey()));
            }
        }
        validateSignPolicy(node, path, performType, errors);
        validateAssignees(node, path, "审批节点", errors);
    }

    /**
     * 校验抄送节点配置。
     *
     * @param node 抄送节点
     * @param path 当前节点路径
     * @param errors 错误列表
     */
    private static void validateCopyNode(CopyNodeModel node, String path, List<String> errors) {
        validateAssignees(node, path, "抄送节点", errors);
    }

    /**
     * 校验节点参与人配置。
     *
     * @param node 当前节点
     * @param path 当前节点路径
     * @param nodeType 节点类型说明
     * @param errors 错误列表
     */
    private static void validateAssignees(TaskNodeModel node, String path, String nodeType, List<String> errors) {
        if (requiresAssignee(node) && CollUtil.isEmpty(node.getNodeAssigneeList())) {
            errors.add(path + " " + nodeType + " 未配置参与人，nodeKey=" + value(node.getNodeKey())
                    + "，setType=" + node.getSetType());
        }
        if (node.getNodeAssigneeList() == null) {
            return;
        }
        Set<String> assigneeIds = new HashSet<>();
        for (int i = 0; i < node.getNodeAssigneeList().size(); i++) {
            FlowNodeAssignee assignee = node.getNodeAssigneeList().get(i);
            String assigneePath = path + ".nodeAssigneeList[" + i + "]";
            if (assignee == null) {
                errors.add(assigneePath + " 参与人不能为空");
                continue;
            }
            if (StrUtil.isBlank(assignee.getId())) {
                errors.add(assigneePath + " 参与人 id 不能为空");
            } else if (!assigneeIds.add(assignee.getId())) {
                errors.add(assigneePath + " 参与人 id 重复，id=" + assignee.getId());
            }
            if (assignee.getWeight() != null && (assignee.getWeight() < 0 || assignee.getWeight() > 100)) {
                errors.add(assigneePath + " 票权 weight 必须在 0-100 之间，id=" + value(assignee.getId()));
            }
        }
    }

    /**
     * 判断当前节点配置是否要求显式参与人列表。
     *
     * @param node 当前节点
     * @return true 表示必须配置参与人
     */
    private static boolean requiresAssignee(TaskNodeModel node) {
        return FlowNodeSetTypeEnum.SPECIFY_MEMBERS.value().equals(node.getSetType())
                || FlowNodeSetTypeEnum.ROLE.value().equals(node.getSetType())
                || FlowNodeSetTypeEnum.DEPARTMENT.value().equals(node.getSetType())
                || FlowNodeSetTypeEnum.CANDIDATE.value().equals(node.getSetType());
    }

    /**
     * 校验分支容器节点是否配置了分支列表。
     *
     * @param node 分支容器节点
     * @param branchNodes 分支节点列表
     * @param path 当前节点路径
     * @param field 分支字段名
     * @param errors 错误列表
     */
    private static void validateBranchContainer(FlowNodeModel node, List<FlowConditionNode> branchNodes, String path,
                                                String field, List<String> errors) {
        if (CollUtil.isEmpty(branchNodes)) {
            errors.add(path + " 分支节点 " + field + " 不能为空，nodeKey=" + value(node.getNodeKey()));
        }
    }

    /**
     * 收集并校验分支节点。
     *
     * @param nodes 分支节点列表
     * @param path 当前分支路径
     * @param owner 分支所属节点
     * @param context 校验上下文
     * @param errors 错误列表
     * @param branchType 分支类型
     */
    private static void collectBranches(List<FlowConditionNode> nodes, String path, FlowNodeModel owner,
                                        ValidationContext context, List<String> errors, BranchType branchType) {
        if (nodes == null) {
            return;
        }
        Set<String> branchKeys = new HashSet<>();
        Set<Integer> priorityLevels = new HashSet<>();
        int defaultConditionCount = 0;
        int emptyChildCount = 0;
        for (int i = 0; i < nodes.size(); i++) {
            FlowConditionNode node = nodes.get(i);
            String currentPath = path + "[" + i + "]";
            if (node == null) {
                errors.add(currentPath + " 分支不能为空");
                continue;
            }
            if (StrUtil.isBlank(node.getNodeKey())) {
                errors.add(currentPath + " 分支 nodeKey 不能为空");
            } else if (!branchKeys.add(node.getNodeKey())) {
                errors.add(currentPath + " 分支 nodeKey 重复，nodeKey=" + node.getNodeKey());
            } else if (containsWhitespace(node.getNodeKey())) {
                errors.add(currentPath + " 分支 nodeKey 不允许包含空白字符，nodeKey=" + node.getNodeKey());
            }
            if (node.getPriorityLevel() != null && !priorityLevels.add(node.getPriorityLevel())) {
                errors.add(currentPath + " 分支 priorityLevel 重复，nodeKey=" + value(node.getNodeKey())
                        + "，priorityLevel=" + node.getPriorityLevel());
            }
            validateConditionNode(node, currentPath, errors, branchType);
            if (CollUtil.isEmpty(node.getConditionList())) {
                defaultConditionCount++;
            }
            if (node.getChildNode() == null) {
                emptyChildCount++;
            }
            if (branchType == BranchType.ROUTE) {
                registerRouteTarget(node, owner, context, errors, currentPath);
            }
            if (node.getChildNode() != null) {
                collectNode(node.getChildNode(), currentPath + ".childNode", context, errors);
            }
        }
        if (branchType != BranchType.PARALLEL && defaultConditionCount > 1) {
            errors.add(path + " 只能配置一个空条件默认分支，nodeKey=" + value(owner.getNodeKey()));
        }
        if (branchType != BranchType.ROUTE && owner.getChildNode() == null && emptyChildCount > 1) {
            errors.add(path + " 无公共后续节点时，最多只能有一个空 childNode 分支，nodeKey=" + value(owner.getNodeKey()));
        }
    }

    /**
     * 校验单个分支节点的条件配置。
     *
     * @param node 分支节点
     * @param path 当前分支路径
     * @param errors 错误列表
     * @param branchType 分支类型
     */
    private static void validateConditionNode(FlowConditionNode node, String path, List<String> errors,
                                              BranchType branchType) {
        if (node.getPriorityLevel() == null) {
            errors.add(path + " priorityLevel 不能为空，nodeKey=" + value(node.getNodeKey()));
        }
        if (node.getConditionList() == null) {
            return;
        }
        if (branchType == BranchType.PARALLEL && CollUtil.isNotEmpty(node.getConditionList())) {
            errors.add(path + " 并行分支不需要配置条件表达式");
        }
        for (int g = 0; g < node.getConditionList().size(); g++) {
            List<FlowCondition> group = node.getConditionList().get(g);
            String groupPath = path + ".conditionList[" + g + "]";
            if (group == null) {
                errors.add(groupPath + " 条件组不能为空");
                continue;
            }
            for (int i = 0; i < group.size(); i++) {
                FlowCondition condition = group.get(i);
                String conditionPath = groupPath + "[" + i + "]";
                if (condition == null) {
                    errors.add(conditionPath + " 条件不能为空");
                    continue;
                }
                if (StrUtil.isBlank(condition.getField())) {
                    errors.add(conditionPath + " field 不能为空");
                }
                if (StrUtil.isBlank(condition.getOperator())) {
                    errors.add(conditionPath + " operator 不能为空");
                } else if (!isSupportedOperator(condition.getOperator())) {
                    errors.add(conditionPath + " operator 不支持，operator=" + condition.getOperator());
                }
            }
        }
    }

    /**
     * 注册并校验路由分支的目标节点。
     *
     * @param route 路由分支
     * @param owner 路由分支所属节点
     * @param context 校验上下文
     * @param errors 错误列表
     * @param path 当前分支路径
     */
    private static void registerRouteTarget(FlowConditionNode route, FlowNodeModel owner, ValidationContext context,
                                            List<String> errors, String path) {
        String targetNodeKey = routeTargetNodeKey(route.getNodeKey());
        if (StrUtil.isBlank(targetNodeKey)) {
            errors.add(path + " 路由目标 nodeKey 不能为空");
            return;
        }
        if (route.getChildNode() != null) {
            return;
        }
        if (targetNodeKey.equals(owner.getNodeKey())) {
            errors.add(path + " 路由目标不能指向自身，nodeKey=" + value(owner.getNodeKey()));
            return;
        }
        context.routeTargets.put(route.getNodeKey(), targetNodeKey);
    }

    /**
     * 判断分支列表中是否存在默认分支。
     *
     * @param nodes 分支节点列表
     * @return true 表示存在默认分支
     */
    private static boolean hasDefaultCondition(List<FlowConditionNode> nodes) {
        for (FlowConditionNode node : nodes) {
            if (node != null && CollUtil.isEmpty(node.getConditionList())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 校验设计器基础字段长度和必填项。
     *
     * @param node 当前节点
     * @param path 当前节点路径
     * @param errors 错误列表
     */
    private static void validateDesignerFields(FlowNodeModel node, String path, List<String> errors) {
        if (StrUtil.isBlank(node.getNodeName())) {
            errors.add(path + " 节点 nodeName 不能为空，nodeKey=" + value(node.getNodeKey()));
        } else if (node.getNodeName().length() > 100) {
            errors.add(path + " 节点 nodeName 长度不能超过 100，nodeKey=" + value(node.getNodeKey()));
        }
        if (node instanceof TaskNodeModel) {
            String actionUrl = ((TaskNodeModel) node).getActionUrl();
            if (actionUrl != null && actionUrl.length() > 200) {
                errors.add(path + " actionUrl 长度不能超过 200，nodeKey=" + value(node.getNodeKey()));
            }
        }
        if (node instanceof CallProcessNodeModel) {
            String callProcess = ((CallProcessNodeModel) node).getCallProcess();
            if (callProcess != null && callProcess.length() > 200) {
                errors.add(path + " callProcess 长度不能超过 200，nodeKey=" + value(node.getNodeKey()));
            }
        }
    }

    /**
     * 校验多人审批收口策略配置。
     *
     * @param node 当前节点
     * @param path 当前节点路径
     * @param performType 审批参与方式
     * @param errors 错误列表
     */
    private static void validateSignPolicy(FlowNodeModel node, String path, PerformType performType,
                                           List<String> errors) {
        Map<String, Object> config = node.getExtendConfig();
        if (CollUtil.isEmpty(config)) {
            return;
        }
        validatePercent(config, FlowSignPolicy.PASS_WEIGHT, path, errors);
        validatePercent(config, FlowSignPolicy.REJECT_WEIGHT, path, errors);
        validatePercent(config, FlowSignPolicy.VETO_WEIGHT, path, errors);
        validateBoolean(config, FlowSignPolicy.ALLOW_ABSTAIN, path, errors);
        validateBoolean(config, FlowSignPolicy.ABSTAIN_AS_PASS, path, errors);
        if ((config.containsKey(FlowSignPolicy.REJECT_WEIGHT)
                || config.containsKey(FlowSignPolicy.VETO_WEIGHT)
                || config.containsKey(FlowSignPolicy.ALLOW_ABSTAIN)
                || config.containsKey(FlowSignPolicy.ABSTAIN_AS_PASS))
                && performType != PerformType.COUNTERSIGN
                && performType != PerformType.OR_SIGN
                && performType != PerformType.VOTE_SIGN) {
            errors.add(path + " 多人审批策略只支持会签、或签、票签节点，nodeKey=" + value(node.getNodeKey()));
        }
    }

    /**
     * 校验扩展配置中的百分比字段。
     *
     * @param config 扩展配置
     * @param key 配置 key
     * @param path 当前节点路径
     * @param errors 错误列表
     */
    private static void validatePercent(Map<String, Object> config, String key, String path, List<String> errors) {
        if (!config.containsKey(key)) {
            return;
        }
        Integer value = integerValue(config.get(key));
        if (value == null || value <= 0 || value > 100) {
            errors.add(path + " extendConfig." + key + " 必须在 1-100 之间");
        }
    }

    /**
     * 校验扩展配置中的布尔字段。
     *
     * @param config 扩展配置
     * @param key 配置 key
     * @param path 当前节点路径
     * @param errors 错误列表
     */
    private static void validateBoolean(Map<String, Object> config, String key, String path, List<String> errors) {
        if (!config.containsKey(key)) {
            return;
        }
        Object value = config.get(key);
        if (value instanceof Boolean) {
            return;
        }
        if (value instanceof String && (StrUtil.equalsIgnoreCase("true", StrUtil.trim((String) value))
                || StrUtil.equalsIgnoreCase("false", StrUtil.trim((String) value)))) {
            return;
        }
        errors.add(path + " extendConfig." + key + " 必须是 boolean 类型");
    }

    /**
     * 将对象转换为整数。
     *
     * @param value 待转换值
     * @return 整数值，无法转换时返回 null
     */
    private static Integer integerValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.valueOf(StrUtil.trim((String) value));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    /**
     * 判断条件操作符是否被默认表达式支持。
     *
     * @param operator 操作符
     * @return true 表示支持
     */
    private static boolean isSupportedOperator(String operator) {
        return "eq".equalsIgnoreCase(operator) || "=".equals(operator)
                || "ne".equalsIgnoreCase(operator) || "!=".equals(operator)
                || "gt".equalsIgnoreCase(operator) || ">".equals(operator)
                || "ge".equalsIgnoreCase(operator) || ">=".equals(operator)
                || "lt".equalsIgnoreCase(operator) || "<".equals(operator)
                || "le".equalsIgnoreCase(operator) || "<=".equals(operator)
                || "contains".equalsIgnoreCase(operator)
                || "notContains".equalsIgnoreCase(operator);
    }

    /**
     * 从路由分支编码中解析目标节点编码。
     *
     * @param nodeKey 路由分支编码
     * @return 目标节点编码
     */
    private static String routeTargetNodeKey(String nodeKey) {
        if (nodeKey == null) {
            return null;
        }
        return nodeKey.startsWith("route:") ? nodeKey.substring("route:".length()) : nodeKey;
    }

    /**
     * 判断列表是否为空。
     *
     * @param values 列表
     * @return true 表示为空
     */
    private static String value(String value) {
        return value == null ? "" : value;
    }

    /**
     * 判断字符串是否包含空白字符。
     *
     * @param value 字符串
     * @return true 表示包含空白字符
     */
    private static boolean containsWhitespace(String value) {
        if (value == null) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (Character.isWhitespace(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 分支类型。
     */
    private enum BranchType {
        /**
         * 条件分支。
         */
        CONDITION,

        /**
         * 并行分支。
         */
        PARALLEL,

        /**
         * 包容分支。
         */
        INCLUSIVE,

        /**
         * 路由分支。
         */
        ROUTE
    }

    /**
     * 流程模型校验上下文。
     */
    private static class ValidationContext {

        /**
         * 已收集的节点编码集合。
         */
        private final Set<String> nodeKeys = new HashSet<>();

        /**
         * 路由分支编码和目标节点编码映射。
         */
        private final Map<String, String> routeTargets = new LinkedHashMap<>();

        /**
         * 是否已经存在审批节点。
         */
        private boolean hasApprovalNode;

        /**
         * 是否已经存在可结束路径。
         */
        private boolean hasEndPath;
    }
}

