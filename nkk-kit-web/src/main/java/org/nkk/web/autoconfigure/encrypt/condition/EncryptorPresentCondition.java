package org.nkk.web.autoconfigure.encrypt.condition;

import org.nkk.web.autoconfigure.encrypt.EncryptProperties;
import org.nkk.web.autoconfigure.encrypt.annotations.EnableEncrypt;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Arrays;
import java.util.Objects;
//@Order(Integer.MAX_VALUE)
public class EncryptorPresentCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();
        String[] activeProfiles = environment.getActiveProfiles();
        // encrypt.profiles
        String property = environment.getProperty("encrypt.profiles.length");
        System.out.println("当前环境变量："+ Arrays.toString(activeProfiles) + " encryptProperties: "+ property);

        String[] beanNamesForAnnotation = Objects.requireNonNull(context.getBeanFactory())
                .getBeanNamesForAnnotation(EnableEncrypt.class);
        return Arrays.stream(beanNamesForAnnotation).anyMatch(Objects::nonNull);
    }
}
