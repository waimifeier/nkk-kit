package org.nkk.web.conf.json;

import org.nkk.core.enums.common.BaseEnum;
import org.nkk.web.conf.serializer.JacksonEnumSerializer;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfigureBefore(JacksonAutoConfiguration.class)
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {
           //builder.deserializerByType(Enum.class, new JacksonEnumDeserializer());
            builder.serializerByType(BaseEnum.class, new JacksonEnumSerializer());
        };
    }



}

