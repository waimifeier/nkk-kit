package org.nkk.web.autoconfigure.encrypt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 加密配置参数
 */
@Data
@ConfigurationProperties(prefix = "encrypt")
public class EncryptProperties {


    /**
     * <a href="http://tool.zaonao.cn/password/">生成秘钥key</a></br>
     */
    private String key = "0CoJUm6Qyw8W8ju=";

    /**
     * 偏移Iv
     */
    private String iv = "zvckzf823j6hkr8t";

}
