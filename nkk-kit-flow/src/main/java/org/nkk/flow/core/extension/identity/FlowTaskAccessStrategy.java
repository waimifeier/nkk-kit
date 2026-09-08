package org.nkk.flow.core.extension.identity;

import org.nkk.flow.entity.FlowTaskActor;

import java.util.List;

/**
 * 任务访问策略。
 */
public interface FlowTaskAccessStrategy {

    /**
     * 判断指定用户是否允许处理当前任务。
     *
     * <p>核心引擎在审批、驳回、转办、委派、加签、减签、已阅等需要校验操作人的入口调用该方法。
     * 默认实现只按 {@code userId == FlowTaskActor.actorId} 精确匹配。</p>
     *
     * <p>如果任务参与者是角色或部门，使用方应覆盖该策略，根据自己的组织关系判断
     * {@code userId} 是否属于任务参与者中的某个角色、部门、岗位或候选范围。判断通过时返回匹配到的
     * {@link FlowTaskActor}，引擎会把它作为本次授权命中的参与者快照。</p>
     *
     * @param userId 当前操作人用户 ID
     * @param taskActors 当前任务的参与者快照列表，可能包含用户、角色、部门等类型
     * @return 有权限时返回命中的任务参与者；无权限时返回 {@code null}
     */
    FlowTaskActor isAllowed(String userId, List<FlowTaskActor> taskActors);
}

