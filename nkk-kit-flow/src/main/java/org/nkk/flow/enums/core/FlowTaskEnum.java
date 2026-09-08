package org.nkk.flow.enums.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nkk.core.enums.common.IEnum;

/**
 * 流程任务枚举容器。
 */
public final class FlowTaskEnum {

    private FlowTaskEnum() {
    }

    /**
     * 任务类型。
     */
    @Getter
    @AllArgsConstructor
    public enum TaskType implements IEnum<Integer> {

        END(-1, "结束"),
        START(0, "发起"),
        APPROVAL(1, "审批"),
        COPY(2, "抄送"),
        CONDITION_APPROVAL(3, "条件审批"),
        CONDITION_BRANCH(4, "条件分支"),
        CALL_PROCESS(5, "子流程"),
        TIMER(6, "定时器"),
        TRIGGER(7, "触发器"),
        PARALLEL_BRANCH(8, "并行分支"),
        INCLUSIVE_BRANCH(9, "包容分支"),
        TRANSFER(10, "转办"),
        DELEGATE(11, "委派"),
        DELEGATE_RETURN(12, "委派归还"),
        AGENT(13, "代理"),
        AGENT_RETURN(14, "代理归还"),
        AGENT_ASSIST(15, "代理协办"),
        AGENT_OWN(16, "被代理人办理"),
        RECLAIM(17, "拿回"),
        WITHDRAW(18, "撤回"),
        REJECT(19, "拒绝"),
        JUMP(20, "跳转"),
        REJECT_JUMP(21, "驳回跳转"),
        ROUTE_JUMP(22, "路由跳转"),
        ROUTE_BRANCH(23, "路由分支"),
        RE_APPROVE_JUMP(24, "驳回重新审批跳转"),
        SAVE_AS_DRAFT(25, "暂存待审"),
        AUTO_PASS(30, "自动通过"),
        AUTO_REJECT(31, "自动拒绝"),
        TRIGGER_JUMP(32, "触发器跳转");

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

        public static TaskType of(Integer value) {
            TaskType type = IEnum.resolveKeyOfNullable(TaskType.class, value);
            return type == null ? APPROVAL : type;
        }
    }

    /**
     * 任务参与方式。
     */
    @Getter
    @AllArgsConstructor
    public enum PerformType implements IEnum<Integer> {

        START(0, "发起"),
        SEQUENTIAL(1, "顺序审批"),
        COUNTERSIGN(2, "会签"),
        OR_SIGN(3, "或签"),
        VOTE_SIGN(4, "票签"),
        CALL_PROCESS(5, "子流程"),
        TIMER(6, "定时器"),
        TRIGGER(7, "触发器"),
        COPY(9, "抄送");

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

        public static PerformType of(Integer value) {
            PerformType type = IEnum.resolveKeyOfNullable(PerformType.class, value);
            return type == null ? SEQUENTIAL : type;
        }
    }

    /**
     * 任务历史状态。
     */
    @Getter
    @AllArgsConstructor
    public enum TaskState implements IEnum<Integer> {

        ACTIVE(0, "活动"),
        JUMP(1, "跳转"),
        COMPLETED(2, "完成"),
        REJECTED(3, "拒绝"),
        REVOKED(4, "撤销"),
        TIMEOUT(5, "超时"),
        TERMINATED(6, "终止"),
        REJECT_END(7, "驳回终止"),
        AUTO_COMPLETED(8, "自动完成"),
        AUTO_REJECTED(9, "自动拒绝"),
        AUTO_JUMP(10, "自动跳转"),
        REJECT_JUMP(11, "驳回跳转"),
        RE_APPROVE_JUMP(12, "驳回重新审批跳转"),
        ROUTE_JUMP(13, "路由跳转"),
        TRIGGER_JUMP(14, "触发器跳转"),
        RECLAIMED(15, "拿回"),
        WITHDRAWN(16, "撤回"),
        RESUMED(17, "唤醒"),
        DESTROYED(18, "作废"),
        ABSTAINED(19, "弃权");

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

        public static TaskState of(Integer value) {
            return IEnum.resolveKeyOfNullable(TaskState.class, value);
        }
    }
}

