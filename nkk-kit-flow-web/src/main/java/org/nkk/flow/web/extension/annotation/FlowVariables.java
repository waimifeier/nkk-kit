package org.nkk.flow.web.extension.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记方法参数为审批流程变量 Map。
 *
 * <p>配合 {@link FlowApproval} 使用，标注在业务 Service 方法的某个参数上。
 * 该参数必须是 {@code Map<String, Object>} 类型，其内容会作为流程变量传入审批引擎。</p>
 *
 * <pre>{@code
 * @FlowApproval(processKey = "leave")
 * public void submitLeave(@FlowBusinessKey String orderNo,
 *                         @FlowVariables Map<String, Object> variables) { ... }
 * }</pre>
 *
 * <p>如果没有标注此注解，切面会尝试把返回值的可序列化属性收集为流程变量。</p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface FlowVariables {
}
