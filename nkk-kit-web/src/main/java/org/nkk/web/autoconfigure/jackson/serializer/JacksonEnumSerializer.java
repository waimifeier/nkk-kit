package org.nkk.web.autoconfigure.jackson.serializer;

import cn.hutool.core.util.ReflectUtil;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.NoArgsConstructor;
import org.nkk.core.enums.common.IEnum;

import java.io.IOException;
import java.lang.reflect.Field;


/**
 * 对base枚举进行序列化操作
 */
@NoArgsConstructor
public class JacksonEnumSerializer extends JsonSerializer<IEnum> {

    @Override
    public void serialize(IEnum iEnum, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Field[] fields = ReflectUtil.getFieldsDirectly(iEnum.getClass(),false);
        for (Field field : fields) {
            JsonValue annotation = field.getAnnotation(JsonValue.class);
            if(annotation != null) {
                gen.writeObject(ReflectUtil.getFieldValue(iEnum,field.getName()));
                return;
            }
        }
        gen.writeObject(iEnum.getEnumResp());
    }

    @Override
    public Class<IEnum> handledType() {
        return IEnum.class;
    }

}
