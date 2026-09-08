package org.nkk.flow.enums.runtime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nkk.core.enums.common.IEnum;

/**
 * 流程执行类型。
 */
@Getter
@AllArgsConstructor
public enum FlowExecuteTypeEnum implements IEnum<Integer> {

    START(1, "启动流程"),
    CREATE_TASK(2, "创建任务"),
    COMPLETE_TASK(3, "完成任务"),
    END_INSTANCE(4, "结束实例"),
    UPDATE_INSTANCE(5, "更新实例");

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
}
