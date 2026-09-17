package org.nkk.flow.web.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审批记录分页请求（当前登录人已审核的记录）。
 *
 * <p>查询当前登录人已经审批过的历史任务。
 * {@link #startTime} / {@link #endTime} 语义为<b>审批时间</b>（flow_his_task.finish_time）。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowApprovedPageRequest extends FlowTodoFilterPageRequest {

    private static final long serialVersionUID = 1L;
}
