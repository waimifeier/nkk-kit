package org.nkk.web.autoconfigure.encrypt.annotations;

import org.nkk.web.autoconfigure.encrypt.EncryptAutoConfigure2;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用接口加密
 *
 * @author nkk
 * @date 2024/11/30
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Import({EncryptAutoConfigure2.class})
public @interface EnableEncrypt {


}
