package org.nkk.flow.core.extension.json;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * JSON 处理扩展点。
 */
public interface FlowJsonHandler {

    <T> T fromJson(String json, Class<T> type);

    <T> T fromJson(String json, TypeReference<T> typeReference);

    String toJson(Object value);
}

