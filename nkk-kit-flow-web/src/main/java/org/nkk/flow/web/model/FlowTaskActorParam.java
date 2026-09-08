package org.nkk.flow.web.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 任务参与人参数。
 */
@Data
public class FlowTaskActorParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 参与人 ID。
     */
    private String actorId;

    /**
     * 参与人名称。
     */
    private String actorName;

    /**
     * 参与人类型，取值见 {@link org.nkk.flow.enums.core.FlowTaskActorEnum.ActorType}。
     */
    private Integer actorType;
}
