package org.nkk.flow.web.extension;

import org.nkk.flow.autoconfigure.NkkFlowInstanceEvent;
import org.nkk.flow.autoconfigure.NkkFlowTaskEvent;
import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.entity.FlowHisInstance;
import org.nkk.flow.entity.FlowHisTask;
import org.nkk.flow.entity.FlowHisTaskActor;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.entity.FlowTaskActor;
import org.nkk.flow.enums.runtime.FlowEventTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@link FlowApprovalCallbackHandler} 事件适配器。
 *
 * <p>引擎通过 {@code FlowInstanceListener}/{@code FlowTaskListener} 发布 Spring 事件，
 * 本适配器监听这些事件并翻译为 {@link FlowApprovalCallbackHandler} 的业务友好回调方法。
 * 使用方只需实现一个或多个 {@code FlowApprovalCallbackHandler} Bean，就能在审批生命周期节点
 * 收到通知并更新自己的业务表状态。</p>
 *
 * <h3>事件映射</h3>
 * <table>
 *   <tr><th>引擎事件</th><th>→ Handler 回调</th></tr>
 *   <tr><td>INSTANCE_STARTED</td><td>onInstanceStart(instance, variables)</td></tr>
 *   <tr><td>INSTANCE_ENDED</td><td>onInstanceComplete(instance) 或 onInstanceTerminate(instance)</td></tr>
 *   <tr><td>INSTANCE_REJECTED</td><td>onInstanceReject(instance)</td></tr>
 *   <tr><td>INSTANCE_REVOKED</td><td>onInstanceRevoke(instance)</td></tr>
 *   <tr><td>INSTANCE_TERMINATED / TIMEOUT / DESTROYED</td><td>onInstanceTerminate(instance)</td></tr>
 *   <tr><td>TASK_CREATED</td><td>onTaskCreate(task, actors)</td></tr>
 *   <tr><td>TASK_COMPLETED / REJECTED / TRANSFERRED / ...</td><td>onTaskComplete(task, actors)</td></tr>
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

    @Autowired(required = false)
    private org.nkk.flow.service.FlowQueryService flowQueryService;

    /**
     * 监听实例事件。
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
                    dispatch(processKey, h -> h.onInstanceTerminate(instance));
                    break;
                default:
                    // 其他实例事件（suspend/active/resume 等）暂不触发业务回调
                    break;
            }
        } catch (Exception e) {
            log.error("[FlowApprovalCallback] 处理实例事件异常，eventType={}, instanceId={}, processKey={}",
                    type, instance.getId(), processKey, e);
        }
    }

    /**
     * 监听任务事件。
     */
    @EventListener
    public void onTaskEvent(NkkFlowTaskEvent event) {
        FlowEventTypeEnum type = event.getEventType();
        FlowTask task = event.getTask();
        if (task == null) {
            return;
        }

        // 任务事件需要从事件上下文获取 processKey
        // 通过 FlowExtInstance 或 FlowInstance 反查；先尝试从 task 所属实例查
        String processKey = resolveProcessKeyForTask(task.getInstanceId());

        try {
            switch (type) {
                case TASK_CREATED:
                    dispatch(processKey, h -> h.onTaskCreate(toHisTask(task), toHisActors(event.getActors())));
                    break;
                case TASK_COMPLETED:
                case TASK_REJECTED:
                case TASK_TRANSFERRED:
                case TASK_DELEGATED:
                case TASK_RESOLVED:
                case TASK_RECLAIMED:
                case TASK_WITHDRAWN:
                case TASK_AUTO_COMPLETED:
                case TASK_AUTO_REJECTED:
                case TASK_JUMPED:
                case TASK_ROUTE_JUMPED:
                case TASK_REJECT_JUMPED:
                case TASK_RE_APPROVE_JUMPED:
                case TASK_TERMINATED:
                case TASK_REVOKED:
                case TASK_DESTROYED:
                case TASK_REJECT_ENDED:
                case TASK_ABSTAINED:
                    dispatch(processKey, h -> h.onTaskComplete(toHisTask(task), toHisActors(event.getActors())));
                    break;
                default:
                    // 其他任务事件（claim/view/copy/remind/timeout 等）暂不触发业务回调
                    break;
            }
        } catch (Exception e) {
            log.error("[FlowApprovalCallback] 处理任务事件异常，eventType={}, taskId={}, processKey={}",
                    type, task.getId(), processKey, e);
        }
    }

    /**
     * 从任务反查 processKey。
     * 优先从活动实例查，没有则从历史实例查。
     */
    private String resolveProcessKeyForTask(Long instanceId) {
        if (instanceId == null) {
            return null;
        }
        try {
            org.nkk.flow.entity.FlowInstance instance = flowQueryService.getInstance(instanceId);
            if (instance != null && instance.getProcessKey() != null) {
                return instance.getProcessKey();
            }
            org.nkk.flow.entity.FlowHisInstance his = flowQueryService.getHisInstance(instanceId);
            if (his != null) {
                return his.getProcessKey();
            }
        } catch (Exception e) {
            log.warn("[FlowApprovalCallback] 反查 processKey 失败，instanceId={}", instanceId, e);
        }
        return null;
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

    /**
     * 把活动任务快照转为历史任务对象（最小字段拷贝，供 Handler 读取）。
     */
    private FlowHisTask toHisTask(FlowTask task) {
        FlowHisTask his = new FlowHisTask();
        his.setId(task.getId());
        his.setInstanceId(task.getInstanceId());
        his.setTaskName(task.getTaskName());
        his.setTaskKey(task.getTaskKey());
        his.setTaskType(task.getTaskType());
        his.setTaskState(0); // 活动状态
        his.setOpinion(task.getOpinion());
        his.setCreateId(task.getCreateId());
        his.setCreateBy(task.getCreateBy());
        his.setCreateTime(task.getCreateTime());
        his.setParentTaskId(task.getParentTaskId());
        return his;
    }

    /**
     * 把活动参与者转为历史参与者对象。
     */
    private List<FlowHisTaskActor> toHisActors(List<FlowTaskActor> actors) {
        if (actors == null) {
            return new ArrayList<>();
        }
        List<FlowHisTaskActor> result = new ArrayList<>(actors.size());
        for (FlowTaskActor actor : actors) {
            FlowHisTaskActor his = new FlowHisTaskActor();
            his.setId(actor.getId());
            his.setInstanceId(actor.getInstanceId());
            his.setTaskId(actor.getTaskId());
            his.setActorId(actor.getActorId());
            his.setActorName(actor.getActorName());
            his.setActorType(actor.getActorType());
            his.setWeight(actor.getWeight());
            result.add(his);
        }
        return result;
    }
}
