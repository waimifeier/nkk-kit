package org.nkk.flow.core.extension.identity;

import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.entity.FlowTaskActor;

import java.util.List;

/**
 * 任务访问策略。
 *
 * <p>该策略保留作为任务级访问控制的扩展点：抄送任务阅读（viewTask）等需要携带
 * {@link FlowTask} 上下文做差异化放行的场景，使用方可覆盖
 * {@link #isAllowed(FlowTask, FlowCreator, List)}。</p>
 *
 * <p>组织身份匹配（用户/角色/部门命中）由 {@link FlowCreator#matches(String, Integer)}
 * 自带完成，业务系统无需再注册自定义 Bean。</p>
 */
public interface FlowTaskAccessStrategy {

    /**
     * 判断当前操作人是否允许处理任务参与者快照。
     *
     * <p>核心引擎在审批、驳回、转办、委派、加签、减签等需要校验操作人的入口调用该方法。
     * 默认实现通过 {@link FlowCreator#matches(String, Integer)} 判断操作人是否命中某个参与者，
     * 用户、角色、部门判断由 {@link FlowCreator} 自带的 {@code deptId}、{@code roleIds} 完成。</p>
     *
     * @param creator    当前流程操作人
     * @param taskActors 当前任务的参与者快照列表，可能包含用户、角色、部门等类型
     * @return 有权限时返回命中的任务参与者；无权限时返回 {@code null}
     */
    FlowTaskActor isAllowed(FlowCreator creator, List<FlowTaskActor> taskActors);

    /**
     * 判断当前操作人是否允许处理当前任务（携带完整任务上下文）。
     *
     * <p>默认委托给 {@link #isAllowed(FlowCreator, List)}。使用方可覆盖该方法，根据
     * {@link FlowTask#getTaskType()} 等字段做差异化放行，例如抄送任务允许任意被抄送人阅读。</p>
     *
     * @param task       当前任务，携带 taskType、nodeKey 等上下文
     * @param creator    当前流程操作人
     * @param taskActors 当前任务的参与者快照列表
     * @return 有权限时返回命中的任务参与者；无权限时返回 {@code null}
     */
    default FlowTaskActor isAllowed(FlowTask task, FlowCreator creator, List<FlowTaskActor> taskActors) {
        return isAllowed(creator, taskActors);
    }
}
