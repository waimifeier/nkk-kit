package org.nkk.web.autoconfigure.encrypt;

import lombok.extern.slf4j.Slf4j;
import org.nkk.web.autoconfigure.encrypt.enums.EncryptMethod;
import org.nkk.web.autoconfigure.encrypt.service.AESEncryptor;
import org.nkk.web.autoconfigure.encrypt.service.DESEncryptor;
import org.nkk.web.autoconfigure.encrypt.service.Encryptor;
import org.nkk.web.autoconfigure.encrypt.service.SM4Encryptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


@Slf4j
@EnableConfigurationProperties({EncryptProperties.class})
public class EncryptAutoConfigure {


    /**
     * 加密器
     *
     * @param encryptProperties 加密配置
     * @return Encryptor
     */
    @Bean
    public Encryptor encryptor(EncryptProperties encryptProperties) {
        EncryptMethod encrypt = encryptProperties.getEncrypt();
        if (encrypt == EncryptMethod.AES) {
            return new AESEncryptor(encryptProperties);
        } else if (encrypt == EncryptMethod.DES) {
            return new DESEncryptor(encryptProperties);
        } else if (encrypt == EncryptMethod.SM4) {
            return new SM4Encryptor(encryptProperties);
        }
        return null;
    }
}
