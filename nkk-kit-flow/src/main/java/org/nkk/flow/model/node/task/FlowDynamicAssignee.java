package org.nkk.flow.model.node.task;

import lombok.Data;
import org.nkk.flow.enums.core.FlowTaskActorEnum.ActorType;

import java.io.Serializable;
import java.util.List;

/**
 * 动态节点办理人。
 */
@Data
public class FlowDynamicAssignee implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 参与者类型，取值见 {@link ActorType}。
     */
    private Integer type;

    /**
     * 动态解析出的参与者列表。
     */
    private List<FlowNodeAssignee> assigneeList;

    /**
     * 创建动态节点办理人。
     *
     * @param type 参与者类型，取值见 {@link ActorType}
     * @param assigneeList 参与者列表
     * @return 动态节点办理人
     */
    public static FlowDynamicAssignee of(Integer type, List<FlowNodeAssignee> assigneeList) {
        FlowDynamicAssignee assignee = new FlowDynamicAssignee();
        assignee.setType(type);
        assignee.setAssigneeList(assigneeList);
        return assignee;
    }

    /**
     * 创建用户类型动态办理人。
     *
     * @param assigneeList 用户列表
     * @return 动态节点办理人
     */
    public static FlowDynamicAssignee userList(List<FlowNodeAssignee> assigneeList) {
        return of(0, assigneeList);
    }

    /**
     * 创建角色类型动态办理人。
     *
     * @param assigneeList 角色列表
     * @return 动态节点办理人
     */
    public static FlowDynamicAssignee roleList(List<FlowNodeAssignee> assigneeList) {
        return of(1, assigneeList);
    }

    /**
     * 创建部门类型动态办理人。
     *
     * @param assigneeList 部门列表
     * @return 动态节点办理人
     */
    public static FlowDynamicAssignee departmentList(List<FlowNodeAssignee> assigneeList) {
        return of(2, assigneeList);
    }
}

