package org.nkk.flow.web.model;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 发起流程请求。
 */
@Data
public class FlowStartProcessRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 流程定义 ID。优先按该字段发起流程。
     */
    private Long processId;

    /**
     * 流程 key。未传流程定义 ID 时，按流程 key 发起。
     */
    private String processKey;

    /**
     * 流程版本。为空时使用当前最新版本。
     */
    private Integer processVersion;

    /**
     * 业务主键，用于关联业务单据。
     */
    private String businessKey;

    /**
     * 是否暂存草稿。
     */
    private Boolean saveAsDraft = false;

    /**
     * 流程变量。
     */
    private Map<String, Object> variables = new LinkedHashMap<>();
}
