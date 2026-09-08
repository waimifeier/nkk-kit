package org.nkk.flow.web.model;

import lombok.Data;
import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.entity.FlowProcessFormBinding;

import java.io.Serializable;
import java.util.List;

/**
 * 流程表单绑定展示对象。
 */
@Data
public class FlowFormBindingVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sourceType;

    private Long formId;

    private String formKey;

    private Integer formVersion;

    private String formName;

    private List<FlowFormFieldSaveItemRequest> fields;

    public static FlowFormBindingVO of(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof FlowFormBindingVO) {
            return (FlowFormBindingVO) value;
        }
        if (value instanceof FlowFormBindingRequest) {
            FlowFormBindingRequest request = (FlowFormBindingRequest) value;
            FlowFormBindingVO vo = new FlowFormBindingVO();
            vo.setSourceType(request.getSourceType());
            vo.setFormId(request.getFormId());
            vo.setFormKey(request.getFormKey());
            vo.setFormVersion(request.getFormVersion());
            vo.setFormName(request.getFormName());
            vo.setFields(request.getFields());
            return vo;
        }
        if (value instanceof FlowProcessFormBinding) {
            FlowProcessFormBinding binding = (FlowProcessFormBinding) value;
            FlowFormBindingVO vo = new FlowFormBindingVO();
            vo.setSourceType(binding.getSourceType());
            vo.setFormId(binding.getFormId());
            vo.setFormKey(binding.getFormKey());
            vo.setFormVersion(binding.getFormVersion());
            vo.setFormName(binding.getFormName());
            return vo;
        }
        return FlowContext.fromJson(FlowContext.toJson(value), FlowFormBindingVO.class);
    }
}
