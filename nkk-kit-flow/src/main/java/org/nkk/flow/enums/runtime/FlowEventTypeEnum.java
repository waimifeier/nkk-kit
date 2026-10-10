package org.nkk.flow.enums.runtime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nkk.core.enums.common.IEnum;

/**
 * 流程事件类型。
 *
 * <p>按资源分组连续编号，对标钉钉 {@code bpms_instance_change}/{@code bpms_task_change}
 * 与飞书审批实例/任务事件模型：</p>
 * <ul>
 *   <li><b>实例级（1-11）</b>：实例生命周期状态变化，终态事件
 *       （ENDED/REJECTED/REVOKED/TERMINATED/TIMEOUT/DESTROYED/CASCADE_REMOVED）
 *       由 {@code FlowApprovalCallbackHandler} 翻译为业务状态回写。</li>
 *   <li><b>任务级（12-42）</b>：任务生命周期、待办域（提醒/催办/超时/抄送）、
 *       跳转类、自动处理与终态，供公共事件监听（消息通知、日志、审计）消费。</li>
 * </ul>
 */
@Getter
@AllArgsConstructor
public enum FlowEventTypeEnum implements IEnum<Integer> {

    // ==================== 实例级：生命周期（对标飞书 START/APPROVED/REJECTED/CANCELED/TERMINATED/DELETED） ====================

    INSTANCE_STARTED(1, "实例已启动"),
    INSTANCE_ENDED(2, "实例已结束"),
    INSTANCE_REJECTED(3, "实例已拒绝"),
    INSTANCE_REVOKED(4, "实例已撤回"),
    INSTANCE_TERMINATED(5, "实例已终止"),
    INSTANCE_TIMEOUT(6, "实例已超时"),
    INSTANCE_DESTROYED(7, "实例已作废"),
    INSTANCE_CASCADE_REMOVED(8, "实例已级联删除"),
    INSTANCE_SUSPENDED(9, "实例已暂停"),
    INSTANCE_ACTIVATED(10, "实例已激活"),
    INSTANCE_RESUMED(11, "实例已唤醒"),

    // ==================== 任务级：生命周期 ====================

    TASK_CREATED(12, "任务已创建"),
    TASK_COMPLETED(13, "任务已完成"),
    TASK_REJECTED(14, "任务已拒绝"),
    TASK_TRANSFERRED(15, "任务已转办"),
    TASK_DELEGATED(16, "任务已委派"),
    TASK_RESOLVED(17, "任务委派已归还"),
    TASK_CLAIMED(18, "任务已认领"),
    TASK_AGENTED(19, "任务已代理"),
    TASK_ACTOR_ADDED(20, "任务已加签"),
    TASK_ACTOR_REMOVED(21, "任务已减签"),
    TASK_ACTOR_CHANGED(22, "任务参与人已变更"),
    TASK_RECLAIMED(23, "任务已拿回"),
    TASK_WITHDRAWN(24, "任务已撤回"),
    TASK_VIEWED(25, "任务已查看"),
    TASK_COPIED(26, "任务已抄送"),

    // ==================== 任务级：待办域（提醒/催办/超时） ====================

    TASK_REMINDED(27, "任务已提醒"),
    TASK_URGED(28, "任务已催办"),
    TASK_TIMEOUT(29, "任务已超时"),
    TASK_TRIGGERED(30, "任务已触发"),

    // ==================== 任务级：跳转类 ====================

    TASK_JUMPED(31, "任务已跳转"),
    TASK_TRIGGER_JUMPED(32, "任务已触发跳转"),
    TASK_REJECT_JUMPED(33, "任务已驳回跳转"),
    TASK_RE_APPROVE_JUMPED(34, "任务已驳回重新审批跳转"),
    TASK_REJECT_ENDED(35, "任务已驳回终止"),

    // ==================== 任务级：自动处理 ====================

    TASK_AUTO_COMPLETED(36, "任务已自动完成"),
    TASK_AUTO_REJECTED(37, "任务已自动拒绝"),

    // ==================== 任务级：终态 ====================

    TASK_TERMINATED(38, "任务已终止"),
    TASK_REVOKED(39, "任务已撤回"),
    TASK_DESTROYED(40, "任务已作废"),
    TASK_RESUMED(41, "任务已唤醒"),
    TASK_ABSTAINED(42, "任务已弃权");

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
