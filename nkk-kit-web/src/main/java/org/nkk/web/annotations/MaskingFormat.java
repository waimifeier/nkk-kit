package org.nkk.web.annotations;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.nkk.conf.serializer.JacksonMaskingSerializer;
import org.nkk.core.enums.verify.MaskingMethod;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 数据脱敏规则处理 序列化注解
 */
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = JacksonMaskingSerializer.class)
public @interface MaskingFormat {

    /**
     * 脱敏方式
     */
    MaskingMethod method() default MaskingMethod.FIRST_MASK;

    /**
     * 用什么打码
     */
    String symbol() default "*";


}