package org.nkk.web.autoconfigure.jackson;

import org.nkk.core.enums.common.IEnum;
import org.nkk.web.autoconfigure.jackson.deSerializer.JacksonDateDeSerializer;
import org.nkk.web.autoconfigure.jackson.deSerializer.JacksonEnumDeserializer;
import org.nkk.web.autoconfigure.jackson.factory.DateConverter;
import org.nkk.web.autoconfigure.jackson.serializer.JacksonEnumSerializer;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Date;

/**
 * 自动处理base枚举序列化和反序列化操作
 */
@AutoConfigureBefore(JacksonAutoConfiguration.class)
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {
            // 反序列化
            builder.deserializerByType(Enum.class, new JacksonEnumDeserializer());
            builder.deserializerByType(Date.class, new JacksonDateDeSerializer());

            // 序列化
            builder.serializerByType(IEnum.class, new JacksonEnumSerializer());
        };
    }

    @Bean
    public WebMvcConfigurer dateConverterWebMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addFormatters(FormatterRegistry registry) {
                registry.addConverter(new DateConverter());
            }
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

