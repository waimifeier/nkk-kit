package org.nkk.flow.enums.node;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nkk.core.enums.common.IEnum;

/**
 * 流程模型节点类型。
 *
 * <p>该枚举描述流程设计器中的节点类型，只用于 {@link org.nkk.flow.model.node.FlowNodeModel#getType()}
 * 和 {@link org.nkk.flow.model.node.router.FlowConditionNode#getType()}。任务办理过程中的转办、委派、跳转、
 * 驳回等运行时任务类型仍由 {@link org.nkk.flow.enums.core.FlowTaskEnum.TaskType} 表示。</p>
 */
@Getter
@AllArgsConstructor
public enum FlowNodeTypeEnum implements IEnum<Integer> {

    END(-1, "结束节点"),
    START(0, "发起节点"),
    APPROVAL(1, "审批节点"),
    COPY(2, "抄送节点"),
    CONDITION_APPROVAL(3, "条件审批"),
    CONDITION_BRANCH(4, "条件分支"),
    CALL_PROCESS(5, "子流程"),
    TIMER(6, "延迟等待"),
    TRIGGER(7, "触发器"),
    PARALLEL_BRANCH(8, "并行分支"),
    INCLUSIVE_BRANCH(9, "包容分支"),
    ROUTE_BRANCH(23, "路由分支"),
    AUTO_PASS(30, "自动通过"),
    AUTO_REJECT(31, "自动拒绝");

    private final Integer value;
    private final String label;

    @Override
    public Integer value() {
        return value;
    }

    @Override
    public String label() {
        return label;
    }

    public boolean eq(Integer value) {
        return this.value.equals(value);
    }
}

