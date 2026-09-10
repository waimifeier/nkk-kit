package org.nkk.flow.model.node.router;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.nkk.flow.enums.node.FlowNodeTypeEnum;

import java.util.List;
import org.nkk.flow.model.node.FlowNodeType;

/**
 * 包容分支路由容器（type=9），满足条件的分支执行。
 */
@Data
@FlowNodeType(FlowNodeTypeEnum.INCLUSIVE_BRANCH)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InclusiveRouterNodeModel extends RouterNodeModel {

    private static final long serialVersionUID = 1L;

    /**
     * 包容分支列表。
     */
    private List<FlowConditionNode> inclusiveNodes;

    @Override
    public List<FlowConditionNode> getBranchNodes() {
        return inclusiveNodes;
    }
}
