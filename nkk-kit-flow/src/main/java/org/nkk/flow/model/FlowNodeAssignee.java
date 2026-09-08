package org.nkk.flow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.nkk.flow.entity.FlowTaskActor;
import org.nkk.flow.enums.core.FlowTaskActorEnum.ActorType;

import java.io.Serializable;

/**
 * 节点配置的审批参与者。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlowNodeAssignee implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 参与者 ID，可能是用户、角色、部门或候选人等业务侧标识。
     */
    private String id;

    /**
     * 参与者名称。
     */
    private String name;

    /**
     * 参与者类型，取值见 {@link ActorType}。
     */
    private Integer actorType;

    /**
     * 参与者权重，票签场景用于计算通过或拒绝比例。
     */
    private Integer weight;

    /**
     * 根据任务参与者转换为节点参与者模型。
     *
     * @param taskActor 任务参与者
     * @return 节点参与者模型
     */
    public static FlowNodeAssignee of(FlowTaskActor taskActor) {
        FlowNodeAssignee assignee = new FlowNodeAssignee();
        assignee.setId(taskActor.getActorId());
        assignee.setName(taskActor.getActorName());
        assignee.setActorType(taskActor.getActorType());
        assignee.setWeight(taskActor.getWeight());
        return assignee;
    }
}

