package org.nkk.flow.autoconfigure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.entity.FlowTaskActor;
import org.nkk.flow.enums.runtime.FlowEventTypeEnum;
import org.nkk.flow.model.node.FlowNodeModel;

import java.util.List;

/**
 * Spring 任务事件。
 *
 * <p>对标钉钉 {@code bpms_task_change} 与飞书 {@code approval_task} 的自包含负载：
 * 事件体直接携带 {@link #processKey} 与 {@link #businessKey}，
 * 消费方拿到事件即可按流程/业务路由，无需再按 instanceId 反查实例表。</p>
 */
@Getter
@AllArgsConstructor
public class NkkFlowTaskEvent {

    private final FlowEventTypeEnum eventType;

    private final FlowTask task;

    private final List<FlowTaskActor> actors;

    private final FlowNodeModel nodeModel;

    private final FlowCreator creator;

    /**
     * 流程定义 key（发布时从所属实例冗余，task 为 null 或反查失败时可能为 null）。
     */
    private final String processKey;

    /**
     * 业务 KEY（发布时从所属实例冗余，task 为 null 或反查失败时可能为 null）。
     */
    private final String businessKey;
}
