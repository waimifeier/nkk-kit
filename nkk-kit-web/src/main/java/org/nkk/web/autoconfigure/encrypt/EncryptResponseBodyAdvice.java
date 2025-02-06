package org.nkk.web.autoconfigure.encrypt;

import cn.hutool.core.util.ArrayUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.nkk.core.beans.common.Result;
import org.nkk.web.autoconfigure.encrypt.annotations.EncryptIgnore;
import org.nkk.web.autoconfigure.encrypt.service.Encryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Objects;


/**
 * 响应数据的加密处理<br>
 */
@Slf4j
@ControllerAdvice
public class EncryptResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    private final ObjectMapper objectMapper;
    private final EncryptProperties encryptProperties;
    private final Encryptor encryptor;
    private final Environment environment;

    public EncryptResponseBodyAdvice(ObjectMapper objectMapper,
                                     Environment environment,
                                     @Autowired(required = false) EncryptProperties encryptProperties,
                                     @Autowired(required = false) Encryptor encryptor) {
        this.objectMapper = objectMapper;
        this.encryptProperties = encryptProperties;
        this.encryptor = encryptor;
        this.environment = environment;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (Objects.isNull(encryptor) || Objects.isNull(encryptProperties)) {
            return false;
        }
        String[] scopeProfiles = encryptProperties.getProfiles();
        if (scopeProfiles.length == 0) {
            return false;
        }

        String[] activeProfiles = environment.getActiveProfiles();
        boolean exists = ArrayUtil.containsAny(scopeProfiles, activeProfiles);
        if (!exists) {
            return false;
        }

        Class<?> declaringClass = returnType.getDeclaringClass();
        if (this.hasIgnoreAnnotation(declaringClass)) {
            return false;
        }
        Method method = returnType.getMethod();
        if (method != null) {
            Class<?> returnValueType = method.getReturnType();
            boolean ignore = this.hasIgnoreAnnotation(method) || this.hasIgnoreAnnotation(returnValueType);
            return !ignore;
        }
        return true;
    }

    /**
     * 判断给定的注解元素是否有忽略加密的注解
     *
     * @param annotatedElement 注解元素
     * @return 如果注解元素上有 {@link EncryptIgnore} 注解，则返回 true；否则返回 false
     */
    private boolean hasIgnoreAnnotation(AnnotatedElement annotatedElement) {
        if (annotatedElement == null) {
            return false;
        }
        return annotatedElement.isAnnotationPresent(EncryptIgnore.class);
    }


    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter methodParameter,
                                  MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        // JSON
        if (mediaType.includes(MediaType.APPLICATION_JSON)) {
            try {
                // 一定要调用，否者就绕过了自定义Jackson的序列化属性值
                String json = objectMapper.writeValueAsString(body);
                // 如果需要可以按照原本数据格式，将加密后封装进去的数据返回
                // Object encryptData = objectMapper.readValue("{\"code\":200,\"msg\":\"成功\",\"data\":\"机密数据..\"}", methodParameter.getParameterType());
                String enx = encryptor.encryptBase64(json);
                return Result.ok(enx, "OK");
            } catch (Exception ex) {
                log.error("encryptor处理失败：{}", ex.getMessage());
                return Result.fail(ex.getMessage());
            }
        }
        // 文本
        else if (mediaType.includes(MediaType.TEXT_PLAIN)) {
            return encryptor.encryptBase64(body.toString());
        }
        return body;
    }
}
