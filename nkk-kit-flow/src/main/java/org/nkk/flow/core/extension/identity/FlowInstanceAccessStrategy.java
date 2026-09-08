package org.nkk.flow.core.extension.identity;

import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.entity.FlowHisInstance;
import org.nkk.flow.entity.FlowInstance;
import org.nkk.flow.enums.runtime.FlowInstanceOperateEnum;

/**
 * 流程实例操作权限策略。
 *
 * <p>该策略负责判断“当前操作人能不能对某个流程实例执行管理类操作”。它和
 * {@link FlowStartAccessStrategy}、{@link FlowTaskAccessStrategy} 的职责不同：</p>
 *
 * <ul>
 *     <li>{@link FlowStartAccessStrategy}：判断能不能发起流程。</li>
 *     <li>{@link FlowTaskAccessStrategy}：判断能不能办理某个任务。</li>
 *     <li>{@link FlowInstanceAccessStrategy}：判断能不能操作整个流程实例。</li>
 * </ul>
 *
 * <p>典型使用场景包括撤回、终止、挂起、激活、作废等实例级操作。审批人是否能办理任务，
 * 不代表他天然拥有挂起、激活、终止整个流程的权限，所以这类操作需要单独策略控制。</p>
 *
 * <p>默认实现建议：</p>
 * <ul>
 *     <li>撤回：只允许流程发起人。</li>
 *     <li>挂起、激活、终止、作废、超时结束：只允许管理员。</li>
 * </ul>
 *
 * <p>核心模块不内置用户、角色、部门、岗位、流程管理员等组织关系。业务系统如果要支持
 * “流程管理员可操作”“部门负责人可操作”“指定角色可操作”等规则，请注册自定义
 * {@link FlowInstanceAccessStrategy} Bean，并在实现中查询自己的组织、权限或业务单据数据。</p>
 */
public interface FlowInstanceAccessStrategy {

    /**
     * 判断当前操作人是否允许执行指定实例操作。
     *
     * <p>调用时机：核心运行服务在真正修改流程实例状态前调用该方法。返回 {@code false} 时，
     * 本次操作会被拒绝，实例状态和任务数据不会继续变更。</p>
     *
     * <p>参数说明：</p>
     * <ul>
     *     <li>{@code creator}：当前操作人，通常来自 {@link FlowCreatorProvider#getCurrentCreator()}。</li>
     *     <li>{@code instance}：活动实例。流程已结束、已撤回、已终止等情况下可能为 {@code null}。</li>
     *     <li>{@code hisInstance}：历史实例。实例创建后通常一定存在，可用于读取发起人、当前状态、变量等。</li>
     *     <li>{@code operateType}：本次实例操作类型，例如挂起、激活、撤回、终止、作废。</li>
     * </ul>
     *
     * <p>实现建议：</p>
     * <ul>
     *     <li>普通业务系统可默认允许流程发起人操作撤回。</li>
     *     <li>挂起、激活、终止、作废一般建议只允许流程管理员、业务管理员或特定角色操作。</li>
     *     <li>如果有租户隔离，建议先校验 {@code creator.tenantId} 和实例租户是否一致。</li>
     *     <li>如果 {@code operateType} 是 {@link FlowInstanceOperateEnum#TIMEOUT}，通常由系统任务或管理员触发。</li>
     * </ul>
     *
     * @param creator 当前操作人
     * @param instance 当前活动实例，可能为 {@code null}
     * @param hisInstance 历史实例，可能为 {@code null}
     * @param operateType 实例操作类型
     * @return true 表示允许操作；false 表示拒绝操作
     */
    boolean isAllowed(FlowCreator creator, FlowInstance instance, FlowHisInstance hisInstance,
                      FlowInstanceOperateEnum operateType);
}
