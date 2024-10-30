package org.nkk.web.autoconfigure.encrypt.annotations;

import java.lang.annotation.*;

/**
 * 忽略数据加密，适用于controller类上或者方法上
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface EncryptIgnore {
}
