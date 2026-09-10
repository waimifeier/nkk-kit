package org.nkk.flow.core.extension.identity;

import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.entity.FlowTaskActor;
import org.nkk.flow.enums.core.FlowTaskActorEnum.ActorType;
import org.nkk.flow.model.node.task.FlowNodeAssignee;

/**
 * 流程参与人访问策略。
 *
 * <p>该策略统一处理“当前用户是否命中某个参与人配置”的判断，参与人配置统一通过
 * {@code actorType} 区分类型：</p>
 *
 * <ul>
 *     <li>{@link ActorType#USER}：指定用户。</li>
 *     <li>{@link ActorType#ROLE}：指定角色。</li>
 *     <li>{@link ActorType#DEPARTMENT}：指定部门。</li>
 * </ul>
 *
 * <p>开始节点的 {@link FlowNodeAssignee} 和审批任务的 {@link FlowTaskActor} 都会走这个策略。
 * 后续如果其他节点也需要按用户、角色、部门判断权限，应优先复用该策略，不再单独定义一套判断逻辑。</p>
 *
 * <p>核心模块只知道参与人 ID 和参与人类型，不知道真实的用户角色、用户部门关系。
 * 使用方应注册自己的 {@link FlowActorAccessStrategy} Bean，在实现中查询业务系统组织关系。</p>
 */
public interface FlowActorAccessStrategy {

    /**
     * 判断当前流程操作人是否命中节点参与人配置。
     *
     * <p>主要用于开始节点发起权限判断。节点的 {@code nodeAssigneeList} 会逐个传入该方法，
     * 任意一个返回 {@code true} 即表示当前用户有权发起。</p>
     *
     * @param creator 当前流程操作人
     * @param assignee 节点参与人配置，可能表示用户、角色或部门
     * @return true 表示当前用户命中该参与人配置
     */
    boolean isAllowed(FlowCreator creator, FlowNodeAssignee assignee);

    /**
     * 判断当前用户是否命中任务参与者快照。
     *
     * <p>主要用于审批、驳回、转办、委派、已阅等任务办理权限判断。任务参与者来自
     * {@code flow_task_actor}，通常由节点 {@code nodeAssigneeList} 转换而来。</p>
     *
     * @param userId 当前操作人用户 ID
     * @param taskActor 任务参与者快照，可能表示用户、角色或部门
     * @return true 表示当前用户命中该任务参与者
     */
    boolean isAllowed(String userId, FlowTaskActor taskActor);
}
