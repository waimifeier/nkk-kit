package org.nkk.flow.core.extension.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存流程模型缓存。
 */
public class SimpleFlowModelCache implements FlowModelCache {

    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    @Override
    public void put(String key, Object value) {
        cache.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) cache.get(key);
    }

    @Override
    public void remove(String key) {
        cache.remove(key);
    }
}


