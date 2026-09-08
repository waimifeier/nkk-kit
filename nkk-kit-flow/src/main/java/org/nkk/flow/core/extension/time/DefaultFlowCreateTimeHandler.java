package org.nkk.flow.core.extension.time;

import org.nkk.flow.enums.runtime.FlowExecuteTypeEnum;

import java.util.Date;

/**
 * 默认流程时间处理器。
 */
public class DefaultFlowCreateTimeHandler implements FlowCreateTimeHandler {

    @Override
    public Date getCurrentTime(FlowExecuteTypeEnum executeType, Long instanceId, Long taskId) {
        return new Date();
    }

    @Override
    public Date getFinishTime(Long instanceId, Long taskId) {
        return new Date();
    }
}


