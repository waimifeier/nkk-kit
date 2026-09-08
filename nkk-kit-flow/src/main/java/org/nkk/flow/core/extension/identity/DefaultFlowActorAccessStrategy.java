package org.nkk.flow.core.extension.identity;

import cn.hutool.core.util.StrUtil;
import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.entity.FlowTaskActor;
import org.nkk.flow.enums.core.FlowTaskActorEnum.ActorType;
import org.nkk.flow.model.FlowNodeAssignee;

/**
 * 默认流程参与人访问策略。
 *
 * <p>默认只内置用户 ID 精确匹配。角色、部门、岗位等组织关系需要业务系统继承该类并覆盖
 * {@link #hasRole(String, String)}、{@link #inDepartment(String, String)}，或者直接注册自定义
 * {@link FlowActorAccessStrategy} Bean。</p>
 */
public class DefaultFlowActorAccessStrategy implements FlowActorAccessStrategy {

    @Override
    public boolean isAllowed(FlowCreator creator, FlowNodeAssignee assignee) {
        if (creator == null || assignee == null) {
            return false;
        }
        return isAllowed(creator.getCreateId(), assignee.getId(), assignee.getActorType());
    }

    @Override
    public boolean isAllowed(String userId, FlowTaskActor taskActor) {
        if (taskActor == null) {
            return false;
        }
        return isAllowed(userId, taskActor.getActorId(), taskActor.getActorType());
    }

    /**
     * 按参与人类型判断当前用户是否命中参与人配置。
     *
     * <p>兼容旧流程 JSON：当 {@code actorType} 为空时，默认按用户处理。</p>
     *
     * @param userId 当前操作人用户 ID
     * @param actorId 参与人 ID，可能是用户 ID、角色 ID 或部门 ID
     * @param actorType 参与人类型
     * @return true 表示当前用户命中该参与人配置
     */
    protected boolean isAllowed(String userId, String actorId, Integer actorType) {
        if (StrUtil.isBlank(userId) || StrUtil.isBlank(actorId)) {
            return false;
        }
        if (actorType == null || ActorType.USER.value().equals(actorType)) {
            return StrUtil.equals(userId, actorId);
        }
        if (ActorType.ROLE.value().equals(actorType)) {
            return hasRole(userId, actorId);
        }
        if (ActorType.DEPARTMENT.value().equals(actorType)) {
            return inDepartment(userId, actorId);
        }
        return false;
    }

    /**
     * 判断当前用户是否拥有指定角色。
     *
     * @param userId 当前操作人用户 ID
     * @param roleId 角色 ID
     * @return true 表示当前用户拥有该角色
     */
    protected boolean hasRole(String userId, String roleId) {
        return false;
    }

    /**
     * 判断当前用户是否属于指定部门。
     *
     * @param userId 当前操作人用户 ID
     * @param departmentId 部门 ID
     * @return true 表示当前用户属于该部门
     */
    protected boolean inDepartment(String userId, String departmentId) {
        return false;
    }
}
