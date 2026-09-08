package org.nkk.flow.core.context;

import java.util.HashMap;
import java.util.Map;

/**
 * 单次调用线程内流程参数传递。
 */
public final class FlowDataTransfer {

    public static final String DYNAMIC_ASSIGNEE = "_dynamicAssignee";
    public static final String CONDITION_NODE_KEY = "_conditionNodeKey";

    private static final ThreadLocal<Map<String, Object>> HOLDER = new ThreadLocal<>();

    private FlowDataTransfer() {
    }

    public static void put(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        Map<String, Object> map = getOrCreate();
        map.putAll(data);
    }

    public static void put(String key, Object value) {
        getOrCreate().put(key, value);
    }

    public static void dynamicAssignee(Map<String, Object> data) {
        put(DYNAMIC_ASSIGNEE, data);
    }

    public static void specifyConditionNodeKey(String conditionNodeKey) {
        put(CONDITION_NODE_KEY, conditionNodeKey);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        Map<String, Object> map = HOLDER.get();
        return map == null ? null : (T) map.get(key);
    }

    public static Map<String, Object> getAll() {
        Map<String, Object> map = HOLDER.get();
        return map == null ? new HashMap<>() : new HashMap<>(map);
    }

    public static void removeByKey(String key) {
        Map<String, Object> map = HOLDER.get();
        if (map != null) {
            map.remove(key);
        }
    }

    public static void remove() {
        HOLDER.remove();
    }

    private static Map<String, Object> getOrCreate() {
        Map<String, Object> map = HOLDER.get();
        if (map == null) {
            map = new HashMap<>();
            HOLDER.set(map);
        }
        return map;
    }
}


