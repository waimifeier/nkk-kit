package org.nkk.flow.core.extension.identity;

import cn.hutool.core.collection.CollUtil;
import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.model.node.task.FlowNodeAssignee;

import java.util.List;

/**
 * 默认流程发起权限策略。
 *
 * <p>该类只负责开始节点 {@code nodeAssigneeList} 的遍历，实际用户、角色、部门判断通过
 * {@link FlowCreator#matches(String, Integer)} 完成。业务系统只需在构造 {@link FlowCreator}
 * 时填充 {@code deptId}、{@code roleIds}，无需再注册自定义策略 Bean。</p>
 */
public class DefaultFlowStartAccessStrategy implements FlowStartAccessStrategy {

    @Override
    public boolean isAllowed(FlowCreator creator, List<FlowNodeAssignee> assignees) {
        // 开始节点没有配置发起范围时，表示所有当前用户都可以发起。
        if (CollUtil.isEmpty(assignees)) {
            return true;
        }
        if (creator == null || creator.getCreateId() == null) {
            return false;
        }
        for (FlowNodeAssignee assignee : assignees) {
            if (assignee == null) {
                continue;
            }
            if (creator.matches(assignee.getId(), assignee.getActorType())) {
                return true;
            }
        }
        return false;
    }
}
