package org.nkk.flow.core.extension.condition;

import org.nkk.flow.model.node.router.FlowCondition;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 默认条件表达式，支持 eq、ne、gt、ge、lt、le、contains、notContains。
 * <p>条件组语义：组间 OR（任一组命中即命中），组内条件 AND（组内全部满足才算命中）。</p>
 */
public class SimpleFlowExpression implements FlowExpression {

    @Override
    public boolean eval(List<List<FlowCondition>> conditionGroups, Map<String, Object> args) {
        if (conditionGroups == null || conditionGroups.isEmpty()) {
            return true;
        }
        for (List<FlowCondition> group : conditionGroups) {
            if (group == null || group.isEmpty()) {
                continue;
            }
            boolean matched = true;
            for (FlowCondition condition : group) {
                if (!match(condition, args)) {
                    matched = false;
                    break;
                }
            }
            if (matched) {
                return true;
            }
        }
        return false;
    }

    private boolean match(FlowCondition condition, Map<String, Object> args) {
        Object actual = resolveValue(args, condition.getField());
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

    /**
     * 从流程变量中取值，兼容驼峰/下划线两种命名格式。
     * <p>条件配置中可能用数据库字段名（order_code）或实体类属性名（orderCode），
     * 此方法依次尝试原始 key、驼峰、下划线三种形式查找。</p>
     */
    private Object resolveValue(Map<String, Object> args, String field) {
        if (args == null || field == null) {
            return null;
        }
        Object value = args.get(field);
        if (value != null) {
            return value;
        }
        // 尝试下划线 → 驼峰
        String camel = toCamelCase(field);
        if (!camel.equals(field)) {
            value = args.get(camel);
            if (value != null) {
                return value;
            }
        }
        // 尝试驼峰 → 下划线
        String snake = toSnakeCase(field);
        if (!snake.equals(field)) {
            value = args.get(snake);
        }
        return value;
    }

    private static String toSnakeCase(String camel) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) sb.append('_');
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String toCamelCase(String snake) {
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (int i = 0; i < snake.length(); i++) {
            char c = snake.charAt(i);
            if (c == '_') {
                upper = true;
            } else if (upper) {
                sb.append(Character.toUpperCase(c));
                upper = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}

