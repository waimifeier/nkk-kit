package org.nkk.flow.core.extension.identity;

import org.nkk.flow.model.node.task.FlowNodeAssignee;

/**
 * 主管解析策略。
 *
 * <p>核心引擎不内置组织表，不知道"谁是谁的主管"。
 * 当节点 setType=SUPERVISOR(2) 或 CONTINUOUS_SUPERVISOR(6) 时，
 * 由宿主项目注册该策略 Bean 提供主管信息。</p>
 *
 * <p>调用场景：流程节点预览（node-map 接口传入 userId 时），
 * 根据预览用户的组织关系解析出具体主管，用于在节点小地图中展示主管名称。</p>
 */
public interface FlowSupervisorResolver {

    /**
     * 解析指定用户在指定层级的主管。
     *
     * @param userId 用户 ID
     * @param level  主管层级：1=直接主管，2=二级主管，以此类推
     * @return 主管信息；该层级无主管（已到顶层）时返回 {@code null}
     */
    FlowNodeAssignee resolveSupervisor(String userId, int level);
}
