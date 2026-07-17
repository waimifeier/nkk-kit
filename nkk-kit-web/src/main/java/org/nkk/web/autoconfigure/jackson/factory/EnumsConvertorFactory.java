package org.nkk.web.autoconfigure.jackson.factory;

import org.nkk.core.enums.common.IEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.util.Objects;

/**
 * 枚举转换工厂：可以处理query 枚举参数、 formData 对象参数
 *
 */
public class EnumsConvertorFactory implements ConverterFactory<String, IEnum<?>> {

    public EnumsConvertorFactory(){

    }

    @Override
    public <T extends IEnum<?>> Converter<String, T> getConverter(Class<T> targetType) {
        if (!IEnum.class.isAssignableFrom(targetType)) {
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
