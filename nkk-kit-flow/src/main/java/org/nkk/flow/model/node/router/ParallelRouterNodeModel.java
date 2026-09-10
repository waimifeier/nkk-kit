package org.nkk.flow.model.node.router;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.nkk.flow.enums.node.FlowNodeTypeEnum;

import java.util.List;
import org.nkk.flow.model.node.FlowNodeType;

/**
 * 并行分支路由容器（type=8），所有分支同时执行。
 */
@Data
@FlowNodeType(FlowNodeTypeEnum.PARALLEL_BRANCH)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ParallelRouterNodeModel extends RouterNodeModel {

    private static final long serialVersionUID = 1L;

    /**
     * 并行分支列表。
     */
    private List<FlowConditionNode> parallelNodes;

    @Override
    public List<FlowConditionNode> getBranchNodes() {
        return parallelNodes;
    }
}
