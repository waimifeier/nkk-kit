package org.nkk.flow.model.node.router;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.nkk.flow.enums.node.FlowNodeTypeEnum;

import java.util.List;
import org.nkk.flow.model.node.FlowNodeType;

/**
 * 路由分支容器（type=23），按分支定义跳转到指定节点。
 */
@Data
@FlowNodeType(FlowNodeTypeEnum.ROUTE_BRANCH)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RouteRouterNodeModel extends RouterNodeModel {

    private static final long serialVersionUID = 1L;

    /**
     * 路由分支列表。
     */
    private List<FlowConditionNode> routeNodes;

    @Override
    public List<FlowConditionNode> getBranchNodes() {
        return routeNodes;
    }
}
