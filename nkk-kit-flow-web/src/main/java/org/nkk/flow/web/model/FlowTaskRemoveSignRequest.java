package org.nkk.flow.web.model;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 审批减签请求。
 */
@Data
public class FlowTaskRemoveSignRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 要删除的加签节点编码。
     */
    private String nodeKey;

    /**
     * 审批意见。
     */
    private String opinion;

    /**
     * 操作变量。
     */
    private Map<String, Object> variables = new LinkedHashMap<>();
}
