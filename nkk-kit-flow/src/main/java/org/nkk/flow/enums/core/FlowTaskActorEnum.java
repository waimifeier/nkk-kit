package org.nkk.flow.enums.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nkk.core.enums.common.IEnum;

/**
 * 任务参与者枚举容器。
 */
public final class FlowTaskActorEnum {

    private FlowTaskActorEnum() {
    }

    /**
     * 参与者类型。
     */
    @Getter
    @AllArgsConstructor
    public enum ActorType implements IEnum<Integer> {

        USER(0, "用户"),
        ROLE(1, "角色"),
        DEPARTMENT(2, "部门");

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

    /**
     * 代理参与类型。
     */
    @Getter
    @AllArgsConstructor
    public enum AgentType implements IEnum<Integer> {

        AGENT(0, "代理人"),
        OWNER(1, "被代理人"),
        CLAIM_ROLE(2, "认领角色"),
        CLAIM_DEPARTMENT(3, "认领部门");

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

