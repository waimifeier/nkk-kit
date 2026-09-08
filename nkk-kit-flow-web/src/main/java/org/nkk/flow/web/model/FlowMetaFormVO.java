package org.nkk.flow.web.model;

import lombok.Data;
import org.nkk.flow.core.context.FlowContext;

import java.io.Serializable;
import java.util.List;

/**
 * 流程元表单展示对象。
 */
@Data
public class FlowMetaFormVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sourceType;

    private Long formId;

    private String formKey;

    private Integer formVersion;

    private String formName;

    private List<FlowFieldMeta> fields;

    public static FlowMetaFormVO of(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof FlowMetaFormVO) {
            return (FlowMetaFormVO) value;
        }
        if (value instanceof FlowMetaFormRequest) {
            FlowMetaFormRequest request = (FlowMetaFormRequest) value;
            FlowMetaFormVO vo = new FlowMetaFormVO();
            vo.setSourceType(request.getSourceType());
            vo.setFormId(request.getFormId());
            vo.setFormKey(request.getFormKey());
            vo.setFormVersion(request.getFormVersion());
            vo.setFormName(request.getFormName());
            vo.setFields(request.getFields());
            return vo;
        }
        return FlowContext.fromJson(FlowContext.toJson(value), FlowMetaFormVO.class);
    }
}
