package org.nkk.web.autoconfigure.encrypt;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.nkk.web.autoconfigure.encrypt.annotations.EnableEncrypt;
import org.nkk.web.autoconfigure.encrypt.enums.EncryptMethod;
import org.nkk.web.autoconfigure.encrypt.service.AESEncryptor;
import org.nkk.web.autoconfigure.encrypt.service.DESEncryptor;
import org.nkk.web.autoconfigure.encrypt.service.SM4Encryptor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class EncryptAutoConfigure implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> ans = importingClassMetadata.getAnnotationAttributes(EnableEncrypt.class.getName(), false);
        if (Objects.isNull(ans) || ans.isEmpty()) {
            return;
        }
        String[] profiles = (String[]) ans.get("profile");
        log.info("已启用{}", Arrays.toString(profiles));
        boolean hasProfiles = StreamEx.of(profiles)
                .anyMatch(environment::acceptsProfiles);
        if (!hasProfiles) {
            return;
        }

        EncryptMethod encrypt = (EncryptMethod) ans.get("value");
        log.info("已启用[{}]数据加密,", encrypt.name());


        String iv = environment.getProperty("encrypt.iv");
        String key = environment.getProperty("encrypt.key");
        EncryptProperties ep = new EncryptProperties();
        ep.setKey(StrUtil.isNotEmpty(key) ? key : ep.getKey());
        ep.setIv(StrUtil.isNotEmpty(iv) ? iv : ep.getIv());

        // 注册加密器
        AbstractBeanDefinition beanDefinition = null;
        if(encrypt == EncryptMethod.AES){
            beanDefinition = BeanDefinitionBuilder
                    .genericBeanDefinition(AESEncryptor.class)
                    .addConstructorArgValue(ep)
                    .getBeanDefinition();
            registry.registerBeanDefinition("aESEncryptor",beanDefinition );
        }
        else if(encrypt == EncryptMethod.DES){
            registry.registerBeanDefinition("dESEncryptor", BeanDefinitionBuilder
                    .genericBeanDefinition(DESEncryptor.class)
                    .addConstructorArgValue(ep)
                    .getBeanDefinition());
        }
        else if(encrypt == EncryptMethod.SM4){
            registry.registerBeanDefinition("sM4Encryptor", BeanDefinitionBuilder
                    .genericBeanDefinition(SM4Encryptor.class)
                    .addConstructorArgValue(ep)
                    .getBeanDefinition());
        }
        registry.registerBeanDefinition("encryptResponseBodyAdvice", BeanDefinitionBuilder
                .genericBeanDefinition(EncryptResponseBodyAdvice.class)
                .addConstructorArgValue(beanDefinition)
                .getBeanDefinition());

       log.info("====>");
    }



}
