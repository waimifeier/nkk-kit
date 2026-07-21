package org.nkk.web.autoconfigure.jackson.deSerializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.nkk.core.beans.exception.BusinessException;
import org.nkk.core.enums.common.SysStatusEnum;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

/**
 * 日期反序列规则
 * <p>根据来自前端的参数个是自动匹配，如：yyy-mm 能自动解析成日期对象</p>
 * 可继续改造...
 *
 */
public class JacksonDateDeSerializer extends JsonDeserializer<Date> implements ContextualDeserializer {
    /**
     *  支持转换的日期格式
     */
    public static String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    private final String pattern;

    public JacksonDateDeSerializer() {
        this(null);
    }

    public JacksonDateDeSerializer(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        Date targetDate = null;
        String text = p.getText();
        if(!StringUtils.isBlank(text)){
            if (StringUtils.isNotBlank(pattern)) {
                return parseDateByPattern(text, pattern);
            }

            try {
                targetDate = this.parseDateWithException(text);
            }catch (ParseException pe) {
                try {
                    long longDate = Long.parseLong(text.trim());
                    targetDate = new Date(longDate);
                } catch (NumberFormatException e) {
                    String errorMsg = String.format("'%s' 不能转换为类型 'java.util.Date', 只支持时间戳和以下类型 【'%s'】", text, StringUtils.join(parsePatterns, ","));
                    throw new BusinessException(SysStatusEnum.BAD_REQUEST,errorMsg);
                }
            }
        }
        return targetDate;
    }

    @Override
    public Class<?> handledType() {
        return Date.class;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctx, BeanProperty property) throws JsonMappingException {
        if (property == null) {
            return this;
        }

        DateTimeFormat dateTimeFormat = property.getAnnotation(DateTimeFormat.class);
        if (dateTimeFormat == null) {
            dateTimeFormat = property.getContextAnnotation(DateTimeFormat.class);
        }

        if (dateTimeFormat == null || StringUtils.isBlank(dateTimeFormat.pattern())) {
            return this;
        }

        return new JacksonDateDeSerializer(dateTimeFormat.pattern());
    }

    private Date parseDateByPattern(String text, String pattern) {
        try {
            return DateUtils.parseDate(text, pattern);
        } catch (ParseException e) {
            String errorMsg = String.format("'%s' 不能转换为类型 'java.util.Date', 只支持 @DateTimeFormat 指定的日期格式 【'%s'】", text, pattern);
            throw new BusinessException(SysStatusEnum.BAD_REQUEST, errorMsg);
        }
    }

    private Date parseDateWithException(Object str) throws ParseException {
        if (str == null){
            return null;
        }
        return DateUtils.parseDate(str.toString(), parsePatterns);
    }
}
