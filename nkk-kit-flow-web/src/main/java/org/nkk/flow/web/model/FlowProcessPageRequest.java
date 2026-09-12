package org.nkk.flow.web.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.nkk.core.beans.common.BaseQuery;

import java.io.Serializable;

/**
 * 流程定义分页查询请求。
 *
 * <p>分页参数 {@code current}（页码，从 1 开始）、{@code size}（每页条数）
 * 继承自 {@link BaseQuery}；其余字段均为可选筛选条件。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowProcessPageRequest extends BaseQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 租户 ID，为空时只查询无租户数据。
     */
    private String tenantId;

    /**
     * 流程名称，模糊匹配。
     */
    private String processName;

    /**
     * 流程 key，精确匹配。
     */
    private String processKey;

    /**
     * 流程分类，精确匹配。
     */
    private String processType;

    /**
     * 流程状态，取值见 org.nkk.flow.enums.core.FlowProcessEnum.ProcessState。
     */
    private Integer processState;

    /**
     * 表单来源类型，精确匹配，例如 {@code form}（自定义表单）、{@code business}（业务表单）。
     */
    private String formSourceType;
}
