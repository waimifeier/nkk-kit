package org.nkk.flow.web.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 手动抄送请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowTaskCopyRequest extends FlowTaskActorRequest {

    private static final long serialVersionUID = 1L;

    /**
     * 抄送原因。
     */
    private String copyReason;
}
