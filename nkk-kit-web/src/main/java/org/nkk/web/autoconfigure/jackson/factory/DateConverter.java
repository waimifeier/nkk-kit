package org.nkk.web.autoconfigure.jackson.factory;

import org.nkk.web.autoconfigure.jackson.deSerializer.JacksonDateDeSerializer;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

/**
 * Spring MVC 参数日期转换器，处理 query、path、formData 等字符串参数。
 */
public class DateConverter implements ConditionalGenericConverter {

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, Date.class));
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        DateTimeFormat dateTimeFormat = targetType.getAnnotation(DateTimeFormat.class);
        return dateTimeFormat == null || !dateTimeFormat.pattern().trim().isEmpty();
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        DateTimeFormat dateTimeFormat = targetType.getAnnotation(DateTimeFormat.class);
        String text = source == null ? null : source.toString();

        if (dateTimeFormat != null && !dateTimeFormat.pattern().trim().isEmpty()) {
            return JacksonDateDeSerializer.parseDate(text, dateTimeFormat.pattern());
        }

        return JacksonDateDeSerializer.parseDate(text);
    }
}
