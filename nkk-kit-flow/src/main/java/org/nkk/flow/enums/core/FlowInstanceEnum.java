package org.nkk.flow.enums.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nkk.core.enums.common.IEnum;

/**
 * 流程实例枚举容器。
 */
public final class FlowInstanceEnum {

    private FlowInstanceEnum() {
    }

    /**
     * 流程实例状态。
     */
    @Getter
    @AllArgsConstructor
    public enum InstanceState implements IEnum<Integer> {

        SUSPENDED(-2, "已暂停"),
        DRAFT(-1, "暂存"),
        ACTIVE(0, "审批中"),
        COMPLETED(1, "审批通过"),
        REJECTED(2, "审批拒绝"),
        REVOKED(3, "已撤销"),
        TIMEOUT(4, "超时结束"),
        TERMINATED(5, "强制终止"),
        AUTO_PASS(6, "自动通过"),
        AUTO_REJECT(7, "自动拒绝"),
        DESTROYED(8, "已作废");

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

