package org.nkk.flow.core.extension.condition;

import org.nkk.flow.model.FlowCondition;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 默认条件表达式，支持 eq、ne、gt、ge、lt、le。
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

