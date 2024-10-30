package org.nkk.web.autoconfigure.encrypt;

import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.nkk.web.autoconfigure.encrypt.annotations.EnableEncrypt;
import org.nkk.web.autoconfigure.encrypt.enums.EncryptMethod;
import org.nkk.web.autoconfigure.encrypt.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

@Slf4j
public class EncryptAutoConfigure implements ImportBeanDefinitionRegistrar {

    private final Environment environment;

    public EncryptAutoConfigure(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public Encryptor encryptor() {
        // 可添加更多加密方式判断
        return null;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> ans = importingClassMetadata.getAnnotationAttributes(EnableEncrypt.class.getName(), false);
        assert ans != null;
        if (ans.isEmpty()) {
            return;
        }
        String[] profiles = (String[]) ans.get("profile");


        boolean hasProfiles = StreamEx.of(profiles)
                .anyMatch(environment::acceptsProfiles);

        if (!hasProfiles) {
            return;
        }

        EncryptMethod encrypt = (EncryptMethod) ans.get("value");
        log.info("已启用[{}]数据加密,", encrypt.name());

        // 注册bean定义信息
        BeanDefinition erb = new GenericBeanDefinition();
        erb.setBeanClassName(EncryptResponseBodyAdvice.class.getName());
        erb.setScope(BeanDefinition.SCOPE_SINGLETON);
        registry.registerBeanDefinition("encryptResponseBodyAdvice", erb);

        // 注册加密器
        BeanDefinition bd = new GenericBeanDefinition();
        bd.setScope(BeanDefinition.SCOPE_SINGLETON);
        if(encrypt == EncryptMethod.AES){
            bd.setBeanClassName(AESEncryptor.class.getName());
            registry.registerBeanDefinition("aESEncryptor", bd);
        }
        else if(encrypt == EncryptMethod.DES){
            bd.setBeanClassName(DESEncryptor.class.getName());
            registry.registerBeanDefinition("dESEncryptor", bd);
        }
        else if(encrypt == EncryptMethod.SM4){
            bd.setBeanClassName(SM4Encryptor.class.getName());
            registry.registerBeanDefinition("sM4Encryptor", bd);
        }

    }


}
