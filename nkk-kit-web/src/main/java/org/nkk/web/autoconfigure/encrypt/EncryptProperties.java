package org.nkk.web.autoconfigure.encrypt;

import lombok.Data;
import org.nkk.web.autoconfigure.encrypt.enums.EncryptMethod;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 加密配置参数
 */
@Data
@ConfigurationProperties(prefix = "encrypt")
public class EncryptProperties {

    /**
     * 作用环境，默认作用于default环境
     */
    private String[] profiles = { "default" };

    /**
     * 加密方法
     * AES, DES, SM4, RSA
     */
    private EncryptMethod encrypt = EncryptMethod.AES;


    /**
     *不同加密方式，key长度略有不同 <a href="http://tool.zaonao.cn/password/">生成秘钥key</a></br>
     * @see org.nkk.web.autoconfigure.encrypt.enums.EncryptMethod
     */
    private String key = "0CoJUm6Qyw8W8ju=";

    /**
     * 偏移Iv
     */
    private String iv = "zvckzf823j6hkr8t";

}
