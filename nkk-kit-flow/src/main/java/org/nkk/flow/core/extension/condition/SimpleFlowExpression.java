package org.nkk.flow.core.extension.condition;

import org.nkk.flow.model.FlowCondition;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 默认条件表达式，支持 eq、ne、gt、ge、lt、le、contains、notContains。
 */
public class SimpleFlowExpression implements FlowExpression {

    @Override
    public boolean eval(List<FlowCondition> conditions, Map<String, Object> args) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }
        for (FlowCondition condition : conditions) {
            if (!match(condition, args)) {
                return false;
            }
        }
        return true;
    }

    private boolean match(FlowCondition condition, Map<String, Object> args) {
        Object actual = args == null ? null : args.get(condition.getField());
        String operator = condition.getOperator() == null ? "eq" : condition.getOperator();
        Object expected = condition.getValue();
        if ("eq".equalsIgnoreCase(operator) || "=".equals(operator)) {
            return Objects.equals(stringValue(actual), stringValue(expected));
        }
        if ("ne".equalsIgnoreCase(operator) || "!=".equals(operator)) {
            return !Objects.equals(stringValue(actual), stringValue(expected));
        }
        if ("contains".equalsIgnoreCase(operator)) {
            return contains(actual, expected);
        }
        if ("notContains".equalsIgnoreCase(operator)) {
            return !contains(actual, expected);
        }
        BigDecimal left = number(actual);
        BigDecimal right = number(expected);
        if (left == null || right == null) {
            return false;
        }
        int compare = left.compareTo(right);
        if ("gt".equalsIgnoreCase(operator) || ">".equals(operator)) {
            return compare > 0;
        }
        if ("ge".equalsIgnoreCase(operator) || ">=".equals(operator)) {
            return compare >= 0;
        }
        if ("lt".equalsIgnoreCase(operator) || "<".equals(operator)) {
            return compare < 0;
        }
        if ("le".equalsIgnoreCase(operator) || "<=".equals(operator)) {
            return compare <= 0;
        }
        return false;
    }

    /**
     * 判断 actual 是否包含 expected。
     * <p>actual 为集合/数组时，判断 expected 是否在集合中；actual 为字符串时，按子串匹配。</p>
     */
    private boolean contains(Object actual, Object expected) {
        if (actual == null || expected == null) {
            return false;
        }
        if (actual instanceof Collection) {
            return ((Collection<?>) actual).contains(expected);
        }
        if (actual.getClass().isArray()) {
            int len = java.lang.reflect.Array.getLength(actual);
            Object expectedValue = expected;
            for (int i = 0; i < len; i++) {
                Object element = java.lang.reflect.Array.get(actual, i);
                if (Objects.equals(element, expectedValue)) {
                    return true;
                }
            }
            return false;
        }
        return String.valueOf(actual).contains(String.valueOf(expected));
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private BigDecimal number(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

