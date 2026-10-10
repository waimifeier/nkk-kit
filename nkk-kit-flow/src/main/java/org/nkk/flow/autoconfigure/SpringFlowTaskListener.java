package org.nkk.flow.autoconfigure;

import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.core.extension.listener.FlowTaskListener;
import org.nkk.flow.entity.FlowInstance;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.entity.FlowTaskActor;
import org.nkk.flow.enums.runtime.FlowEventTypeEnum;
import org.nkk.flow.model.node.FlowNodeModel;
import org.nkk.flow.service.FlowQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

/**
 * Spring 任务事件发布器。
 *
 * <p>当配置 {@code flow.eventing.task=true} 且使用方没有自定义
 * {@link FlowTaskListener} Bean 时，starter 会自动装配该监听器。核心流程引擎触发任务事件后，
 * 该监听器会把事件包装成 {@link NkkFlowTaskEvent} 并发布到 Spring 容器。</p>
 *
 * <p>发布时会按 {@code task.instanceId} 反查一次实例，把 {@code processKey}/{@code businessKey}
 * 冗余进事件体，保证事件负载自包含（对标钉钉/飞书事件模型），消费方无需再反查数据库。</p>
 *
 * <p>使用方可以通过 {@code @EventListener}、{@code ApplicationListener} 或自定义
 * {@link FlowTaskListener} Bean 接收任务事件，用于待办通知、审批记录、业务审计、消息推送等扩展。</p>
 */
public class SpringFlowTaskListener implements FlowTaskListener {

    private static final Logger log = LoggerFactory.getLogger(SpringFlowTaskListener.class);

    private final ApplicationEventPublisher publisher;

    private final FlowQueryService queryService;

    /**
     * 创建 Spring 任务事件发布器。
     *
     * @param publisher    Spring 应用事件发布器
     * @param queryService 流程查询服务（用于反查实例，冗余 processKey/businessKey）
     */
    public SpringFlowTaskListener(ApplicationEventPublisher publisher, FlowQueryService queryService) {
        this.publisher = publisher;
        this.queryService = queryService;
    }

    /**
     * 接收核心引擎任务事件，并转发为 Spring 应用事件。
     *
     * @param eventType 任务事件类型
     * @param task 当前任务或已归档任务快照，部分事件可能为 {@code null}
     * @param actors 当前任务参与者快照，任务归档事件中为归档前参与者
     * @param nodeModel 当前相关节点模型，非节点触发事件时可能为 {@code null}
     * @param creator 当前操作人，系统自动动作通常为管理员身份
     */
    @Override
    public void notify(FlowEventTypeEnum eventType, FlowTask task, List<FlowTaskActor> actors,
                       FlowNodeModel nodeModel, FlowCreator creator) {
        String processKey = null;
        String businessKey = null;
        if (task != null && task.getInstanceId() != null) {
            try {
                FlowInstance instance = queryService.getInstance(task.getInstanceId());
                if (instance != null) {
                    processKey = instance.getProcessKey();
                    businessKey = instance.getBusinessKey();
                }
            } catch (Exception e) {
                // 反查失败不阻断事件发布，事件体中 processKey/businessKey 置空
                log.warn("[FlowEvent] 任务事件反查实例失败，eventType={}, instanceId={}",
                        eventType, task.getInstanceId());
            }
        }
        publisher.publishEvent(new NkkFlowTaskEvent(eventType, task, actors, nodeModel, creator,
                processKey, businessKey));
    }
}
