package org.nkk.core.annotations.validator;

import org.nkk.core.validator.ConditionalValidationValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 条件分组校验。
 *
 * <pre>
 * &#64;ConditionalValidation
 * public class Request {
 *     &#64;ConditionalGroup(value = "1", groups = TypeOne.class)
 *     &#64;ConditionalGroup(value = "2", groups = TypeTwo.class)
 *     private Integer type;
 *
 *     &#64;NotBlank(groups = TypeOne.class)
 *     private String fieldA;
 *
 *     &#64;NotBlank(groups = TypeTwo.class)
 *     private String fieldB;
 * }
 * </pre>
 */
@Target({TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = ConditionalValidationValidator.class)
public @interface ConditionalValidation {

    String message() default "条件分组校验失败";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
