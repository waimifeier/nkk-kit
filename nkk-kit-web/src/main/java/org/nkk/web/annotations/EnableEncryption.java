package org.nkk.web.annotations;

import org.springframework.context.annotation.Configuration;

import java.lang.annotation.*;

/**
 * 启用接口加密
 *
 * @author nkk
 * @date 2023/11/30
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Configuration
public @interface EnableEncryption {

}
