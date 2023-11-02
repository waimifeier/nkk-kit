package org.nkk.web.conf.deSerializer;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import org.apache.commons.lang3.StringUtils;
import org.nkk.core.enums.common.BaseEnum;

import java.io.IOException;

/**
*   Jackson枚举反序列化器
* @author Nkks
* @class JacksonEnumDeSerializer
* @time 2022/1/27 9:50
*/
public class JacksonEnumDeSerializer extends JsonDeserializer<BaseEnum> {

    /**
    *    反序列化
    * @author Nkks
    * @time 2022/1/27 9:52
    * @param jsonParser jsonParser
    * @param deserializationContext deserializationContext
    * @return {@link BaseEnum}
    * @throws IOException  IOException
    */
    @SuppressWarnings("unchecked")
    @Override
    public BaseEnum deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String currentName = jsonParser.currentName();
        Object currentValue = jsonParser.getCurrentValue();
        Class<?> propertyType = ObjectUtil.getTypeArgument(currentValue);
        if(BaseEnum.class.isAssignableFrom(propertyType)){
            Class<BaseEnum> clazz = (Class<BaseEnum>) propertyType;
            String text = node.asText();
            if(StringUtils.isNotBlank(text)){
                return BaseEnum.resolve(text,clazz);
            }
        }
        return null;
    }

    @Override
    public Class<BaseEnum> handledType() {
        return BaseEnum.class;
    }


}
