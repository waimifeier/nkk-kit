package org.nkk.flow.model.node.router;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.nkk.flow.model.node.FlowNodeModel;

/**
 * 路由容器节点基类：条件、并行、包容、路由等持有分支列表的节点类型。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class RouterNodeModel extends FlowNodeModel {

    private static final long serialVersionUID = 1L;

    /**
     * 分支汇聚策略，1 表示所有分支汇聚后再继续流转。
     */
    private Integer groupStrategy;

    /**
     * 判断当前节点是否需要等待所有分支汇聚。
     *
     * @return true 表示所有分支汇聚后再继续流转
     */
    public boolean allJoinGroupStrategy() {
        return Integer.valueOf(1).equals(groupStrategy);
    }
}
