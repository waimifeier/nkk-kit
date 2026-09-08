package org.nkk.flow.core.extension.time;

import org.nkk.flow.enums.runtime.FlowExecuteTypeEnum;

import java.util.Date;

/**
 * 流程时间处理器。
 */
public interface FlowCreateTimeHandler {

    Date getCurrentTime(FlowExecuteTypeEnum executeType, Long instanceId, Long taskId);

    Date getFinishTime(Long instanceId, Long taskId);
}


