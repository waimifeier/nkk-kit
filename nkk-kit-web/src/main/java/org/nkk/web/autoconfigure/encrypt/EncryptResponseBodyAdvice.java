package org.nkk.web.autoconfigure.encrypt;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.nkk.core.beans.common.Result;
import org.nkk.web.autoconfigure.encrypt.annotations.EncryptIgnore;
import org.nkk.web.autoconfigure.encrypt.service.Encryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;


/**
 * 响应数据的加密处理<br>
 */
@ControllerAdvice
@Slf4j
@EnableConfigurationProperties(EncryptProperties.class)
public class EncryptResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;
    private final Encryptor encryptor;

    @Autowired
    public EncryptResponseBodyAdvice(ObjectMapper objectMapper,
                                     EncryptProperties encryptProperties,
                                     Encryptor encryptor) {
        this.objectMapper = objectMapper;
        this.encryptor = encryptor;
    }


    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> declaringClass = returnType.getDeclaringClass();
        if (this.hasIgnoreAnnotation(declaringClass)) {
            return false;
        }
        Method method = returnType.getMethod();
        if (method != null) {
            Class<?> returnValueType = method.getReturnType();
            boolean ignore = this.hasIgnoreAnnotation(method) || this.hasIgnoreAnnotation(returnValueType);
            if (ignore) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否有忽略加密的注解
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
