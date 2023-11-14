package org.nkk.web.conf.deSerializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.nkk.core.enums.common.BaseEnum;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@Setter
@JsonComponent
public class JacksonEnumDeserializer extends JsonDeserializer<BaseEnum> implements ContextualDeserializer {

    private JavaType type;

    public JacksonEnumDeserializer(){

    }

    public JacksonEnumDeserializer(JavaType type) {
        this.type = type;
    }

    @Override
    public BaseEnum deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JacksonException {
        String text = parser.getText();
        Class<?> rawClass = type.getRawClass();
        if (BaseEnum.class.isAssignableFrom(rawClass) && StringUtils.isNotBlank(text)) {
            return BaseEnum.resolveKey((Class<BaseEnum>) rawClass,text);
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