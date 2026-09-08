package org.nkk.flow.enums.runtime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nkk.core.enums.common.IEnum;

/**
 * 流程事件类型。
 */
@Getter
@AllArgsConstructor
public enum FlowEventTypeEnum implements IEnum<Integer> {

    PROCESS_DEPLOYED(1, "流程已部署"),
    INSTANCE_STARTED(2, "实例已启动"),
    INSTANCE_ENDED(3, "实例已结束"),
    INSTANCE_SUSPENDED(4, "实例已暂停"),
    INSTANCE_ACTIVATED(5, "实例已激活"),
    INSTANCE_REJECTED(6, "实例已拒绝"),
    INSTANCE_REVOKED(7, "实例已撤销"),
    INSTANCE_TIMEOUT(8, "实例已超时"),
    INSTANCE_TERMINATED(9, "实例已终止"),
    TASK_CREATED(10, "任务已创建"),
    TASK_COMPLETED(11, "任务已完成"),
    TASK_REJECTED(12, "任务已拒绝"),
    TASK_TRANSFERRED(13, "任务已转办"),
    TASK_DELEGATED(14, "任务已委派"),
    TASK_CLAIMED(15, "任务已认领"),
    TASK_AGENTED(16, "任务已代理"),
    TASK_ACTOR_ADDED(17, "任务已加签"),
    TASK_ACTOR_REMOVED(18, "任务已减签"),
    TASK_RECLAIMED(19, "任务已拿回"),
    TASK_WITHDRAWN(20, "任务已撤回"),
    INSTANCE_RESUMED(21, "实例已唤醒"),
    TASK_REMINDED(22, "任务已提醒"),
    TASK_TIMEOUT(23, "任务已超时"),
    TASK_TRIGGERED(24, "任务已触发"),
    TASK_JUMPED(25, "任务已跳转"),
    TASK_ROUTE_JUMPED(26, "任务已路由跳转"),
    TASK_TRIGGER_JUMPED(27, "任务已触发跳转"),
    TASK_AUTO_COMPLETED(28, "任务已自动完成"),
    TASK_AUTO_REJECTED(29, "任务已自动拒绝"),
    SUB_PROCESS_STARTED(30, "子流程已启动"),
    SUB_PROCESS_ENDED(31, "子流程已结束"),
    INSTANCE_DESTROYED(33, "实例已作废"),
    INSTANCE_VARIABLE_UPDATED(34, "实例变量已更新"),
    INSTANCE_MODEL_UPDATED(35, "实例模型已更新"),
    INSTANCE_NODE_APPENDED(36, "实例节点已追加"),
    INSTANCE_NODE_REMOVED(37, "实例节点已移除"),
    INSTANCE_CASCADE_REMOVED(38, "实例已级联删除"),
    TASK_REJECT_JUMPED(39, "任务已驳回跳转"),
    TASK_RE_APPROVE_JUMPED(40, "任务已驳回重新审批跳转"),
    TASK_RESOLVED(41, "任务委派已归还"),
    TASK_VIEWED(42, "任务已查看"),
    TASK_ACTOR_CHANGED(43, "任务参与人已变更"),
    TASK_TERMINATED(44, "任务已终止"),
    TASK_REVOKED(45, "任务已撤销"),
    TASK_DESTROYED(46, "任务已作废"),
    TASK_REJECT_ENDED(47, "任务已驳回终止"),
    TASK_RESUMED(48, "任务已唤醒"),
    TASK_ABSTAINED(49, "任务已弃权"),
    TASK_COPIED(50, "任务已抄送");

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
