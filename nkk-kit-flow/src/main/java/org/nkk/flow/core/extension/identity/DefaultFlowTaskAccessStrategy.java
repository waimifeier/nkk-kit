package org.nkk.flow.core.extension.identity;

import cn.hutool.core.collection.CollUtil;
import org.nkk.flow.entity.FlowTaskActor;

import java.util.List;

/**
 * 默认任务访问策略。
 *
 * <p>该类只负责任务参与者列表的遍历，具体判断委托给 {@link FlowActorAccessStrategy}。</p>
 */
public class DefaultFlowTaskAccessStrategy implements FlowTaskAccessStrategy {

    private final FlowActorAccessStrategy actorAccessStrategy;

    public DefaultFlowTaskAccessStrategy() {
        this(new DefaultFlowActorAccessStrategy());
    }

    public DefaultFlowTaskAccessStrategy(FlowActorAccessStrategy actorAccessStrategy) {
        this.actorAccessStrategy = actorAccessStrategy == null
                ? new DefaultFlowActorAccessStrategy()
                : actorAccessStrategy;
    }

    @Override
    public FlowTaskActor isAllowed(String userId, List<FlowTaskActor> taskActors) {
        if (userId == null || CollUtil.isEmpty(taskActors)) {
            return null;
        }
        for (FlowTaskActor taskActor : taskActors) {
            if (taskActor == null) {
                continue;
            }
            if (actorAccessStrategy.isAllowed(userId, taskActor)) {
                return taskActor;
            }
        }
        return null;
    }
}

