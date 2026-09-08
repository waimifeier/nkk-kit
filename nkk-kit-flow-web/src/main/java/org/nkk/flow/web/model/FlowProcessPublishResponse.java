package org.nkk.flow.web.model;

import lombok.Data;
import org.nkk.flow.entity.FlowProcess;

import java.io.Serializable;

/**
 * 流程发布响应。
 */
@Data
public class FlowProcessPublishResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 流程定义 ID。
     */
    private Long processId;

    /**
     * 流程 key。
     */
    private String processKey;

    /**
     * 当前发布后的流程版本号。
     */
    private Integer processVersion;

    /**
     * 当前发布后的流程定义。
     */
    private FlowProcess process;
}
