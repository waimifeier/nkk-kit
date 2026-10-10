package org.nkk.flow.web.extension;

import org.nkk.flow.entity.FlowHisInstance;

import java.util.Map;

/**
 * 审批结果回调处理器 SPI（仅实例状态回写）。
 *
 * <p>审批引擎在流程实例的关键生命周期节点触发回调，业务系统通过实现此接口
 * 同步自己的单据状态（通过、拒绝、撤回、终止等）。</p>
 *
 * <h3>职责边界（与 Spring 事件通道分离）</h3>
 * <p>本接口只承载<b>实例状态</b>回调，对标钉钉 {@code bpms_instance_change}
 * （agree/refuse/terminate/cancel）与飞书审批实例事件
 * （START/APPROVED/REJECTED/CANCELED/DELETED）的业务语义。</p>
 * <ul>
 *   <li><b>实例状态回写</b> → 实现本接口（业务系统更新业务单据状态）。</li>
 *   <li><b>任务级/过程级事件</b>（任务创建、完成、催办、提醒、转办等）→ 不进本接口，
 *       统一走引擎发布的 Spring 事件（{@code NkkFlowInstanceEvent}/{@code NkkFlowTaskEvent}），
 *       由宿主公共监听器处理（消息通知、日志、待办等）。</li>
 * </ul>
 *
 * <h3>多流程多 Handler 路由</h3>
 * <p>业务系统有 N 条不同流程时，可以注册 N 个 Handler Bean，每个 Handler 只处理自己关心的流程。
 * 适配器在 dispatch 前会先调用 {@link #supports(String)} 做路由过滤，
 * 只有返回 true 的 Handler 才会收到回调。</p>
 *
 * <pre>{@code
 * // 只处理订单审批
 * @Component
 * public class OrderApprovalCallback implements FlowApprovalCallbackHandler {
 *     @Override
 *     public boolean supports(String processKey) {
 *         return "purchase-order".equals(processKey);
 *     }
 *     @Override
 *     public void onInstanceComplete(FlowHisInstance instance) {
 *         orderRepository.updateStatus(instance.getBusinessKey(), "APPROVED");
 *     }
 * }
 *
 * // 只处理请假审批
 * @Component
 * public class LeaveApprovalCallback implements FlowApprovalCallbackHandler {
 *     @Override
 *     public boolean supports(String processKey) {
 *         return "leave".equals(processKey);
 *     }
 *     @Override
 *     public void onInstanceComplete(FlowHisInstance instance) {
 *         leaveRepository.updateStatus(instance.getBusinessKey(), "APPROVED");
 *     }
 * }
 * }</pre>
 *
 * <p>如果不覆盖 {@code supports}，默认返回 true——即接收所有流程的回调。
 * 适合只注册一个 Handler、在内部通过 {@code instance.getProcessKey()} 或
 * {@code instance.getBusinessKey()} 前缀做分发的场景。</p>
 *
 * <h3>businessKey 命名约定</h3>
 * <p>也可以用 businessKey 前缀区分业务类型，例如：
 * {@code order:ORD-001}、{@code leave:LEAVE-001}、{@code expense:EXP-001}。
 * 这种方式适合 Handler 内部统一处理、再分发给不同 Repository 的场景。</p>
 *
 * <h3>多 Handler 支持</h3>
 * <p>可以有多个 Handler Bean，适配器会按 order 值从小到大逐个触发。
 * 如果某个 Handler 抛出异常，会打印 ERROR 日志但不影响其他 Handler 和主流程。</p>
 */
public interface FlowApprovalCallbackHandler {

    /**
     * 判断当前 Handler 是否关心指定流程 key 的回调。
     *
     * <p>适配器在 dispatch 前先调用此方法做路由过滤。
     * 默认返回 true——接收所有流程的回调。</p>
     *
     * @param processKey 流程定义 key
     * @return true 表示接收该流程的回调
     */
    default boolean supports(String processKey) {
        return true;
    }

    /**
     * 流程实例发起后回调。
     *
     * <p>此时实例状态为审批中，第一个任务已生成但还没有人处理。</p>
     *
     * @param instance 流程实例
     * @param variables 发起时传入的流程变量
     */
    default void onInstanceStart(FlowHisInstance instance, Map<String, Object> variables) {
    }

    /**
     * 流程实例审批通过回调。
     *
     * @param instance 流程实例，此时 {@code instanceState = 1}
     */
    default void onInstanceComplete(FlowHisInstance instance) {
    }

    /**
     * 流程实例审批拒绝回调。
     *
     * @param instance 流程实例，此时 {@code instanceState = 2}
     */
    default void onInstanceReject(FlowHisInstance instance) {
    }

    /**
     * 流程实例撤销回调。
     *
     * @param instance 流程实例，此时 {@code instanceState = 3}
     */
    default void onInstanceRevoke(FlowHisInstance instance) {
    }

    /**
     * 流程实例终止回调（强制终止、超时、作废、级联删除等非正常通过/拒绝的终态）。
     *
     * @param instance 流程实例，此时 {@code instanceState} 可能是 5、6 或 7
     */
    default void onInstanceTerminate(FlowHisInstance instance) {
    }

    /**
     * 回调执行顺序，数值越小越先执行。默认 0。
     */
    default int order() {
        return 0;
    }

    /**
     * 空实现，方便测试或作为默认 Bean。
     */
    FlowApprovalCallbackHandler EMPTY = new FlowApprovalCallbackHandler() {
        @Override
        public void onInstanceStart(FlowHisInstance instance, Map<String, Object> variables) {
        }

        @Override
        public void onInstanceComplete(FlowHisInstance instance) {
        }

        @Override
        public void onInstanceReject(FlowHisInstance instance) {
        }

        @Override
        public void onInstanceRevoke(FlowHisInstance instance) {
        }

        @Override
        public void onInstanceTerminate(FlowHisInstance instance) {
        }
    };
}
