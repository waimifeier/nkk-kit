package org.nkk.core.annotations.validator;

import org.nkk.core.enums.verify.ConditionType;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 条件分组规则
 */
@Target({FIELD})
@Retention(RUNTIME)
@Documented
@Repeatable(ConditionalGroups.class)
public @interface ConditionalGroup {

    /**
     * 条件值。IN、NOT_IN 可用逗号分隔多个值。
     */
    String value() default "";

    /**
     * 条件值集合。配置后优先于 value。
     */
    String[] values() default {};

    /**
     * 条件类型
     */
    ConditionType condition() default ConditionType.EQUAL;

    /**
     * 条件满足时追加校验的分组
     */
    Class<?>[] groups();
}
