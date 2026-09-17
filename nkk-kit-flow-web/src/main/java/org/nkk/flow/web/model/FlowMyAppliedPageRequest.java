package org.nkk.flow.web.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.nkk.core.beans.common.BaseQuery;

import java.io.Serializable;
import java.util.Date;

/**
 * 我发起的记录分页请求。
 *
 * <p>查询当前登录用户发起的流程实例，支持按审批状态和发起时间范围筛选。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowMyAppliedPageRequest extends BaseQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 审批状态，可选；取值见 {@link org.nkk.flow.enums.core.FlowInstanceEnum.InstanceState}：
     * 0-审批中、1-审批通过、2-审批拒绝、3-已撤销；不传表示查询全部状态。
     */
    private Integer instanceState;

    /**
     * 发起时间下限（含），格式 yyyy-MM-dd HH:mm:ss，可选。
     */
    private Date startTime;

    /**
     * 发起时间上限（含），格式 yyyy-MM-dd HH:mm:ss，可选。
     */
    private Date endTime;
}
