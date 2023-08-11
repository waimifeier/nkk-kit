package org.nkk.web.conf.exception;

import lombok.extern.slf4j.Slf4j;
import org.nkk.core.beans.common.Result;
import org.nkk.core.beans.exception.BusinessException;
import org.nkk.core.enums.fail.FailCodeEnum;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 全局异常错误处理器
 * @author Nkks
 */
@RestControllerAdvice
@ResponseStatus(HttpStatus.OK)
@Order(value = Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class ExceptionsHandler {


    /**
     * 路径不存在异常
     * @param e 找不到处理器异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<Object> handlerNoFoundException(NoHandlerFoundException e) {
        log.error("路径不存在异常：{}",e.getMessage());
        return Result.fail(FailCodeEnum.NOT_FOUND);
    }

    /**
     *  请求方式不支持异常
     */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public Result<Object> httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("请求方式不支持异常：{}",e.getMessage());
        return Result.fail(FailCodeEnum.NOT_SUPPORT_METHODS,String.format("请求方式有误，不支持%s请求",e.getMethod()));
    }

    /**
     *
     * Parameter参数: 缺少参数
     * 标注@RequestParam(required = true)或 @RequestParam注解时 (表示该参数是必须),而请求中，没有带该参数会触发这个异常。
     *
     */
    @ExceptionHandler({MissingServletRequestParameterException.class})
    public Result<Object> missingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("缺少Parameter参数异常：{}",e.getMessage());
        return Result.fail(FailCodeEnum.NOT_FOUND,String.format("缺少参数:%s %s",e.getParameterType(),e.getParameterName()));
    }


    /**
     *
     *  Parameter参数: 类型错误,如Int类型 接受String类型的参数，处理不了就抛出该异常。
     *
     */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public Result<Object> missingServletRequestParameterException(MethodArgumentTypeMismatchException e) {
        log.error("Parameter参数类型有误：{}",e.getMessage());
        return Result.fail(FailCodeEnum.NOT_FOUND,String.format("%s参数类型错误",e.getName()));
    }

    /**
     *
     * Parameter参数: 通过JSR303校验注解校验失败时异常处理
     * <b>需在Controller加上@Validated注解开启 Parameter JSR303参数校验</b>
     *
     */
    @ExceptionHandler({ConstraintViolationException.class})
    public Result<Object> bindException(ConstraintViolationException ex) {
        log.error("Parameter参数Violation校验异常：{}", ex.getMessage());
        List<String> defaultMsg = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        return Result.fail(FailCodeEnum.BAD_REQUEST,defaultMsg.get(0));
    }


       /**
     * 对象参数: 处理校验失败触发异常 【JSR303校验失败，或转换失败触发】
     * <b>需要加上 @Valid 或 @Validated 注解开启入参Bean对象JSR303校验</b>
     */
    @ExceptionHandler({BindException.class})
    public Result<Object> bindException(BindException e) {
        log.error("对象参数绑定失败异常：{}", e.getMessage());
        return getJsonReturn(e.getBindingResult());
    }

    /**
     * JSON参数校验失败: JSR303校验失败触发
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public Result<Object> methodArgumentNotValidException(MethodArgumentNotValidException  e) {
        log.error("JSON参数绑定失败异常：{}", e.getMessage());
        return getJsonReturn(e.getBindingResult());
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public Result<Object> illegalArgumentException(IllegalArgumentException  e){
        log.error("JSON参数绑定失败异常：{}", e.getMessage());
        return Result.fail(FailCodeEnum.BAD_REQUEST, e.getMessage());
    }

    private Result<Object> getJsonReturn(BindingResult bindingResult) {
        return Result.fail(FailCodeEnum.BAD_REQUEST, Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
    }

    /**
     * Json转换异常【JSON格式不对,或数据类型转换失败】
     */
    @ExceptionHandler({HttpMessageNotReadableException.class})
    public Result<Object> httpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("JSON 转换异常：{}", e.getMessage());
        return Result.fail(FailCodeEnum.BAD_REQUEST,"参数转换失败,请检查JSON格式或类型是否正确");
    }

    /**
     * 媒体类型不正确[需要json类型，传入的是一个formData类型]
     */
    @ExceptionHandler({HttpMediaTypeNotSupportedException.class})
    public Result<Object> httpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        log.error("http媒体类型不支持异常：{}", e.getMessage());
        return Result.fail(FailCodeEnum.BAD_REQUEST,"参数媒体类型不支持");
    }


        /**
     * 内置校验器或自定义校验器发出错误时抛出
     */
    @ExceptionHandler({ValidationException.class})
    public Result<Object> validationExceptionException(ValidationException e) {
        log.error("参数验证失败：{}", e.getMessage());
        return Result.fail(FailCodeEnum.BAD_REQUEST,e.getMessage());
    }

    /**
     * 业务异常
     */
    @ExceptionHandler({BusinessException.class})
    public Result<Object> plantException(BusinessException e) {
        log.error("业务异常",e);
        return Result.fail(e.getErrorCode(),e.getMessage());
    }

    /**
     * 兜底异常处理，统一返回异常结果
     */
    @ExceptionHandler({Exception.class})
    public Result<Object> exceptionHandler(Exception e) {
        log.error("全局异常：",e);
        return Result.fail("服务器错误");
    }

}