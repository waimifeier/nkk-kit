package org.nkk.flow.core.extension.id;

/**
 * 流程 ID 生成器。
 */
public interface FlowIdGenerator {

    Long nextId(Long preferredId);
}

