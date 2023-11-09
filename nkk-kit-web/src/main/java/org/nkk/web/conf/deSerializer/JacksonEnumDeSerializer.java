package org.nkk.web.conf.deSerializer;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import org.apache.commons.lang3.StringUtils;
import org.nkk.core.enums.common.BaseEnum;

import java.io.IOException;

/**
 * <p>描述: Jackson枚举反序列化器
 * <p>开发者: dlj
 * <p>时间: 2022/11/4 15:24
 **/
public class JacksonEnumDeSerializer extends JsonDeserializer<BaseEnum> {


    @SuppressWarnings("unchecked")
    @Override
    public BaseEnum deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        Object currentValue = jsonParser.getCurrentValue();
        Class<?> propertyType = ObjectUtil.getTypeArgument(currentValue);
        System.out.println("......");
        if(BaseEnum.class.isAssignableFrom(propertyType)){
            Class<BaseEnum> clazz = (Class<BaseEnum>) propertyType;
            String text = node.asText();
            if(StringUtils.isNotBlank(text)){
                return BaseEnum.resolveKey(clazz,text);
            }
        }
        return null;
    }

    @Override
    public Class<BaseEnum> handledType() {
        return BaseEnum.class;
    }


}
