package org.nkk.flow.enums.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nkk.core.enums.common.IEnum;

/**
 * 流程定义枚举容器。
 */
public final class FlowProcessEnum {

    private FlowProcessEnum() {
    }

    /**
     * 流程定义状态。
     */
    @Getter
    @AllArgsConstructor
    public enum ProcessState implements IEnum<Integer> {

        DRAFT(-1, "草稿"),
        DISABLED(0, "禁用"),
        ENABLED(1, "启用"),
        HISTORY(2, "历史版本");

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
}

