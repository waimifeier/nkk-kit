package org.nkk.flow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.nkk.flow.enums.core.FlowFormFieldEnum;

/**
 * 流程表单字段元数据。
 *
 * <p>用于给流程设计器条件节点提供可选字段列表，也用于绑定已有业务表单或自定义表单字段。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flow_form_field")
public class FlowFormField extends FlowEntity {

    private static final long serialVersionUID = 1L;

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
     * 字段编码，例如 days、amount、leaveType。
     */
    private String fieldKey;

    /**
     * 字段名称，例如 请假天数、报销金额。
     */
    private String fieldName;

    /**
     * 字段类型，取值见 {@link FlowFormFieldEnum.FieldType}。
     */
    private String fieldType;

    /**
     * 字段路径，例如 days、employee.name、items[].amount。
     */
    private String fieldPath;

    /**
     * 字段来源类型，取值见 {@link FlowFormFieldEnum.SourceType}。
     */
    private String sourceType;

    /**
     * 是否必填，1 是，0 否。
     */
    private Integer required;

    /**
     * 选项 JSON，适用于单选、多选等字段。
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
