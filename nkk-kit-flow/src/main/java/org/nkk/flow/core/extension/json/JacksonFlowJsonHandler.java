package org.nkk.flow.core.extension.json;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Jackson JSON 处理器。
 */
public class JacksonFlowJsonHandler implements FlowJsonHandler {

    private final ObjectMapper objectMapper;

    public JacksonFlowJsonHandler() {
        this(new ObjectMapper());
    }

    public JacksonFlowJsonHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T fromJson(String json, Class<T> type) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalArgumentException("流程 JSON 解析失败", e);
        }
    }

    @Override
    public <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            throw new IllegalArgumentException("流程 JSON 解析失败", e);
        }
    }

    @Override
    public String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("流程 JSON 序列化失败", e);
        }
    }
}

