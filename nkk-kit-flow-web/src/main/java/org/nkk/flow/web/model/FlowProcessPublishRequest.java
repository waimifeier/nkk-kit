package org.nkk.flow.web.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 流程发布请求。
 */
@Data
public class FlowProcessPublishRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 流程名称。
     */
    private String processName;

    /**
     * 流程 key，同一流程多版本共享该编码。
     */
    private String processKey;

    /**
     * 流程图标标识。
     */
    private String processIcon;

    /**
     * 流程类型。
     */
    private String processType;

    /**
     * 流程描述。
     */
    private String remark;

    /**
     * 流程实例详情页地址。
     */
    private String instanceUrl;

    /**
     * 表单来源类型。例如 FORM、URL、CUSTOM。
     */
    private String formSourceType;

    /**
     * 表单 ID。
     */
    private Long formId;

    /**
     * 表单唯一编码。
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
     * 流程 JSON 模型内容。支持传 JSON 对象或 JSON 字符串。
     *
     * 这是模型内容 {@link org.nkk.flow.model.FlowProcessModel}
     *
     */
    private Object modelContent;

    /**
     * 是否保存为草稿。
     *
     * <p>true 表示只保存草稿，不强校验流程模型，也不会影响已启用版本；false 表示正式发布。</p>
     */
    private Boolean saveAsDraft = false;

    /**
     * 是否允许重复发布为新版本。
     */
    private Boolean repeat = true;
}
