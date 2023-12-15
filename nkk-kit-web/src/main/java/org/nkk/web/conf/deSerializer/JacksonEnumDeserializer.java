package org.nkk.web.conf.deSerializer;

import cn.hutool.core.util.ReflectUtil;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nkk.core.enums.common.BaseEnum;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;

@Setter
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class JacksonEnumDeserializer extends JsonDeserializer<Enum<?>> implements ContextualDeserializer {

    private JavaType type;


    @Override
    public Enum<?> deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JacksonException {
        Class<?> enumType = type.getRawClass();
        if(Objects.isNull(enumType) || !enumType.isEnum()){
            return null;
        }

        String text = parser.getText();
        Enum<?>[] enumConstants = (Enum<?>[]) enumType.getEnumConstants();
        if (BaseEnum.class.isAssignableFrom(enumType) && StringUtils.isNotBlank(text)) {
            Method method = ReflectUtil.getMethod(enumType, "value");
            for (Enum<?> e : enumConstants) {
                try {
                    if(StringUtils.equals(method.invoke(e).toString(), text)) {
                        return e;
                    }
                }catch (Exception ex){
                   log.error("枚举值[{}]匹配失败",text);
                   return null;
                }
            }
        }else {
            for (Enum<?> enumConstant : enumConstants) {
                if(StringUtils.equals(enumConstant.name(),text)){
                    return enumConstant;
                }
            }
        }
        return null;
    }


    @Override
    public Class<BaseEnum> handledType() {
        return BaseEnum.class;
    }


    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctx, BeanProperty property) throws JsonMappingException {
        JavaType type = ctx.getContextualType() != null ? ctx.getContextualType() : property.getMember().getType();
        return new JacksonEnumDeserializer(type);
    }
}