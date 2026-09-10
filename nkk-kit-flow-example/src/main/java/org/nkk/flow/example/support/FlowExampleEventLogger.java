package org.nkk.flow.example.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.nkk.flow.autoconfigure.NkkFlowInstanceEvent;
import org.nkk.flow.autoconfigure.NkkFlowTaskEvent;
import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.entity.FlowHisInstance;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.entity.FlowTaskActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 审批流事件日志监听器示例。
 *
 * <p>对应 application.yml 中的两个开关：</p>
 * <ul>
 *     <li>{@code flow.eventing.task=true}：starter 发布 {@link NkkFlowTaskEvent}（任务级事件）</li>
 *     <li>{@code flow.eventing.instance=true}：starter 发布 {@link NkkFlowInstanceEvent}（实例级事件）</li>
 * </ul>
 *
 * <p>使用方在业务系统中实现类似的 {@code @EventListener} 即可接收事件，
 * 用于待办通知、消息推送、业务单据状态同步、审批审计等场景。</p>
 */
@Component
public class FlowExampleEventLogger {

    private static final Logger log = LoggerFactory.getLogger(FlowExampleEventLogger.class);

    /**
     * 监听任务级事件：任务创建、完成、拒绝、转办、加签、超时、触发等。
     */
    @EventListener
    public void onTaskEvent(NkkFlowTaskEvent event) {
        FlowTask task = event.getTask();
        String actorNames = describeActors(event.getActors());
        String nodeName = event.getNodeModel() == null ? null : event.getNodeModel().getNodeName();

        if (task == null) {
            // 子流程启动/结束等事件不携带具体任务
            log.info("[流程任务事件] {} 操作人={} 节点={} 参与人={}",
                    describeType(event.getEventType() == null ? null : event.getEventType().name()),
                    describeCreator(event.getCreator()), nodeName, actorNames);
            return;
        }
        log.info("[流程任务事件] {} 任务ID={} 实例ID={} 节点={}({}) 参与人={} 操作人={}",
                describeType(event.getEventType() == null ? null : event.getEventType().name()),
                task.getId(), task.getInstanceId(), task.getTaskName(), task.getTaskKey(),
                actorNames, describeCreator(event.getCreator()));
    }

    /**
     * 监听实例级事件：实例启动、结束、暂停、激活、撤销、终止、作废等。
     */
    @EventListener
    public void onInstanceEvent(NkkFlowInstanceEvent event) {
        FlowHisInstance instance = event.getInstance();
        String nodeName = event.getNodeModel() == null ? null : event.getNodeModel().getNodeName();

        if (instance == null) {
            log.info("[流程实例事件] {} 节点={} 操作人={}",
                    describeType(event.getEventType() == null ? null : event.getEventType().name()),
                    nodeName, describeCreator(event.getCreator()));
            return;
        }
        log.info("[流程实例事件] {} 实例ID={} 流程标识={} 业务键={} 当前节点={}({}) 实例状态={} 操作人={}",
                describeType(event.getEventType() == null ? null : event.getEventType().name()),
                instance.getId(), instance.getProcessKey(), instance.getBusinessKey(),
                instance.getCurrentNodeName(), instance.getCurrentNodeKey(),
                instance.getInstanceState(), describeCreator(event.getCreator()));
    }

    /**
     * 拼接参与者列表，格式：姓名(ID)，多人用逗号分隔。
     */
    private String describeActors(List<FlowTaskActor> actors) {
        if (CollUtil.isEmpty(actors)) {
            return "-";
        }
        return actors.stream()
                .map(actor -> StrUtil.format("{}({})", actor.getActorName(), actor.getActorId()))
                .collect(Collectors.joining(", "));
    }

    /**
     * 描述操作人，格式：姓名(ID)。
     */
    private String describeCreator(FlowCreator creator) {
        if (creator == null) {
            return "-";
        }
        return StrUtil.format("{}({})", creator.getCreateBy(), creator.getCreateId());
    }

    /**
     * 事件类型可能为 null（引擎内部部分通知不传事件类型），做空值保护。
     */
    private String describeType(String eventType) {
        return eventType == null ? "UNKNOWN" : eventType;
    }
}
