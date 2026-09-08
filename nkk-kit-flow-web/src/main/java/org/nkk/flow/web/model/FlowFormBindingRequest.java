package org.nkk.flow.web.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 流程表单绑定请求。
 */
@Data
public class FlowFormBindingRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 表单来源类型，取值见 {@link org.nkk.flow.enums.core.FlowFormFieldEnum.SourceType}。
     */
    private String sourceType;

    /**
     * 表单 ID。
     */
    private Long formId;

    /**
     * 表单编码。
     */
    private String formKey;

    /**
     * 表单版本号。
     */
    private Integer formVersion;

    /**
     * 表单名称。
     */
    private String formName;
}
