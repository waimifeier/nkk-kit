package org.nkk.flow.web.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.model.FlowFieldMeta;

import java.io.Serializable;
import java.util.List;

/**
 * 流程元表单展示对象。
 */
@Data
public class FlowMetaFormVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sourceType;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long formId;

    private String formKey;

    private String formVersion;

    private String formName;

    private List<FlowFieldMeta> fields;

    public static FlowMetaFormVO of(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof FlowMetaFormVO) {
            return (FlowMetaFormVO) value;
        }
        return FlowContext.fromJson(FlowContext.toJson(value), FlowMetaFormVO.class);
    }
}
