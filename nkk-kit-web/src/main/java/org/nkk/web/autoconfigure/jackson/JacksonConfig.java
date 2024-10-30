package org.nkk.web.autoconfigure.jackson;

import org.nkk.core.enums.common.BaseEnum;
import org.nkk.web.autoconfigure.jackson.deSerializer.JacksonEnumDeserializer;
import org.nkk.web.autoconfigure.jackson.serializer.JacksonEnumSerializer;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 自动处理base枚举序列化和反序列化操作
 */
@AutoConfigureBefore(JacksonAutoConfiguration.class)
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {
            builder.deserializerByType(Enum.class, new JacksonEnumDeserializer());
            builder.serializerByType(BaseEnum.class, new JacksonEnumSerializer());
        };
    }

    /*
    *
    *  如果需要开启formData参数 对base枚举处理，需要在 WebMvcConfigurer 的子类中，重写一下方法：
    *
    * <code>
       @Override
       public void addFormatters(FormatterRegistry registry) {
         registry.addConverterFactory(new EnumsConvertorFactory());
       }
    * </code>
    *
    * */

}

