package org.nkk.flow.web.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 流程元表单请求。
 *
 * <p>发布流程时携带的表单绑定信息，最终写入流程模型 JSON 的 {@code extendConfig.metaForm}。
 * 不再落独立表，字段元数据随流程模型一起持久化。</p>
 */
@Data
public class FlowMetaFormRequest implements Serializable {

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

    /**
     * 字段元数据列表。
     *
     * <p>随流程模型 JSON 一起持久化，供设计器条件分支和运行时表单渲染使用。</p>
     */
    private List<FlowFieldMeta> fields;
}
