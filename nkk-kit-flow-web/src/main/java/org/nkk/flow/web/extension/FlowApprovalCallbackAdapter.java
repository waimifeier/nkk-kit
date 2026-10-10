package org.nkk.flow.web.extension;

import org.nkk.flow.autoconfigure.NkkFlowInstanceEvent;
import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.entity.FlowHisInstance;
import org.nkk.flow.enums.runtime.FlowEventTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@link FlowApprovalCallbackHandler} 事件适配器（仅实例状态回调）。
 *
 * <p>引擎通过 {@code FlowInstanceListener}/{@code FlowTaskListener} 发布 Spring 事件，
 * 本适配器只监听<b>实例级</b>事件并翻译为 {@link FlowApprovalCallbackHandler} 的业务状态回写方法；
 * 任务级/过程级事件（创建、完成、催办、提醒、转办等）不进本适配器，
 * 由宿主公共监听器直接消费 Spring 事件。</p>
 * <p>使用方只需实现一个或多个 {@code FlowApprovalCallbackHandler} Bean，就能在审批实例
 * 生命周期节点收到通知并更新自己的业务表状态。</p>
 *
 * <h3>事件映射（对标飞书实例状态：START/APPROVED/REJECTED/CANCELED/DELETED）</h3>
 * <table>
 *   <tr><th>引擎事件</th><th>→ Handler 回调</th></tr>
 *   <tr><td>INSTANCE_STARTED</td><td>onInstanceStart(instance, variables)</td></tr>
 *   <tr><td>INSTANCE_ENDED</td><td>onInstanceComplete(instance)</td></tr>
 *   <tr><td>INSTANCE_REJECTED</td><td>onInstanceReject(instance)</td></tr>
 *   <tr><td>INSTANCE_REVOKED</td><td>onInstanceRevoke(instance)</td></tr>
 *   <tr><td>INSTANCE_TERMINATED / TIMEOUT / DESTROYED / CASCADE_REMOVED</td><td>onInstanceTerminate(instance)</td></tr>
 * </table>
 *
 * <h3>事务安全</h3>
 * <p>使用 {@code @EventListener} 默认在当前事务内执行。如果 Handler 内要更新业务表，
 * 可以加 {@code @Transactional} 注解参与同一事务。如果要在事务提交后执行，
 * 可以在 Handler Bean 的方法上加 {@code @TransactionalEventListener(phase = AFTER_COMMIT)}。</p>
 *
 * <h3>多 Handler 支持</h3>
 * <p>容器中可以有多个 {@code FlowApprovalCallbackHandler} Bean，适配器会逐个触发。
 * 某个 Handler 抛异常会被 catch 并打印日志，不影响其他 Handler 和主流程。
 * 如果要控制执行顺序，可以在 Handler 上实现 {@code Ordered} 接口或加
 * {@code @Order} 注解，按 order 值从小到大执行。</p>
 */
public class FlowApprovalCallbackAdapter {

    private static final Logger log = LoggerFactory.getLogger(FlowApprovalCallbackAdapter.class);

    @Autowired(required = false)
    private List<FlowApprovalCallbackHandler> handlers = new ArrayList<>();

    /**
     * 监听实例事件，翻译为业务状态回写回调。
     */
    @EventListener
    public void onInstanceEvent(NkkFlowInstanceEvent event) {
        FlowEventTypeEnum type = event.getEventType();
        FlowHisInstance instance = event.getInstance();
        if (instance == null) {
            return;
        }
        String processKey = instance.getProcessKey();

        try {
            switch (type) {
                case INSTANCE_STARTED:
                    dispatch(processKey, h -> h.onInstanceStart(instance, extractVariables(instance)));
                    break;
                case INSTANCE_ENDED:
                    // 正常结束 = 审批通过
                    dispatch(processKey, h -> h.onInstanceComplete(instance));
                    break;
                case INSTANCE_REJECTED:
                    dispatch(processKey, h -> h.onInstanceReject(instance));
                    break;
                case INSTANCE_REVOKED:
                    dispatch(processKey, h -> h.onInstanceRevoke(instance));
                    break;
                case INSTANCE_TERMINATED:
                case INSTANCE_TIMEOUT:
                case INSTANCE_DESTROYED:
                case INSTANCE_CASCADE_REMOVED:
                    dispatch(processKey, h -> h.onInstanceTerminate(instance));
                    break;
                default:
                    // 其他实例事件（suspend/active/resume/变量更新/模型更新/节点增删等）不触发业务状态回写
                    break;
            }
        } catch (Exception e) {
            log.error("[FlowApprovalCallback] 处理实例事件异常，eventType={}, instanceId={}, processKey={}",
                    type, instance.getId(), processKey, e);
        }
    }

    private void dispatch(String processKey, java.util.function.Consumer<FlowApprovalCallbackHandler> action) {
        if (handlers == null || handlers.isEmpty()) {
            return;
        }
        for (FlowApprovalCallbackHandler handler : handlers) {
            try {
                if (!handler.supports(processKey)) {
                    continue;
                }
                action.accept(handler);
            } catch (Exception e) {
                log.error("[FlowApprovalCallback] Handler 执行异常，handler={}, processKey={}",
                        handler.getClass().getSimpleName(), processKey, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractVariables(FlowHisInstance instance) {
        if (instance == null || instance.getVariable() == null) {
            return java.util.Collections.emptyMap();
        }
        try {
            return FlowContext.fromJson(instance.getVariable(), Map.class);
        } catch (Exception e) {
            log.warn("[FlowApprovalCallback] 解析实例变量失败，instanceId={}", instance.getId());
            return java.util.Collections.emptyMap();
        }
    }
}
