package org.nkk.web.conf.serializer;

import cn.hutool.core.util.ReflectUtil;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.NoArgsConstructor;
import org.nkk.core.enums.common.BaseEnum;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.lang.reflect.Field;


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
        Field[] fields = ReflectUtil.getFieldsDirectly(baseEnum.getClass(),false);
        for (Field field : fields) {
            JsonValue annotation = field.getAnnotation(JsonValue.class);
            if(annotation != null) {
                gen.writeObject(ReflectUtil.getFieldValue(baseEnum,field.getName()));
                return;
            }
        }
        gen.writeObject(baseEnum.getEnumResp());
    }

    @Override
    public Class<BaseEnum> handledType() {
        return BaseEnum.class;
    }

}
