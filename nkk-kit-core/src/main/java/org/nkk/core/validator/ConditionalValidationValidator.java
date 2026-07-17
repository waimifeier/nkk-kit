package org.nkk.core.validator;

import org.apache.commons.lang3.StringUtils;
import org.nkk.core.annotations.validator.ConditionalGroup;
import org.nkk.core.annotations.validator.ConditionalValidation;
import org.nkk.core.enums.verify.ConditionType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 条件分组校验器
 */
public class ConditionalValidationValidator implements ConstraintValidator<ConditionalValidation, Object> {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private ConditionalValidation conditionalValidation;

    @Override
    public void initialize(ConditionalValidation conditionalValidation) {
        this.conditionalValidation = conditionalValidation;
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        if (Objects.isNull(object)) {
            return true;
        }

        Class<?>[] groups = getMatchedGroups(object);
        if (groups.length == 0) {
            return true;
        }

        Set<ConstraintViolation<Object>> violations = VALIDATOR.validate(object, groups);
        if (violations.isEmpty()) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        for (ConstraintViolation<Object> violation : violations) {
            addViolation(context, violation);
        }
        return false;
    }

    private Class<?>[] getMatchedGroups(Object object) {
        Set<Class<?>> groups = new LinkedHashSet<>();

        for (Field field : getAllFields(object.getClass())) {
            ConditionalGroup[] conditionalGroups = field.getAnnotationsByType(ConditionalGroup.class);
            for (ConditionalGroup conditionalGroup : conditionalGroups) {
                if (matches(object, conditionalGroup, field)) {
                    groups.addAll(Arrays.asList(conditionalGroup.groups()));
                }
            }
        }

        return groups.stream()
                .filter(Objects::nonNull)
                .filter(group -> !Default.class.equals(group))
                .toArray(Class<?>[]::new);
    }

    private boolean matches(Object object, ConditionalGroup conditionalGroup, Field field) {
        Object actualValue = getFieldValue(object, field);
        List<String> expectedValues = getExpectedValues(conditionalGroup);
        return compare(actualValue, expectedValues, conditionalGroup.condition());
    }

    private Object getFieldValue(Object object, Field field) {
        if (Objects.isNull(object) || Objects.isNull(field)) {
            return null;
        }

        try {
            field.setAccessible(true);
            return field.get(object);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> searchType = clazz;
        while (Objects.nonNull(searchType) && !Object.class.equals(searchType)) {
            fields.addAll(Arrays.asList(searchType.getDeclaredFields()));
            searchType = searchType.getSuperclass();
        }
        return fields;
    }

    private List<String> getExpectedValues(ConditionalGroup conditionalGroup) {
        if (conditionalGroup.values().length > 0) {
            return Arrays.asList(conditionalGroup.values());
        }

        if (StringUtils.isBlank(conditionalGroup.value())) {
            return new ArrayList<>();
        }

        return Arrays.stream(conditionalGroup.value().split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    private boolean compare(Object actualValue, List<String> expectedValues, ConditionType conditionType) {
        switch (conditionType) {
            case IS_NULL:
                return Objects.isNull(actualValue);
            case NOT_NULL:
                return Objects.nonNull(actualValue);
            case BLANK:
                return Objects.isNull(actualValue) || StringUtils.isBlank(String.valueOf(actualValue));
            case NOT_BLANK:
                return Objects.nonNull(actualValue) && StringUtils.isNotBlank(String.valueOf(actualValue));
            default:
                if (Objects.isNull(actualValue)) {
                    return false;
                }
        }

        String actual = String.valueOf(actualValue);
        String expected = expectedValues.isEmpty() ? "" : expectedValues.get(0);

        switch (conditionType) {
            case EQUAL:
                return Objects.equals(actual, expected);
            case NOT_EQUAL:
                return !Objects.equals(actual, expected);
            case GREATER_THAN:
                return compareNumber(actual, expected) > 0;
            case GREATER_THAN_OR_EQUAL:
                return compareNumber(actual, expected) >= 0;
            case LESS_THAN:
                return compareNumber(actual, expected) < 0;
            case LESS_THAN_OR_EQUAL:
                return compareNumber(actual, expected) <= 0;
            case CONTAINS:
                return actual.contains(expected);
            case STARTS_WITH:
                return actual.startsWith(expected);
            case ENDS_WITH:
                return actual.endsWith(expected);
            case IN:
                return expectedValues.contains(actual);
            case NOT_IN:
                return !expectedValues.contains(actual);
            default:
                return false;
        }
    }

    private int compareNumber(String actual, String expected) {
        try {
            return new BigDecimal(actual).compareTo(new BigDecimal(expected));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void addViolation(ConstraintValidatorContext context, ConstraintViolation<Object> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        ConstraintValidatorContext.ConstraintViolationBuilder builder =
                context.buildConstraintViolationWithTemplate(violation.getMessage());

        if (StringUtils.isBlank(propertyPath)) {
            builder.addConstraintViolation();
            return;
        }

        builder.addPropertyNode(propertyPath).addConstraintViolation();
    }
}
