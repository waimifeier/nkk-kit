package org.nkk.flow.web.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.nkk.core.beans.common.BaseQuery;

import java.io.Serializable;
import java.util.Date;

/**
 * 流程实例分页查询请求。
 *
 * <p>分页参数 {@code current}（页码，从 1 开始）、{@code size}（每页条数）
 * 继承自 {@link BaseQuery}；流程定义 ID 通过 URL 路径参数传入，其余字段均为可选筛选条件。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowInstancePageRequest extends BaseQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 发起人名称，模糊匹配。
     */
    private String starterName;

    /**
     * 发起开始时间（按提交时间 createTime 下限过滤，含），格式：{@code yyyy-MM-dd HH:mm:ss}。
     */
    private Date startTime;

    /**
     * 发起结束时间（按提交时间 createTime 上限过滤，含），格式：{@code yyyy-MM-dd HH:mm:ss}。
     */
    private Date endTime;

    /**
     * 实例状态，精确匹配，取值见 {@link org.nkk.flow.enums.core.FlowInstanceEnum.InstanceState}：
     * 0-审批中、1-审批通过、2-审批驳回、3-已撤回。为空时不过滤。
     */
    private Integer instanceState;

    /**
     * 实例 ID，精确匹配（实例主键）。
     */
    private Long instanceId;
}
