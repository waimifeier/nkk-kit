package org.nkk.flow.enums.node;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nkk.core.enums.common.IEnum;

/**
 * 驳回策略。
 */
@Getter
@AllArgsConstructor
public enum FlowRejectStrategyEnum implements IEnum<Integer> {

    TO_INITIATOR(1, "驳回发起人"),
    TO_PREVIOUS_NODE(2, "驳回上一节点"),
    TO_SPECIFIED_NODE(3, "驳回指定节点"),
    TERMINATE_APPROVAL(4, "终止审批"),
    TO_PARENT_NODE(5, "驳回父节点");

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

    public static FlowRejectStrategyEnum of(Integer value) {
        FlowRejectStrategyEnum strategy = IEnum.resolveKeyOfNullable(FlowRejectStrategyEnum.class, value);
        return strategy == null ? TO_PREVIOUS_NODE : strategy;
    }
}

