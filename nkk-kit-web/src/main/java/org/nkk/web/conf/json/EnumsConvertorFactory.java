package org.nkk.web.conf.json;

import org.nkk.core.enums.common.BaseEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;


public class EnumsConvertorFactory implements ConverterFactory<String, BaseEnum> {

    public EnumsConvertorFactory(){

    }

    @Override
    public <T extends BaseEnum> Converter<String, T> getConverter(Class<T> targetType) {
        return source -> (T) BaseEnum.resolveKey(targetType,source);
    }
}
