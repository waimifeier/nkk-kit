package org.nkk.flow.core.extension.identity;

import cn.hutool.core.collection.CollUtil;
import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.entity.FlowTaskActor;

import java.util.List;

/**
 * 默认任务访问策略。
 *
 * <p>该类只负责任务参与者列表的遍历，具体判断通过
 * {@link FlowCreator#matches(String, Integer)} 完成。业务系统只需在构造 {@link FlowCreator}
 * 时填充 {@code deptId}、{@code roleIds}，无需再注册自定义策略 Bean。</p>
 */
public class DefaultFlowTaskAccessStrategy implements FlowTaskAccessStrategy {

    @Override
    public FlowTaskActor isAllowed(FlowCreator creator, List<FlowTaskActor> taskActors) {
        if (creator == null || CollUtil.isEmpty(taskActors)) {
            return null;
        }
        for (FlowTaskActor taskActor : taskActors) {
            if (taskActor == null) {
                continue;
            }
            if (creator.matches(taskActor.getActorId(), taskActor.getActorType())) {
                return taskActor;
            }
        }
        return null;
    }
}
