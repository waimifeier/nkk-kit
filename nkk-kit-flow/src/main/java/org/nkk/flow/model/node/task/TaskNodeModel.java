package org.nkk.flow.model.node.task;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import org.nkk.flow.model.node.FlowNodeModel;

/**
 * 任务节点基类：发起、审批、抄送、延迟等待、触发器等会创建办理任务的节点类型。
 *
 * <p>持有任务创建所需的公共配置：办理人、参与方式、任务期限、办理页地址等。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class TaskNodeModel extends FlowNodeModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 审批人设置类型，取值见 {@link org.nkk.flow.enums.node.FlowNodeSetTypeEnum}。
     */
    private Integer setType;

    /**
     * 审批参与方式，取值见 {@link org.nkk.flow.enums.core.FlowTaskEnum.PerformType}。
     */
    private Integer examineMode;

    /**
     * 票签通过权重，票签场景下达到该权重后视为通过。
     */
    private Integer passWeight;

    /**
     * 驳回策略，取值见 {@link org.nkk.flow.enums.node.FlowRejectStrategyEnum}。
     */
    private Integer rejectStrategy;

    /**
     * 是否允许驳回到发起节点，1 表示允许。
     */
    private Integer rejectStart;

    /**
     * 是否开启任务到期自动处理。
     */
    private Boolean termAuto;

    /**
     * 任务期限，单位小时。
     */
    private Integer term;

    /**
     * 任务到期处理模式，1 表示超时自动拒绝，其他值按自动通过处理。
     */
    private Integer termMode;

    /**
     * 是否开启任务提醒。
     */
    private Boolean remind;

    /**
     * 节点任务办理页地址。
     */
    private String actionUrl;

    /**
     * 节点办理人配置。
     */
    private List<FlowNodeAssignee> nodeAssigneeList;

    /**
     * 判断当前节点是否需要保存办理人权重。
     *
     * @return true 表示当前节点是票签模式，需要保存权重
     */
    public boolean saveWeight() {
        return Integer.valueOf(4).equals(examineMode);
    }
}
