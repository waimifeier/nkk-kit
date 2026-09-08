package org.nkk.flow.core.extension.id;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 默认内存递增 ID 生成器。
 */
public class DefaultFlowIdGenerator implements FlowIdGenerator {

    private final AtomicLong counter = new AtomicLong(System.currentTimeMillis());

    @Override
    public Long nextId(Long preferredId) {
        return preferredId == null ? counter.incrementAndGet() : preferredId;
    }
}

