package org.nkk.flow.web.model;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 任务操作请求。
 */
@Data
public class FlowTaskOperateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 目标节点编码。驳回、跳转等场景使用。
     */
    private String nodeKey;

    /**
     * 审批意见。通过、驳回等动作使用。
     */
    private String opinion;

    /**
     * 操作变量，例如表单数据。
     */
    private Map<String, Object> variables = new LinkedHashMap<>();
}
