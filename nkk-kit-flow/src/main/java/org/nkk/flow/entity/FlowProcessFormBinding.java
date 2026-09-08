package org.nkk.flow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.nkk.flow.enums.core.FlowFormFieldEnum;

/**
 * 流程与表单绑定信息。
 *
 * <p>用于记录流程定义选择了哪个表单，以及表单来源类型、版本等基础信息。
 * 字段元数据仍然保存在 {@code flow_form_field} 中。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flow_process_form_binding")
public class FlowProcessFormBinding extends FlowEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 流程定义 ID。
     */
    private Long processId;

    /**
     * 表单来源类型，取值见 {@link FlowFormFieldEnum.SourceType}。
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
