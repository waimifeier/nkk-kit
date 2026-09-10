package org.nkk.flow.model;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 流程字段元数据。
 *
 * <p>统一承载设计器条件分支字段、自定义表单字段、业务表单字段等场景的字段描述。
 * 字段元数据最终随流程模型 JSON 持久化，由前端设计器在发布流程时一并传入。</p>
 */
@Data
public class FlowFieldMeta implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字段编码，条件分支和表单渲染时作为唯一标识。
     */
    private String fieldKey;

    /**
     * 字段展示名称。
     */
    private String fieldName;

    /**
     * 字段类型，取值见 {@link org.nkk.flow.enums.core.FlowFormFieldEnum.FieldType}。
     */
    private String fieldType;

    /**
     * 字段路径，支持对象或数组嵌套路径。为空时默认等于 fieldKey。
     */
    private String fieldPath;

    /**
     * 是否必填，0 否 1 是。
     */
    private Integer required;

    /**
     * 选项列表 JSON，select/multi_select/radio/checkbox 类型字段使用。
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

    /**
     * 扩展字段，预留。
     */
    private Map<String, Object> extra = new LinkedHashMap<>();
}
