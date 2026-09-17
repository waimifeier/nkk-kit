package org.nkk.flow.web.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.nkk.core.beans.common.BaseQuery;

import java.io.Serializable;
import java.util.Date;

/**
 * 待办中心筛选分页请求基类（接口 2/3/4 共用）。
 *
 * <p>继承分页参数 {@code current}（页码）、{@code size}（每页条数）。</p>
 * <p>子类通过继承获得以下筛选字段，时间字段语义由子类注释界定。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowTodoFilterPageRequest extends BaseQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 流程单号（实例 ID），精确匹配，可选。
     */
    private Long instanceId;

    /**
     * 发起人名称，模糊匹配 flow_his_instance.create_by，可选。
     */
    private String startUserName;

    /**
     * 表单来源类型（flow_process.form_source_type），可选；
     * 示例值：{@code form}（自定义表单）、{@code business}（业务表单）。
     */
    private String sourceType;

    /**
     * 流程分类（flow_process.process_type），精确匹配，可选。
     */
    private String processType;

    /**
     * 时间范围下限（含），语义由子类界定（发起时间 / 抄送时间 / 审批时间），可选。
     */
    private Date startTime;

    /**
     * 时间范围上限（含），语义由子类界定（发起时间 / 抄送时间 / 审批时间），可选。
     */
    private Date endTime;
}
