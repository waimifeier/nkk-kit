package org.nkk.flow.core.extension.condition;

import org.nkk.flow.model.FlowCondition;

import java.util.List;
import java.util.Map;

/**
 * 流程条件表达式。
 */
public interface FlowExpression {

    boolean eval(List<FlowCondition> conditions, Map<String, Object> args);
}

