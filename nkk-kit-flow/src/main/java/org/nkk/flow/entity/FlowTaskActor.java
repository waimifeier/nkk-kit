package org.nkk.flow.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.nkk.flow.enums.core.FlowTaskActorEnum.AgentType;
import org.nkk.flow.enums.core.FlowTaskActorEnum.ActorType;
import org.nkk.flow.model.node.task.FlowNodeAssignee;

import java.io.Serializable;

/**
 * 活动任务参与者。
 */
@Data
@TableName("flow_task_actor")
public class FlowTaskActor implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID，由流程引擎的 ID 生成器写入。
     */
    @TableId(type = IdType.INPUT)
    private Long id;

    /**
     * 租户 ID，用于多租户场景下的数据隔离。
     */
    private String tenantId;

    /**
     * 流程实例 ID。
     */
    private Long instanceId;

    /**
     * 任务 ID。
     */
    private Long taskId;

    /**
     * 参与者 ID。
     */
    private String actorId;

    /**
     * 参与者名称。
     */
    private String actorName;

    /**
     * 参与者类型，取值见 {@link ActorType}。
     */
    private Integer actorType = ActorType.USER.value();

    /**
     * 参与者权重，票签场景用于计算通过或拒绝比例。
     */
    private Integer weight;

    /**
     * 代理关系中的代理人 ID。
     */
    private String agentId;

    /**
     * 代理参与类型，取值见 {@link AgentType}。
     */
    private Integer agentType;

    /**
     * 扩展信息 JSON 内容。
     */
    private String ext;

    /**
     * 根据节点办理人创建活动任务参与者。
     *
     * @param assignee 节点办理人
     * @param defaultActorType 默认参与者类型
     * @param saveWeight 是否保存办理人权重
     * @return 活动任务参与者
     */
    public static FlowTaskActor of(FlowNodeAssignee assignee, Integer defaultActorType, boolean saveWeight) {
        FlowTaskActor actor = new FlowTaskActor();
        actor.setActorId(assignee.getId());
        actor.setActorName(assignee.getName());
        actor.setActorType(assignee.getActorType() == null ? defaultActorType : assignee.getActorType());
        actor.setWeight(saveWeight ? assignee.getWeight() : null);
        return actor;
    }
}


