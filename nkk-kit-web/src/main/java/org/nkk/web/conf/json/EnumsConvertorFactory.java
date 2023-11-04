package org.nkk.web.conf.json;

import org.nkk.core.enums.common.BaseEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.util.Objects;

/**
 * <p>描述: 解析对象类型接受枚举参数的转换
 * <p>开发者: dlj
 * <p>时间 2022/6/20 5:53 下午
 */
public class EnumsConvertorFactory implements ConverterFactory<String, BaseEnum> {

    @Override
    public <T extends BaseEnum> Converter<String, T> getConverter(Class<T> targetType) {
        return source ->  {
            for (T t : targetType.getEnumConstants()) {
                if (Objects.equals(String.valueOf(t.value()), source)) {
                    return t;
                }
            }
            return null;
        };
    }
}
