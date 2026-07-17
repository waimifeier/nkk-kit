package org.nkk.core.annotations.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 条件分组规则容器
 */
@Target({FIELD})
@Retention(RUNTIME)
@Documented
public @interface ConditionalGroups {

    ConditionalGroup[] value();
}
