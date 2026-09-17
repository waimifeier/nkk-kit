package org.nkk.flow.web.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 流程节点小地图段 —— 精简版。
 *
 * <p>接口返回 {@code List<FlowProcessNodeMapResponse>}，每个段通过 {@link #kind} 区分类型：
 * <ul>
 *     <li>{@code kind=node} — 普通节点（发起/审批/抄送/延迟/触发器/子流程/结束），
 *         携带 {@code nodeKey/nodeName/type}，任务类节点额外携带 {@code assignees}。</li>
 *     <li>{@code kind=branch} — 分支点（条件路由/并行/包容/路由分支容器），
 *         携带 {@code branches} 列表，每个分支含 {@code branchKey/branchName} 和嵌套的 {@code segments}。</li>
 * </ul>
 * 分支点之后紧跟的下一个段即为各分支汇聚后的主链后继。</p>
 *
 * <p>设计原则：仅保留小地图展示所需的最小字段，前端自行根据 {@code type} 映射节点图标和类型名称。</p>
 */
@Data
public class FlowProcessNodeMapResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 段类型：{@code node} — 普通节点，{@code branch} — 分支点。
     */
    private String kind;

    // ==================== node / branch 共有字段 ====================

    /**
     * 节点/路由容器唯一编码。
     */
    private String nodeKey;

    /**
     * 节点/路由容器显示名称。
     */
    private String nodeName;

    /**
     * 节点类型，取值见 {@link org.nkk.flow.enums.node.FlowNodeTypeEnum}。
     */
    private Integer type;

    // ==================== node 段特有字段 ====================

    /**
     * 节点候选办理人简要列表，仅任务类节点（审批/抄送等）有值。
     */
    private List<Assignee> assignees;

    // ==================== branch 段特有字段 ====================

    /**
     * 分支列表，路由容器节点携带。
     */
    private List<Branch> branches;

    /**
     * 路由分支信息（精简版）。
     */
    @Data
    public static class Branch implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 分支节点编码。
         */
        private String branchKey;

        /**
         * 分支显示名称（如"条件1"、"默认条件"）。
         */
        private String branchName;

        /**
         * 分支内的段列表（支持分支内嵌套路由）。
         */
        private List<FlowProcessNodeMapResponse> segments;
    }

    /**
     * 节点候选办理人简要信息（仅保留小地图展示所需字段）。
     */
    @Data
    public static class Assignee implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 参与者 ID。
         */
        private String id;

        /**
         * 参与者名称。
         */
        private String name;

        /**
         * 参与者类型：{@code 0} 用户、{@code 1} 角色、{@code 2} 部门。
         */
        private Integer actorType;
    }
}
