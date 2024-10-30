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
     * <a href="http://tool.zaonao.cn/password/">秘钥key</a></br>
     *
     *
     */
    private String key = "0CoJUm6Qyw8W8ju=";

    /**
     * 偏移Iv
     */
    private String iv = "SoC2uTD4Mj7bJdGa" ;

}
