package org.nkk.flow.core.extension.json;

/**
 * 流程 JSON 处理器。
 */
public interface FlowJsonHandler {

    <T> T fromJson(String json, Class<T> type);

    String toJson(Object value);
}

