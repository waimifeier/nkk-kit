package org.nkk.flow.web.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 待我审批记录分页请求。
 *
 * <p>查询当前登录人需要审批的活动任务对应的流程实例。
 * {@link #startTime} / {@link #endTime} 语义为<b>发起时间</b>（flow_his_instance.create_time）。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowPendingPageRequest extends FlowTodoFilterPageRequest {

    private static final long serialVersionUID = 1L;
}
