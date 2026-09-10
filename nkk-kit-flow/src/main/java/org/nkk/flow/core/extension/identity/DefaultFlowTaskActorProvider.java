package org.nkk.flow.core.extension.identity;

import org.nkk.flow.core.context.FlowExecution;
import org.nkk.flow.core.context.FlowDataTransfer;
import org.nkk.flow.entity.FlowTaskActor;
import org.nkk.flow.enums.core.FlowTaskActorEnum.ActorType;
import org.nkk.flow.enums.node.FlowNodeSetTypeEnum;
import org.nkk.flow.model.node.task.FlowDynamicAssignee;
import org.nkk.flow.model.node.task.FlowNodeAssignee;
import org.nkk.flow.model.node.task.TaskNodeModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 默认参与者提供者，仅处理模型中直接配置的参与者。
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
            actors.add(FlowTaskActor.of(assignee, actorType, nodeModel.saveWeight()));
        }
        return actors;
    }

    @Override
    public List<FlowNodeAssignee> getNodeAssignees(TaskNodeModel nodeModel, FlowExecution execution) {
        FlowDynamicAssignee dynamicAssignee = resolveDynamicAssignee(nodeModel, execution);
        if (dynamicAssignee != null && dynamicAssignee.getAssigneeList() != null) {
            return dynamicAssignee.getAssigneeList();
        }
        return nodeModel.getNodeAssigneeList();
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

