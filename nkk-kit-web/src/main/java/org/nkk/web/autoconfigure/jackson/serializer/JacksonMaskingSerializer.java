package org.nkk.web.autoconfigure.jackson.serializer;

import cn.hutool.core.util.DesensitizedUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.nkk.core.enums.verify.MaskingMethod;
import org.nkk.web.annotations.MaskingFormat;

import java.io.IOException;
import java.util.Objects;


/**
 * 数据脱敏序列化器
 *
 * @author dlj
 * @date 2023/07/29
 */
@NoArgsConstructor
@AllArgsConstructor
public class JacksonMaskingSerializer extends JsonSerializer<String> implements ContextualSerializer  {

    /**
     * 屏蔽方法
     */
    private MaskingMethod maskingMethod;

    /**
     * 隐藏字符串
     */
    @Getter
    private String symbol;


    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        DesensitizedUtil.DesensitizedType desensitizedType =
                StreamEx.of(DesensitizedUtil.DesensitizedType.values())
                .findFirst(item -> StringUtils.equals(item.name(),this.maskingMethod.name()))
                .orElseThrow(()-> new IllegalArgumentException("脱敏方式不存在"));

        gen.writeString(DesensitizedUtil.desensitized(value,desensitizedType));
    }


    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {

        if (property == null){
            return prov.findNullValueSerializer(null);
        }

        if (Objects.equals(property.getType().getRawClass(),String.class)){
            MaskingFormat annotation = property.getAnnotation(MaskingFormat.class);
            if (annotation == null) {
                annotation = property.getContextAnnotation(MaskingFormat.class);
            }
            if (annotation != null){
                MaskingMethod method = annotation.method();
                String symbol = annotation.symbol();
                return new JacksonMaskingSerializer(method,symbol);
            }
        }
        return prov.findValueSerializer(String.class, property);
    }

}
