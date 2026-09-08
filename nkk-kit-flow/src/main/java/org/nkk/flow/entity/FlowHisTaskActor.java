package org.nkk.flow.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 历史任务参与者。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flow_his_task_actor")
public class FlowHisTaskActor extends FlowTaskActor {

    private static final long serialVersionUID = 1L;

    /**
     * 根据活动任务参与者创建历史任务参与者。
     *
     * @param actor 活动任务参与者
     * @return 历史任务参与者
     */
    public static FlowHisTaskActor of(FlowTaskActor actor) {
        FlowHisTaskActor his = new FlowHisTaskActor();
        his.setId(actor.getId());
        his.setTenantId(actor.getTenantId());
        his.setInstanceId(actor.getInstanceId());
        his.setTaskId(actor.getTaskId());
        his.setActorId(actor.getActorId());
        his.setActorName(actor.getActorName());
        his.setActorType(actor.getActorType());
        his.setWeight(actor.getWeight());
        his.setAgentId(actor.getAgentId());
        his.setAgentType(actor.getAgentType());
        his.setExt(actor.getExt());
        return his;
    }
}


