package org.nkk.flow.core.extension.identity;

import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.enums.core.FlowTaskActorEnum.ActorType;
import org.nkk.flow.model.FlowNodeAssignee;

import java.util.List;

/**
 * 流程发起权限策略。
 *
 * <p>该策略只负责判断“当前用户能不能发起当前流程”，不会创建流程实例，也不会解析后续审批节点。</p>
 *
 * <p>调用时机：核心引擎在发起流程实例前调用。也就是说，只要通过
 * {@code NkkFlowEngine.startInstanceById(...)} 或
 * {@code NkkFlowEngine.startInstanceByProcessKey(...)} 发起流程，都会先经过该策略。</p>
 *
 * <p>数据来源：{@code assignees} 来自流程开始节点的 {@code nodeAssigneeList}。
 * 前端设计器可以把允许发起的用户、角色、部门等范围保存到该字段。</p>
 *
 * <p>默认约定：</p>
 * <ul>
 *     <li>{@code assignees} 为空：表示不限制发起人。</li>
 *     <li>{@link ActorType#USER}：表示指定用户。</li>
 *     <li>{@link ActorType#ROLE}：表示指定角色，需要使用方根据自己的用户角色关系判断。</li>
 *     <li>{@link ActorType#DEPARTMENT}：表示指定部门，需要使用方根据自己的用户部门关系判断。</li>
 * </ul>
 *
 * <p>核心模块不内置组织表，也不知道用户和角色、部门、岗位之间的关系。
 * 如果要支持角色、部门、岗位等发起限制，请在业务系统中注册自定义 {@link FlowStartAccessStrategy} Bean。</p>
 */
public interface FlowStartAccessStrategy {

    /**
     * 判断当前操作人是否允许发起流程。
     *
     * <p>实现建议：</p>
     * <ul>
     *     <li>如果 {@code assignees} 为空，通常直接返回 {@code true}。</li>
     *     <li>如果 {@code assignee.actorType} 是用户，判断 {@code creator.createId} 是否等于 {@code assignee.id}。</li>
     *     <li>如果 {@code assignee.actorType} 是角色，查询当前用户是否拥有该角色。</li>
     *     <li>如果 {@code assignee.actorType} 是部门，查询当前用户是否属于该部门。</li>
     *     <li>如果后续扩展岗位、用户组等类型，也建议在这里统一判断。</li>
     * </ul>
     *
     * @param creator 当前流程操作人，通常来自 {@link FlowCreatorProvider#getCurrentCreator()}
     * @param assignees 开始节点配置的发起范围，通常来自 {@code FlowNodeModel.nodeAssigneeList}
     * @return true 表示允许发起；false 表示拒绝发起
     */
    boolean isAllowed(FlowCreator creator, List<FlowNodeAssignee> assignees);
}
