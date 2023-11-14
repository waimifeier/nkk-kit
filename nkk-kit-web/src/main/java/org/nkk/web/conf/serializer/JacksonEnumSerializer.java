package org.nkk.web.conf.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import lombok.NoArgsConstructor;
import org.nkk.core.enums.common.BaseEnum;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;


/**
 * <p>描述: 枚举{@link BaseEnum} 的jackson序列化实现
 * <p>开发者: dlj 
 * <p>时间: 2022/1/27 13:56
 **/
@JsonComponent
@NoArgsConstructor
public class JacksonEnumSerializer extends JsonSerializer<BaseEnum> {

    @Override
    public void serialize(BaseEnum baseEnum, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        System.out.println("枚举序列化。。");
        gen.writeObject(baseEnum.getEnumResp());
    }

    @Override
    public Class<BaseEnum> handledType() {
        return BaseEnum.class;
    }

}
