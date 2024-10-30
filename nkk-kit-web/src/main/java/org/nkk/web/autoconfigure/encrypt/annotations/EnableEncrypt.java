package org.nkk.web.autoconfigure.encrypt.annotations;

import org.nkk.web.autoconfigure.encrypt.EncryptAutoConfigure;
import org.nkk.web.autoconfigure.encrypt.enums.EncryptMethod;
import org.springframework.context.annotation.Import;

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
@Import({EncryptAutoConfigure.class})
public @interface EnableEncrypt {

    /**
     * 加密方式
     */
    EncryptMethod value() default EncryptMethod.AES;

    /**
     * 加密环境,默认只对default环境加密。
     * <p>
     * 项目中一般为多环境，为方便调试只会对正式环境数据进行加密。
     * </P>
     */
    String[] profile() default {"default"};


}
