package org.nkk.flow.enums.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nkk.core.enums.common.IEnum;

/**
 * 待办查询类型。
 */
@Getter
@AllArgsConstructor
public enum FlowTodoTypeEnum implements IEnum<Integer> {

    MY_APPLY(1, "我申请的"),
    PENDING(2, "待我审批"),
    APPROVED(3, "我已审批"),
    COPY(4, "抄送消息");

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

    public static FlowTodoTypeEnum of(Integer value) {
        return IEnum.resolveKey(FlowTodoTypeEnum.class, value);
    }
}
