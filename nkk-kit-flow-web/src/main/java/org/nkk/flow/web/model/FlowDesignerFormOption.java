package org.nkk.flow.web.model;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 流程设计器表单下拉选项。
 *
 * <p>用于前端设计器选择自定义表单或业务表单。核心模块不关心表单主数据来源，
 * 只接收使用方通过 Provider 暴露出来的选项。</p>
 */
@Data
public class FlowDesignerFormOption implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 选项 ID，通常就是表单 ID。
     */
    private String id;

    /**
     * 表单 ID。
     */
    private Long formId;

    /**
     * 表单编码。
     */
    private String formKey;

    /**
     * 表单名称。
     */
    private String formName;

    /**
     * 表单版本号。
     */
    private Integer formVersion;

    /**
     * 表单来源类型，取值见 {@code FlowFormFieldEnum.SourceType}。
     */
    private String sourceType;

    /**
     * 前端展示文本。
     */
    private String label;

    /**
     * 是否禁用选择。
     */
    private Boolean disabled = false;

    /**
     * 扩展字段，预留给前端设计器使用。
     */
    private Map<String, Object> extra = new LinkedHashMap<>();

    public static FlowDesignerFormOption of(Long formId, String formKey, String formName, Integer formVersion,
                                            String sourceType) {
        FlowDesignerFormOption option = new FlowDesignerFormOption();
        option.setId(formId == null ? null : String.valueOf(formId));
        option.setFormId(formId);
        option.setFormKey(formKey);
        option.setFormName(formName);
        option.setFormVersion(formVersion);
        option.setSourceType(sourceType);
        option.setLabel(formName);
        return option;
    }
}
