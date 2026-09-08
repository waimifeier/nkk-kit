package org.nkk.flow.core.extension.identity;

import cn.hutool.core.collection.CollUtil;
import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.model.FlowNodeAssignee;

import java.util.List;

/**
 * 默认流程发起权限策略。
 *
 * <p>该类只负责开始节点 {@code nodeAssigneeList} 的遍历，实际用户、角色、部门判断委托给
 * {@link FlowActorAccessStrategy}。业务系统通常只需要注册自定义 {@link FlowActorAccessStrategy} Bean，
 * 不需要再单独覆盖本类。</p>
 */
public class DefaultFlowStartAccessStrategy implements FlowStartAccessStrategy {

    private final FlowActorAccessStrategy actorAccessStrategy;

    public DefaultFlowStartAccessStrategy() {
        this(new DefaultFlowActorAccessStrategy());
    }

    public DefaultFlowStartAccessStrategy(FlowActorAccessStrategy actorAccessStrategy) {
        this.actorAccessStrategy = actorAccessStrategy == null
                ? new DefaultFlowActorAccessStrategy()
                : actorAccessStrategy;
    }

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
            if (actorAccessStrategy.isAllowed(creator, assignee)) {
                return true;
            }
        }
        return false;
    }
}
