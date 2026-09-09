package org.nkk.flow.web.extension;

import cn.hutool.core.util.StrUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.nkk.flow.web.extension.annotation.FlowApproval;
import org.nkk.flow.web.extension.annotation.FlowApproval.ReturnCondition;
import org.nkk.flow.web.extension.annotation.FlowBusinessKey;
import org.nkk.flow.web.extension.annotation.FlowVariables;
import org.nkk.flow.web.model.FlowRuntimeResponse;
import org.nkk.flow.web.model.FlowStartProcessRequest;
import org.nkk.flow.web.service.FlowDesignerRuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link FlowApproval} 自动发起审批切面。
 *
 * <p>拦截标注了 {@code @FlowApproval} 的业务方法，方法执行成功后自动发起审批流程。
 * 默认在事务提交后启动，避免业务数据未持久化时审批引擎读不到 businessKey。</p>
 *
 * <h3>参数提取优先级</h3>
 * <ul>
 *   <li><b>businessKey</b>：{@code @FlowBusinessKey} 参数 → 返回值的 {@code businessKeyFrom} 属性 → 返回值 toString()</li>
 *   <li><b>variables</b>：{@code @FlowVariables} Map 参数 → 返回值的所有可序列化属性</li>
 * </ul>
 *
 * <h3>返回值成功判定</h3>
 * <table>
 *   <tr><th>条件</th><th>触发时机</th></tr>
 *   <tr><td>NON_NULL（默认）</td><td>返回值非 null</td></tr>
 *   <tr><td>TRUE</td><td>boolean/Boolean 返回 true</td></tr>
 *   <tr><td>VOID_OR_NULL</td><td>void 方法或返回 null</td></tr>
 *   <tr><td>ALWAYS</td><td>方法正常返回（无异常）</td></tr>
 * </table>
 *
 * <h3>使用条件</h3>
 * <ul>
 *   <li>starter 已开启 Web 扩展（默认开启），且容器中存在 {@code FlowDesignerRuntimeService}。</li>
 *   <li>需要 Spring AOP 支持，确保 {@code spring-boot-starter-aop} 在依赖中。</li>
 * </ul>
 */
@Aspect
public class FlowApprovalAutoStartAspect {

    private static final Logger log = LoggerFactory.getLogger(FlowApprovalAutoStartAspect.class);

    @Autowired
    private FlowDesignerRuntimeService runtimeService;

    /**
     * 拦截所有标注了 @FlowApproval 的方法。
     */
    @Around("@annotation(annotation)")
    public Object around(ProceedingJoinPoint pjp, FlowApproval annotation) throws Throwable {
        Object result = pjp.proceed();

        if (!isSuccess(result, annotation.successOn())) {
            log.debug("[FlowApproval] 方法未通过成功判定，跳过自动发起审批，method={}", pjp.getSignature().getName());
            return result;
        }

        String businessKey = resolveBusinessKey(pjp, result, annotation);
        Map<String, Object> variables = resolveVariables(pjp, result);

        FlowStartProcessRequest request = buildStartRequest(annotation, businessKey, variables);
        Runnable starter = () -> doStart(request, pjp.getSignature().toShortString());

        if (annotation.afterCommit() && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    starter.run();
                }
            });
            log.debug("[FlowApproval] 已注册事务提交后自动发起审批，processKey={}, businessKey={}",
                    annotation.processKey(), businessKey);
        } else {
            starter.run();
        }

        return result;
    }

    private boolean isSuccess(Object result, ReturnCondition condition) {
        switch (condition) {
            case TRUE:
                return Boolean.TRUE.equals(result);
            case VOID_OR_NULL:
                return result == null;
            case ALWAYS:
                return true;
            case NON_NULL:
            default:
                return result != null;
        }
    }

    private String resolveBusinessKey(ProceedingJoinPoint pjp, Object result, FlowApproval annotation) {
        // 1. 优先取 @FlowBusinessKey 标注的方法参数
        Object businessKeyArg = findAnnotatedArg(pjp, FlowBusinessKey.class);
        if (businessKeyArg != null) {
            return String.valueOf(businessKeyArg);
        }

        // 2. 从返回值属性中提取
        if (StrUtil.isNotBlank(annotation.businessKeyFrom()) && result != null) {
            String value = readProperty(result, annotation.businessKeyFrom());
            if (StrUtil.isNotBlank(value)) {
                return value;
            }
        }

        // 3. 兜底：返回值的 toString
        if (result != null) {
            return result.toString();
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> resolveVariables(ProceedingJoinPoint pjp, Object result) {
        // 1. 优先取 @FlowVariables 标注的 Map 参数
        Object variablesArg = findAnnotatedArg(pjp, FlowVariables.class);
        if (variablesArg instanceof Map) {
            return new LinkedHashMap<>((Map<String, Object>) variablesArg);
        }

        // 2. 从返回值的 JavaBean 属性中收集
        if (result != null && !(result instanceof Map)) {
            return collectProperties(result);
        }

        return Collections.emptyMap();
    }

    private Object findAnnotatedArg(ProceedingJoinPoint pjp, Class<? extends java.lang.annotation.Annotation> annotationType) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Object[] args = pjp.getArgs();
        java.lang.annotation.Annotation[][] paramAnnotations = method.getParameterAnnotations();

        for (int i = 0; i < paramAnnotations.length; i++) {
            for (java.lang.annotation.Annotation ann : paramAnnotations[i]) {
                if (annotationType.isInstance(ann)) {
                    return args[i];
                }
            }
        }
        return null;
    }

    private String readProperty(Object target, String propertyName) {
        try {
            java.beans.BeanInfo info = java.beans.Introspector.getBeanInfo(target.getClass(), Object.class);
            for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                if (propertyName.equals(pd.getName()) && pd.getReadMethod() != null) {
                    Object value = pd.getReadMethod().invoke(target);
                    return value == null ? null : value.toString();
                }
            }
        } catch (Exception ignored) {
            // introspector 异常兜底
        }
        // 兜底：Map 直接 get
        if (target instanceof Map) {
            Object value = ((Map<?, ?>) target).get(propertyName);
            return value == null ? null : value.toString();
        }
        return null;
    }

    private Map<String, Object> collectProperties(Object target) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            java.beans.BeanInfo info = java.beans.Introspector.getBeanInfo(target.getClass(), Object.class);
            for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                if (pd.getReadMethod() == null || "class".equals(pd.getName())) {
                    continue;
                }
                Object value = pd.getReadMethod().invoke(target);
                if (value != null) {
                    result.put(pd.getName(), value);
                }
            }
        } catch (Exception ignored) {
            // introspector 异常时返回空
        }
        if (target instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) target).entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    result.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
        }
        return result;
    }

    private FlowStartProcessRequest buildStartRequest(FlowApproval annotation, String businessKey,
                                                      Map<String, Object> variables) {
        FlowStartProcessRequest request = new FlowStartProcessRequest();
        request.setProcessId(annotation.processId() > 0 ? annotation.processId() : null);
        request.setProcessKey(StrUtil.isNotBlank(annotation.processKey()) ? annotation.processKey() : null);
        request.setProcessVersion(annotation.processVersion() > 0 ? annotation.processVersion() : null);
        request.setBusinessKey(businessKey);
        request.setVariables(variables);
        return request;
    }

    private void doStart(FlowStartProcessRequest request, String methodSignature) {
        try {
            FlowRuntimeResponse response = runtimeService.start(request);
            log.info("[FlowApproval] ✅ 自动发起审批成功，method={}, processKey={}, businessKey={}, instanceId={}",
                    methodSignature, request.getProcessKey(), request.getBusinessKey(), response.getInstanceId());
        } catch (Exception e) {
            log.error("[FlowApproval] ❌ 自动发起审批失败，method={}, processKey={}, businessKey={}",
                    methodSignature, request.getProcessKey(), request.getBusinessKey(), e);
            // 不重新抛出，避免影响业务方法的正常返回
        }
    }
}
