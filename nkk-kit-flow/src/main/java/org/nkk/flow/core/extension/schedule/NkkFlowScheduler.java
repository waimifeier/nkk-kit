package org.nkk.flow.core.extension.schedule;

import lombok.Getter;
import lombok.Setter;
import org.nkk.flow.service.NkkFlowEngine;

import java.util.Date;

/**
 * 流程提醒和超时调度门面。
 */
@Getter
@Setter
public class NkkFlowScheduler {

    private final NkkFlowEngine flowEngine;

    private FlowJobLock jobLock;

    public NkkFlowScheduler(NkkFlowEngine flowEngine, FlowJobLock jobLock) {
        this.flowEngine = flowEngine;
        this.jobLock = jobLock;
    }

    /**
     * 处理当前时间之前已到期的提醒和超时任务。
     */
    public boolean remind() {
        return process(new Date());
    }

    /**
     * 按指定时间处理提醒和超时任务，便于测试或外部调度指定执行时间。
     */
    public boolean process(Date now) {
        if (jobLock != null && !jobLock.tryLock()) {
            return false;
        }
        try {
            flowEngine.processTimeoutOrRemind(now);
            return true;
        } finally {
            if (jobLock != null) {
                jobLock.unlock();
            }
        }
    }
}

