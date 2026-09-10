package org.nkk.flow.model.node.router;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.nkk.flow.enums.node.FlowNodeTypeEnum;

import java.util.List;
import org.nkk.flow.model.node.FlowNodeType;

/**
 * 条件分支路由容器（type=4），分支项见 {@link FlowConditionNode}。
 */
@Data
@FlowNodeType(FlowNodeTypeEnum.CONDITION_BRANCH)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ConditionRouterNodeModel extends RouterNodeModel {

    private static final long serialVersionUID = 1L;

    /**
     * 条件分支列表。
     */
    private List<FlowConditionNode> conditionNodes;

    @Override
    public List<FlowConditionNode> getBranchNodes() {
        return conditionNodes;
    }
}
