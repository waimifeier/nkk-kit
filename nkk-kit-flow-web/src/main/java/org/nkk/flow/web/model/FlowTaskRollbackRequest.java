package org.nkk.flow.web.model;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 任务回退请求。
 */
@Data
public class FlowTaskRollbackRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 回退目标节点编码。
     *
     * <p>该值来自流程模型节点的 nodeKey，通常由前端“回退节点”下拉框选择。</p>
     */
    private String nodeKey;

    /**
     * 回退审批意见。
     */
    private String opinion;

    /**
     * 操作变量，例如当前表单数据。
     */
    private Map<String, Object> variables = new LinkedHashMap<>();
}
