package org.nkk.flow.core.extension.identity;

import org.nkk.flow.core.context.FlowExecution;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.entity.FlowTaskActor;
import org.nkk.flow.model.node.task.FlowDynamicAssignee;
import org.nkk.flow.model.node.task.FlowNodeAssignee;
import org.nkk.flow.model.node.task.TaskNodeModel;

import java.util.List;

/**
 * 任务参与者提供者。
 */
public interface FlowTaskActorProvider {

    /**
     * 解析当前节点需要生成的任务参与者。
     *
     * <p>核心引擎创建审批、抄送、子流程等待等任务时会调用该方法。默认实现会优先读取
     * {@link org.nkk.flow.core.context.FlowDataTransfer#DYNAMIC_ASSIGNEE} 中当前节点的动态参与人，
     * 其次调用 {@link #getDynamicAssignee(TaskNodeModel, FlowExecution)}，最后回退到节点模型的
     * {@link TaskNodeModel#getNodeAssigneeList()}。</p>
     *
     * <p>如果使用方需要根据用户、角色、部门、岗位、表单字段等业务组织关系动态计算办理人，
     * 可以覆盖该方法并返回最终写入任务表的参与者快照。</p>
     *
     * @param nodeModel 当前正在创建任务的任务节点
     * @param execution 当前流程执行上下文，可获取发起人、实例、变量、当前任务等信息
     * @return 任务参与者列表；返回 {@code null} 或空集合时，引擎会调用
     * {@link #abnormal(FlowTask, List, FlowExecution, TaskNodeModel)} 处理异常场景
     */
    List<FlowTaskActor> getTaskActors(TaskNodeModel nodeModel, FlowExecution execution);

    /**
     * 获取节点配置层面的参与人。
     *
     * <p>该方法返回的是模型参与人 {@link FlowNodeAssignee}，还不是最终任务参与者
     * {@link FlowTaskActor}。默认直接返回节点 JSON 中的 {@code nodeAssigneeList}。
     * 使用方可以覆盖该方法，把角色、部门、候选人、自选人等配置转换成具体候选列表。</p>
     *
     * @param nodeModel 当前任务节点
     * @param execution 当前流程执行上下文
     * @return 节点参与人列表
     */
    default List<FlowNodeAssignee> getNodeAssignees(TaskNodeModel nodeModel, FlowExecution execution) {
        return nodeModel.getNodeAssigneeList();
    }

    /**
     * 动态获取当前节点的参与人配置。
     *
     * <p>适用于办理人需要在运行时计算的场景，例如发起人自选、按表单字段找用户、
     * 按业务单据找部门负责人、按金额找审批角色等。返回的
     * {@link FlowDynamicAssignee#getType()} 表示参与者类型：0 用户，1 角色，2 部门。</p>
     *
     * @param nodeModel 当前任务节点
     * @param execution 当前流程执行上下文
     * @return 动态参与人配置；返回 {@code null} 时继续使用节点静态配置
     */
    default FlowDynamicAssignee getDynamicAssignee(TaskNodeModel nodeModel, FlowExecution execution) {
        return null;
    }

    /**
     * 处理任务参与者为空的异常场景。
     *
     * <p>默认直接抛出异常。使用方可以覆盖该方法实现兜底策略，例如自动指派给管理员、
     * 保存为异常待分配任务、结束当前分支、写业务告警等。</p>
     *
     * @param task 已构建但尚未完成参与人分配的任务对象
     * @param taskActors 当前解析得到的任务参与者，通常为 {@code null} 或空集合
     * @param execution 当前流程执行上下文
     * @param nodeModel 当前任务节点
     * @return 返回 {@code true} 表示异常已处理，当前节点不再继续创建普通审批任务；
     * 返回 {@code false} 表示调用方已经补齐 {@code taskActors}，引擎可继续创建任务
     */
    default boolean abnormal(FlowTask task, List<FlowTaskActor> taskActors, FlowExecution execution, TaskNodeModel nodeModel) {
        throw new IllegalStateException("节点没有审批人，nodeKey=" + nodeModel.getNodeKey());
    }

    /**
     * 获取当前节点默认的任务参与者类型。
     *
     * <p>返回值会写入 {@link FlowTaskActor#getActorType()}。约定为：
     * 0 用户，1 角色，2 部门。默认实现中，角色/部门节点会分别返回角色/部门，
     * 其他情况返回用户。</p>
     *
     * @param nodeModel 当前任务节点
     * @return 参与者类型
     */
    Integer getActorType(TaskNodeModel nodeModel);
}

