package org.nkk.flow.web.model;

import lombok.Data;
import org.nkk.flow.entity.FlowFormField;

import java.io.Serializable;
import java.util.Date;

/**
 * 流程表单字段展示对象。
 */
@Data
public class FlowFormFieldVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String tenantId;

    private String createId;

    private String createBy;

    private Date createTime;

    private Long formId;

    private String formKey;

    private Integer formVersion;

    private String fieldKey;

    private String fieldName;

    private String fieldType;

    private String fieldPath;

    private String sourceType;

    private Integer required;

    private String optionsJson;

    private Integer sort;

    private String remark;

    public static FlowFormFieldVO of(FlowFormField field) {
        if (field == null) {
            return null;
        }
        FlowFormFieldVO vo = new FlowFormFieldVO();
        vo.setId(field.getId());
        vo.setTenantId(field.getTenantId());
        vo.setCreateId(field.getCreateId());
        vo.setCreateBy(field.getCreateBy());
        vo.setCreateTime(field.getCreateTime());
        vo.setFormId(field.getFormId());
        vo.setFormKey(field.getFormKey());
        vo.setFormVersion(field.getFormVersion());
        vo.setFieldKey(field.getFieldKey());
        vo.setFieldName(field.getFieldName());
        vo.setFieldType(field.getFieldType());
        vo.setFieldPath(field.getFieldPath());
        vo.setSourceType(field.getSourceType());
        vo.setRequired(field.getRequired());
        vo.setOptionsJson(field.getOptionsJson());
        vo.setSort(field.getSort());
        vo.setRemark(field.getRemark());
        return vo;
    }
}
