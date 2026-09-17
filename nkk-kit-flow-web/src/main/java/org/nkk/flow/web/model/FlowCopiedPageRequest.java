package org.nkk.flow.web.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 抄送给我记录分页请求。
 *
 * <p>查询抄送给当前登录人的记录（活动抄送 + 历史抄送）。
 * {@link #startTime} / {@link #endTime} 语义为<b>抄送时间</b>（抄送任务的 create_time）。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowCopiedPageRequest extends FlowTodoFilterPageRequest {

    private static final long serialVersionUID = 1L;
}
