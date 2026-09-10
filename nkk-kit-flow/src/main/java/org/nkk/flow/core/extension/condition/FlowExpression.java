package org.nkk.flow.core.extension.condition;

import org.nkk.flow.model.node.router.FlowCondition;

import java.util.List;
import java.util.Map;

/**
 * 流程条件表达式。
 */
public interface FlowExpression {

    /**
     * 求值分支条件组。
     *
     * @param conditionGroups 条件组列表，组间 OR，组内条件 AND
     * @param args 流程变量
     * @return true 表示分支命中
     */
    boolean eval(List<List<FlowCondition>> conditionGroups, Map<String, Object> args);
}

