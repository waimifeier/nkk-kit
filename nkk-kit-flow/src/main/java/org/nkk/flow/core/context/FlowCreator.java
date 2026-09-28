package org.nkk.flow.core.context;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import org.nkk.flow.enums.core.FlowTaskActorEnum.ActorType;

import java.io.Serializable;
import java.util.List;

/**
 * 流程操作人。
 */
@Getter
public class FlowCreator implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tenantId;

    private String createId;

    private String createBy;

    /**
     * 部门 ID，用于发起节点指定部门（setType=7）的权限校验。
     */
    private String deptId;

    /**
     * 角色 ID 列表，用于发起节点指定角色（setType=3）的权限校验。
     */
    private List<String> roleIds;

    private FlowCreator(String createId, String createBy) {
        this.createId = createId;
        this.createBy = createBy;
    }

    public static FlowCreator of(String createId, String createBy) {
        return of(null, createId, createBy);
    }

    public static FlowCreator of(String tenantId, String createId, String createBy) {
        return new FlowCreator(createId, createBy).tenantId(normalize(tenantId));
    }

    /**
     * 构造携带部门、角色信息的流程操作人，用于发起节点权限校验。
     *
     * @param tenantId 租户 ID
     * @param createId 操作人 ID
     * @param createBy 操作人名称
     * @param deptId   部门 ID
     * @param roleIds  角色 ID 列表
     * @return 流程操作人
     */
    public static FlowCreator of(String tenantId, String createId, String createBy,
                                 String deptId, List<String> roleIds) {
        return new FlowCreator(createId, createBy)
                .tenantId(normalize(tenantId))
                .deptId(normalize(deptId))
                .roleIds(roleIds);
    }

    public FlowCreator tenantId(String tenantId) {
        this.tenantId = normalize(tenantId);
        return this;
    }

    public FlowCreator deptId(String deptId) {
        this.deptId = normalize(deptId);
        return this;
    }

    public FlowCreator roleIds(List<String> roleIds) {
        this.roleIds = roleIds;
        return this;
    }

    /**
     * 判断当前流程操作人是否命中某个参与人配置。
     *
     * <p>参与人类型由 {@code actorType} 指定，兼容旧流程 JSON：{@code actorType} 为空时按用户处理。</p>
     *
     * <ul>
     *     <li>{@link ActorType#USER}：操作人 ID 等于参与人 ID。</li>
     *     <li>{@link ActorType#ROLE}：操作人角色列表包含该角色 ID。</li>
     *     <li>{@link ActorType#DEPARTMENT}：操作人部门 ID 等于参与人 ID。</li>
     * </ul>
     *
     * <p>开始节点发起权限、审批/抄送任务办理权限等组织身份匹配统一走该方法，
     * 业务系统无需再注册自定义策略 Bean。</p>
     *
     * @param actorId   参与人 ID，可能是用户 ID、角色 ID 或部门 ID
     * @param actorType 参与人类型，参见 {@link ActorType}
     * @return true 表示当前操作人命中该参与人配置
     */
    public boolean matches(String actorId, Integer actorType) {
        if (StrUtil.isBlank(actorId)) {
            return false;
        }
        if (actorType == null || ActorType.USER.value().equals(actorType)) {
            return StrUtil.equals(createId, actorId);
        }
        if (ActorType.ROLE.value().equals(actorType)) {
            return CollUtil.isNotEmpty(roleIds) && roleIds.contains(actorId);
        }
        if (ActorType.DEPARTMENT.value().equals(actorType)) {
            return StrUtil.equals(deptId, actorId);
        }
        return false;
    }

    private static String normalize(String value) {
        return StrUtil.trimToNull(value);
    }
}

