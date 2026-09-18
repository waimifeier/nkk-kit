package org.nkk.flow.web.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nkk.core.enums.common.IEnum;

/**
 * 审批轨迹节点状态（前端 timeline 颜色和标签的唯一来源）。
 *
 * <p>引擎 {@code FlowTask.getTaskState()} 原始值语义模糊（18 种，分阶段混用），
 * 审批轨迹前端只关心 6 种终态，service 层通过
 * {@link org.nkk.flow.web.service.FlowDesignerRuntimeService#mapTaskStateToNodeStatus(Integer)}
 * 把引擎 TaskState 归并到这里。</p>
 *
 * <p>对外 API 返回的 {@code FlowInstanceApprovalRecordResponse.nodeStatus}
 * 就是这里的 {@link #value}，前端已按此值映射颜色，新增/修改枚举值时需同步前端
 * {@code nkk-flow/src/api/flow-api.ts} 的 {@code NodeStatus} 常量。</p>
 */
@Getter
@AllArgsConstructor
public enum ApprovalNodeStatusEnum implements IEnum<Integer> {

    /** 已通过（正常完成 / 自动完成）。 */
    COMPLETED(0, "已通过"),
    /** 审批中（活动任务，未结束）。 */
    IN_PROGRESS(1, "待处理"),
    /** 已驳回。 */
    REJECTED(2, "已驳回"),
    /** 已撤回（发起人撤回、发起人收回、撤销）。 */
    REVOKED(3, "已撤回"),
    /** 已终止 / 已作废。 */
    TERMINATED(4, "已终止"),
    /** 已跳过（条件跳转、自动通过、路由跳转等未实际到达的节点）。 */
    SKIPPED(5, "已跳过");

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

    /**
     * 按 value 反向查找枚举（service 层组装 Response 时用）。
     */
    public static ApprovalNodeStatusEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (ApprovalNodeStatusEnum e : values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return null;
    }
}
