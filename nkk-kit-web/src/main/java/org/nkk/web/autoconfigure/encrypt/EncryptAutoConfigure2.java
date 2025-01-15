package org.nkk.web.autoconfigure.encrypt;

import lombok.extern.slf4j.Slf4j;
import org.nkk.web.autoconfigure.encrypt.enums.EncryptMethod;
import org.nkk.web.autoconfigure.encrypt.service.AESEncryptor;
import org.nkk.web.autoconfigure.encrypt.service.DESEncryptor;
import org.nkk.web.autoconfigure.encrypt.service.Encryptor;
import org.nkk.web.autoconfigure.encrypt.service.SM4Encryptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@Slf4j
@EnableConfigurationProperties({EncryptProperties.class})
public class EncryptAutoConfigure2/* implements EnvironmentAware*/ {

/*
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
*/


    /**
     * 加密器
     *
     * @param encryptProperties 加密配置
     * @return Encryptor
     */
    @Bean
    public Encryptor encryptor(EncryptProperties encryptProperties,Environment environment) {
        System.out.println("========== 加密配置 ==========,event:"+ Arrays.toString(environment.getActiveProfiles()));
        System.out.println("event:"+ Arrays.toString(environment.getProperty("encrypt.profiles", String[].class)));
        EncryptMethod encrypt = encryptProperties.getEncrypt();
        System.out.println("加密方式：" + encrypt);
        if (encrypt == EncryptMethod.AES) {
            return new AESEncryptor(encryptProperties);
        }else if (encrypt == EncryptMethod.DES) {
            return new DESEncryptor(encryptProperties);
        } else if (encrypt == EncryptMethod.SM4) {
            return new SM4Encryptor(encryptProperties);
        }
        return null;
    }


}
