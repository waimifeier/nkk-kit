package org.nkk.web.autoconfigure.jackson.serializer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 日期序列化规则
 */
public class JacksonDateSerializer extends JsonSerializer<Date> implements ContextualSerializer {

    public static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private final String pattern;

    private final String timezone;

    private final String locale;

    public JacksonDateSerializer() {
        this(DEFAULT_PATTERN, null, null);
    }

    public JacksonDateSerializer(String pattern, String timezone, String locale) {
        this.pattern = StringUtils.defaultIfBlank(pattern, DEFAULT_PATTERN);
        this.timezone = timezone;
        this.locale = locale;
    }

    @Override
    public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        gen.writeString(createDateFormat().format(value));
    }

    @Override
    public Class<Date> handledType() {
        return Date.class;
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property == null) {
            return this;
        }

        JsonFormat jsonFormat = property.getAnnotation(JsonFormat.class);
        if (jsonFormat == null) {
            jsonFormat = property.getContextAnnotation(JsonFormat.class);
        }

        if (jsonFormat == null) {
            return this;
        }

        return new JacksonDateSerializer(jsonFormat.pattern(), jsonFormat.timezone(), jsonFormat.locale());
    }

    private SimpleDateFormat createDateFormat() {
        SimpleDateFormat dateFormat = isDefaultJsonFormatValue(locale)
                ? new SimpleDateFormat(pattern)
                : new SimpleDateFormat(pattern, Locale.forLanguageTag(locale.replace('_', '-')));

        if (!isDefaultJsonFormatValue(timezone) && !JsonFormat.DEFAULT_TIMEZONE.equals(timezone)) {
            dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        }
        return dateFormat;
    }

    private boolean isDefaultJsonFormatValue(String value) {
        return StringUtils.isBlank(value) || "##default".equals(value);
    }
}
