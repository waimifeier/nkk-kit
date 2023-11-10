package org.nkk.web.conf.json;

import org.nkk.core.enums.common.BaseEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.util.Objects;

/**
 * 枚举转换工厂：可以处理query 枚举参数、 formData 对象参数
 */
public class EnumsConvertorFactory implements ConverterFactory<String, BaseEnum> {

    public EnumsConvertorFactory(){

    }

    @Override
    public <T extends BaseEnum> Converter<String, T> getConverter(Class<T> targetType) {

        if (!BaseEnum.class.isAssignableFrom(targetType)) {
            return null;
        }

        return source -> {
            for (T t : targetType.getEnumConstants()) {
                if (Objects.equals(String.valueOf(t.value()), source)) {
                    return t;
                }
            }
            return null;
        };
    }
}
