package org.nkk.web.conf.deSerializer;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.lang3.StringUtils;
import org.nkk.core.beans.exception.BusinessException;
import org.nkk.core.enums.fail.FailCodeEnum;
import org.nkk.core.utils.ParseDateUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

/**
* jackson 日期格式化
* @author Nkks
* @class JacksonDateDeSerializer
* @time 2022/1/27 14:22
*/
public class JacksonDateDeSerializer extends JsonDeserializer<Date> {

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        Date targetDate = null;
        String text = p.getText();
        if(!StringUtils.isBlank(text)){
            try {
                targetDate = ParseDateUtils.parseDateWithException(text);
            }catch (ParseException pe) {
                try {
                    long longDate = Long.parseLong(text.trim());
                    targetDate = new Date(longDate);
                } catch (NumberFormatException e) {
                    String errorMsg = String.format("'%s' 不能转换为类型 'java.util.Date', 只支持时间戳和以下类型 【'%s'】", text, StringUtils.join(ParseDateUtils.parsePatterns, ","));
                    throw BusinessException.fail(FailCodeEnum.BAD_REQUEST,errorMsg);
                }
            }
        }
        return targetDate;
    }

    @Override
    public Class<?> handledType() {
        return Date.class;
    }
}
