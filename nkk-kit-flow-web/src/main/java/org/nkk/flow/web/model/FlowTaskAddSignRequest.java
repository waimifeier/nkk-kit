package org.nkk.flow.web.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审批加签请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowTaskAddSignRequest extends FlowTaskActorRequest {

    private static final long serialVersionUID = 1L;

    /**
     * 加签方式：1 前加签，2 后加签。
     */
    private Integer signType = 1;

    /**
     * 追加节点编码。为空时由后端生成。
     */
    private String nodeKey;

    /**
     * 追加节点名称。
     */
    private String nodeName;

    /**
     * 审批意见。
     */
    private String opinion;
}
