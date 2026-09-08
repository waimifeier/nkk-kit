package org.nkk.flow.web.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 表单字段保存项。
 */
@Data
public class FlowFormFieldSaveItemRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字段编码。
     */
    private String fieldKey;

    /**
     * 字段名称。
     */
    private String fieldName;

    /**
     * 字段类型。
     */
    private String fieldType;

    /**
     * 字段路径。
     */
    private String fieldPath;

    /**
     * 字段来源类型。
     */
    private String sourceType;

    /**
     * 是否必填，1 是，0 否。
     */
    private Integer required;

    /**
     * 选项 JSON。
     */
    private String optionsJson;

    /**
     * 排序值。
     */
    private Integer sort;

    /**
     * 备注。
     */
    private String remark;
}
