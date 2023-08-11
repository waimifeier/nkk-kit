package org.nkk.conf.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.nkk.enums.base.BaseEnum;

import java.io.IOException;


/**
*   枚举{@link BaseEnum} 的jackson序列化实现
* @author Nkks
* @class JacksonEnumSerializer
* @time 2022/1/27 13:56
*/
public class JacksonEnumSerializer extends JsonSerializer<BaseEnum> {

    @Override
    public void serialize(BaseEnum baseEnum, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObject(baseEnum.getEnumResp());
    }

    @Override
    public Class<BaseEnum> handledType() {
        return BaseEnum.class;
    }
}
