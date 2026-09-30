package org.nkk.flow.core.extension.identity;

import cn.hutool.core.util.StrUtil;
import org.nkk.flow.core.context.FlowExecution;
import org.nkk.flow.core.context.FlowDataTransfer;
import org.nkk.flow.entity.FlowTaskActor;
import org.nkk.flow.enums.core.FlowTaskActorEnum.ActorType;
import org.nkk.flow.enums.node.FlowNodeSetTypeEnum;
import org.nkk.flow.model.node.task.ApprovalNodeModel;
import org.nkk.flow.model.node.task.FlowDynamicAssignee;
import org.nkk.flow.model.node.task.FlowNodeAssignee;
import org.nkk.flow.model.node.task.TaskNodeModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 默认参与者提供者。
 *
 * <p>处理顺序：</p>
 * <ol>
 *     <li>动态参与人（FlowDataTransfer 或 getDynamicAssignee）</li>
 *     <li>主管/连续主管审批（setType=2/6）——通过 {@link FlowSupervisorResolver} 解析</li>
 *     <li>节点静态配置的 nodeAssigneeList</li>
 * </ol>
 */
public class DefaultFlowTaskActorProvider implements FlowTaskActorProvider {

    @Override
    public List<FlowTaskActor> getTaskActors(TaskNodeModel nodeModel, FlowExecution execution) {
        FlowDynamicAssignee dynamicAssignee = resolveDynamicAssignee(nodeModel, execution);
        List<FlowNodeAssignee> assignees = dynamicAssignee == null
                ? getNodeAssignees(nodeModel, execution)
                : dynamicAssignee.getAssigneeList();
        if (assignees == null || assignees.isEmpty()) {
            return null;
        }
        List<FlowTaskActor> actors = new ArrayList<>();
        Integer actorType = dynamicAssignee == null || dynamicAssignee.getType() == null
                ? getActorType(nodeModel)
                : dynamicAssignee.getType();
        for (FlowNodeAssignee assignee : assignees) {
            boolean saveWeight = nodeModel instanceof ApprovalNodeModel && ((ApprovalNodeModel) nodeModel).saveWeight();
            actors.add(FlowTaskActor.of(assignee, actorType, saveWeight));
        }
        return actors;
    }

    @Override
    public List<FlowNodeAssignee> getNodeAssignees(TaskNodeModel nodeModel, FlowExecution execution) {
        FlowDynamicAssignee dynamicAssignee = resolveDynamicAssignee(nodeModel, execution);
        if (dynamicAssignee != null && dynamicAssignee.getAssigneeList() != null) {
            return dynamicAssignee.getAssigneeList();
        }
        // 主管/连续主管审批：通过策略解析具体主管
        List<FlowNodeAssignee> supervisors = resolveSupervisors(nodeModel, execution);
        if (supervisors != null) {
            return supervisors;
        }
        return nodeModel.getNodeAssigneeList();
    }

    /**
     * 解析主管/连续主管审批节点的参与者。
     *
     * <ul>
     *     <li>setType=2（主管）：只取 examineLevel 级的主管（单人）。</li>
     *     <li>setType=6（连续主管）：从 1 到 examineLevel 级依次解析（多人）。</li>
     * </ul>
     *
     * @return 解析到的主管列表；非主管类型节点返回 null（走默认逻辑）
     */
    private List<FlowNodeAssignee> resolveSupervisors(TaskNodeModel nodeModel, FlowExecution execution) {
        Integer setType = nodeModel.getSetType();
        if (!FlowNodeSetTypeEnum.SUPERVISOR.value().equals(setType)
                && !FlowNodeSetTypeEnum.CONTINUOUS_SUPERVISOR.value().equals(setType)) {
            return null;
        }
        // 主管解析必须基于流程发起人，而非当前操作人：
        // 顺序签场景下 execution.getFlowCreator() 是当前审批人，不是发起人
        String userId = resolveInitiatorId(execution);
        if (StrUtil.isBlank(userId)) {
            return null;
        }
        FlowSupervisorResolver resolver = execution.getContext().getSupervisorResolver();
        if (resolver == null) {
            return null;
        }

        int maxLevel = resolveExamineLevel(nodeModel);

        // SUPERVISOR(2)：只取 maxLevel 级这一人
        // CONTINUOUS_SUPERVISOR(6)：从 1 到 maxLevel 级依次取
        boolean continuous = FlowNodeSetTypeEnum.CONTINUOUS_SUPERVISOR.value().equals(setType);
        int startLevel = continuous ? 1 : maxLevel;

        List<FlowNodeAssignee> result = new ArrayList<>();
        for (int level = startLevel; level <= maxLevel; level++) {
            FlowNodeAssignee supervisor = resolver.resolveSupervisor(userId, level);
            if (supervisor != null) {
                result.add(supervisor);
            }
        }
        return result;
    }

    /**
     * 解析流程发起人 ID。
     *
     * <p>优先从流程实例获取发起人（保证顺序签场景下始终用同一发起人解析主管链），
     * 实例不可用时回退到当前操作人（首次创建任务时操作人即发起人）。</p>
     */
    private String resolveInitiatorId(FlowExecution execution) {
        if (execution.getFlowInstance() != null
                && StrUtil.isNotBlank(execution.getFlowInstance().getCreateId())) {
            return execution.getFlowInstance().getCreateId();
        }
        if (execution.getFlowCreator() != null) {
            return execution.getFlowCreator().getCreateId();
        }
        return null;
    }

    /**
     * 解析审批节点的 examineLevel，默认 1。
     */
    private int resolveExamineLevel(TaskNodeModel nodeModel) {
        if (nodeModel instanceof ApprovalNodeModel) {
            Integer examineLevel = ((ApprovalNodeModel) nodeModel).getExamineLevel();
            if (examineLevel != null && examineLevel > 0) {
                return examineLevel;
            }
        }
        return 1;
    }

    private FlowDynamicAssignee resolveDynamicAssignee(TaskNodeModel nodeModel, FlowExecution execution) {
        Map<String, Object> dynamicMap = FlowDataTransfer.get(FlowDataTransfer.DYNAMIC_ASSIGNEE);
        if (dynamicMap != null && nodeModel.getNodeKey() != null && dynamicMap.containsKey(nodeModel.getNodeKey())) {
            Object value = dynamicMap.get(nodeModel.getNodeKey());
            if (value instanceof FlowDynamicAssignee) {
                return (FlowDynamicAssignee) value;
            }
            if (value instanceof List) {
                return FlowDynamicAssignee.of(null, (List<FlowNodeAssignee>) value);
            }
        }
        FlowDynamicAssignee dynamicAssignee = getDynamicAssignee(nodeModel, execution);
        if (dynamicAssignee != null && dynamicAssignee.getAssigneeList() != null) {
            return dynamicAssignee;
        }
        return null;
    }

    @Override
    public Integer getActorType(TaskNodeModel nodeModel) {
        if (FlowNodeSetTypeEnum.ROLE.value().equals(nodeModel.getSetType())) {
            return ActorType.ROLE.value();
        }
        if (FlowNodeSetTypeEnum.DEPARTMENT.value().equals(nodeModel.getSetType())) {
            return ActorType.DEPARTMENT.value();
        }
        return ActorType.USER.value();
    }
}

