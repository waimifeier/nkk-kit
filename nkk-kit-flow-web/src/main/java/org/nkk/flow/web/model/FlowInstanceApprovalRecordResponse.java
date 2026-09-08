package org.nkk.flow.web.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 流程实例审批轨迹响应。
 */
@Data
public class FlowInstanceApprovalRecordResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 轨迹记录 ID。
     */
    private Long id;

    /**
     * 操作人 ID。
     */
    private String createId;

    /**
     * 操作人名称。
     */
    private String createBy;

    /**
     * 操作时间。
     */
    private Date createTime;

    /**
     * 流程实例 ID。
     */
    private Long instanceId;

    /**
     * 任务 ID。
     */
    private Long taskId;

    /**
     * 节点名称。
     */
    private String taskName;

    /**
     * 节点编码。
     */
    private String taskKey;

    /**
     * 当前节点类型，取值见 {@link org.nkk.flow.enums.node.FlowNodeTypeEnum}。
     */
    private Integer type;

    /**
     * 核心任务类型。
     */
    private Integer taskType;

    /**
     * 历史任务状态。当前活动任务为空。
     */
    private Integer taskState;

    /**
     * 任务完成时间。当前活动任务为空。
     */
    private Date finishTime;

    /**
     * 审批内容。
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Content content;

    /**
     * 审批内容。
     */
    @Data
    public static class Content implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 审批意见。
         */
        private String opinion;

        /**
         * 节点参与人列表。
         */
        private List<NodeUser> nodeUserList = new ArrayList<>();

    }

    /**
     * 节点参与人。
     */
    @Data
    public static class NodeUser implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 参与人 ID。
         */
        private String id;

        /**
         * 参与人名称。
         */
        private String name;

        /**
         * 参与人权重。
         */
        private Integer weight;

        /**
         * 参与人类型，0 用户，1 角色，2 部门。
         */
        private Integer actorType;
    }
}
