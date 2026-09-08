package org.nkk.flow.autoconfigure;

import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.core.extension.listener.FlowInstanceListener;
import org.nkk.flow.entity.FlowHisInstance;
import org.nkk.flow.enums.runtime.FlowEventTypeEnum;
import org.nkk.flow.model.FlowNodeModel;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Spring 实例事件发布器。
 *
 * <p>当配置 {@code nkk.flow.eventing.instance=true} 且使用方没有自定义
 * {@link FlowInstanceListener} Bean 时，starter 会自动装配该监听器。核心流程引擎触发实例事件后，
 * 该监听器会把事件包装成 {@link NkkFlowInstanceEvent} 并发布到 Spring 容器。</p>
 *
 * <p>使用方可以通过 {@code @EventListener}、{@code ApplicationListener} 或自定义
 * {@link FlowInstanceListener} Bean 接收实例事件，用于业务落库、消息通知、审计日志等扩展。</p>
 */
public class SpringFlowInstanceListener implements FlowInstanceListener {

    private final ApplicationEventPublisher publisher;

    /**
     * 创建 Spring 实例事件发布器。
     *
     * @param publisher Spring 应用事件发布器
     */
    public SpringFlowInstanceListener(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * 接收核心引擎实例事件，并转发为 Spring 应用事件。
     *
     * @param eventType 实例事件类型
     * @param instance 历史实例快照，部分删除或级联场景可能只包含可用字段
     * @param nodeModel 当前相关节点模型，非节点触发事件时可能为 {@code null}
     * @param creator 当前操作人，系统自动动作通常为管理员身份
     */
    @Override
    public void notify(FlowEventTypeEnum eventType, FlowHisInstance instance, FlowNodeModel nodeModel, FlowCreator creator) {
        publisher.publishEvent(new NkkFlowInstanceEvent(eventType, instance, nodeModel, creator));
    }
}

