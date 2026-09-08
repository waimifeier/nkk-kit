package org.nkk.flow.web.extension.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记方法参数为审批 businessKey。
 *
 * <p>配合 {@link FlowApproval} 使用，标注在业务 Service 方法的某个参数上，
 * 该参数的值就是审批引擎关联业务单据的 businessKey。</p>
 *
 * <pre>{@code
 * @FlowApproval(processKey = "leave")
 * public void submitLeave(@FlowBusinessKey String orderNo, LeaveForm form) { ... }
 * }</pre>
 *
 * <p>参数类型任意，切面会调用 {@code toString()} 取值。</p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface FlowBusinessKey {
}
