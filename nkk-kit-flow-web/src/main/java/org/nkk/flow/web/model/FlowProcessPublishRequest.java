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
     * 流程 JSON 模型内容。支持传 JSON 对象或 JSON 字符串。
     *
     * <p>如果需要绑定表单元数据，前端应直接写入模型的
     * {@code extendConfig.metaForm}，后端发布时不再单独处理 metaForm 字段。</p>
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
