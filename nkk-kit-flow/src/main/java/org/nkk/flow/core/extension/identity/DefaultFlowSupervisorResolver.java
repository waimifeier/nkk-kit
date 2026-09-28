package org.nkk.flow.core.extension.identity;

import org.nkk.flow.model.node.task.FlowNodeAssignee;

/**
 * 默认主管解析策略。
 *
 * <p>默认实现始终返回 {@code null}，表示不解析主管。
 * 宿主项目需要注册自定义 {@link FlowSupervisorResolver} Bean，
 * 在实现中查询业务组织关系并返回具体主管信息。</p>
 */
public class DefaultFlowSupervisorResolver implements FlowSupervisorResolver {

    @Override
    public FlowNodeAssignee resolveSupervisor(String userId, int level) {
        return null;
    }
}
