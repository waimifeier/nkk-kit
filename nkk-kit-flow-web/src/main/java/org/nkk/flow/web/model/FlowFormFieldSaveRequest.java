package org.nkk.flow.web.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 表单字段保存请求。
 */
@Data
public class FlowFormFieldSaveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 租户 ID。
     */
    private String tenantId;

    /**
     * 表单 ID。
     */
    private Long formId;

    /**
     * 表单版本号。
     */
    private Integer formVersion;

    /**
     * 字段来源类型，取值见 {@link org.nkk.flow.enums.core.FlowFormFieldEnum.SourceType}。
     *
     * <p>为空时默认按 form 处理。</p>
     */
    private String sourceType;

    /**
     * 字段列表。
     */
    private List<FlowFormFieldSaveItemRequest> fields;
}
