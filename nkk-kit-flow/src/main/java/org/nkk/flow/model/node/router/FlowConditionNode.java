package org.nkk.flow.model.node.router;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.nkk.flow.enums.node.FlowNodeTypeEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.nkk.flow.model.node.FlowNodeModel;
import org.nkk.flow.model.node.FlowNodeType;

/**
 * 分支条件节点（type=3），条件/并行/包容/路由容器内的分支项。
 */
@Data
@FlowNodeType(FlowNodeTypeEnum.CONDITION_APPROVAL)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlowConditionNode extends FlowNodeModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分支优先级，值越小越先匹配。
     */
    private Integer priorityLevel = 0;

    /**
     * 分支条件组列表，二维结构：外层为条件组（组间 OR），内层为组内条件（组内 AND）。
     * 空列表表示默认分支。
     */
    private List<List<FlowCondition>> conditionList = new ArrayList<>();
}
