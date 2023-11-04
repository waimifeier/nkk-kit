package org.nkk.web.conf.json;

import one.util.streamex.StreamEx;
import org.nkk.core.enums.common.BaseEnum;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Objects;
import java.util.stream.Stream;


/**
 * <p>描述: 处理Query参数和Form-Data参数枚举类型转换
 * <p>开发者: dlj
 * <p>时间 2022/6/21 9:29 上午
 */
public class EnumQuery implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        Class<?> parameterType = methodParameter.getParameterType();
        return parameterType.isEnum() && BaseEnum.class.isAssignableFrom(parameterType);
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest webRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {

        Class<?> parameterClass = methodParameter.getParameterType();

        String parameterName = methodParameter.getParameterName();
        if (!StringUtils.hasText(parameterName)) {
            return null;
        }

        String parameterValue = webRequest.getParameter(parameterName);
        BaseEnum[] enumConstants = (BaseEnum[]) parameterClass.getEnumConstants();
        return StreamEx.of(enumConstants)
                .findFirst(item -> Objects.equals(String.valueOf(item.value()), parameterValue))
                .orElse(null);
    }
}
