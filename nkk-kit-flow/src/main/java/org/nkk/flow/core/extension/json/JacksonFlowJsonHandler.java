package org.nkk.flow.core.extension.json;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.nkk.flow.model.node.FlowNodeModel;
import org.nkk.flow.model.node.FlowNodeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Jackson JSON 处理器。
 *
 * <p>构造时会扫描节点模型包（{@link #NODE_MODEL_PACKAGE}）下带 {@link FlowNodeType}
 * 注解的类，自动向 ObjectMapper 注册节点多态子类型，新增节点类型无需手工维护清单。
 * 外部传入自定义 ObjectMapper 时同样会执行注册（注册幂等，可重复执行）。</p>
 */
public class JacksonFlowJsonHandler implements FlowJsonHandler {

    /**
     * 节点模型所在包，多态子类型扫描根路径（递归扫描 task/router/endpoint 子包）。
     */
    public static final String NODE_MODEL_PACKAGE = "org.nkk.flow.model.node";

    private final ObjectMapper objectMapper;

    public JacksonFlowJsonHandler() {
        this(new ObjectMapper());
    }

    public JacksonFlowJsonHandler(ObjectMapper objectMapper) {
        this.objectMapper = registerNodeSubtypes(objectMapper);
    }

    /**
     * 扫描 {@link FlowNodeType} 注解标注的节点模型子类并注册为 Jackson 多态子类型。
     *
     * <p>判别值取注解中枚举的 value（如 {@code 1} 表示审批节点），与 JSON 中
     * {@code type} 字段对应。抽象基类（未标注注解）自动跳过。</p>
     *
     * @param objectMapper 待注册的 ObjectMapper
     * @return 注册后的同一个 ObjectMapper
     */
    public static ObjectMapper registerNodeSubtypes(ObjectMapper objectMapper) {
        Set<Class<?>> classes = ClassUtil.scanPackage(NODE_MODEL_PACKAGE);
        List<NamedType> namedTypes = new ArrayList<>();
        for (Class<?> clazz : classes) {
            FlowNodeType annotation = clazz.getAnnotation(FlowNodeType.class);
            if (annotation == null
                    || !FlowNodeModel.class.isAssignableFrom(clazz)
                    || clazz == FlowNodeModel.class) {
                continue;
            }
            namedTypes.add(new NamedType(clazz, String.valueOf(annotation.value().value())));
        }
        if (!namedTypes.isEmpty()) {
            objectMapper.registerSubtypes(namedTypes.toArray(new NamedType[0]));
        }
        return objectMapper;
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
