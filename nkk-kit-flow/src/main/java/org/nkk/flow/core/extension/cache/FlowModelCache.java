package org.nkk.flow.core.extension.cache;

/**
 * 流程模型缓存。
 */
public interface FlowModelCache {

    void put(String key, Object value);

    <T> T get(String key);

    void remove(String key);
}


