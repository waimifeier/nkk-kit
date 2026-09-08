package org.nkk.flow.web.model;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 业务发起审批请求。
 *
 * <p>业务系统通过 {@link org.nkk.flow.web.extension.FlowBusinessApprovalLauncher}
 * 桥接进入审批引擎时使用的请求模型。</p>
 */
@Data
public class FlowBusinessLaunchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 流程定义 ID 或流程 key。推荐传流程 key，引擎自动取最新启用版本。
     */
    private String processKey;

    /**
     * 业务单据 ID，审批引擎通过 businessKey 关联业务数据。
     * 业务表单场景下此字段必传，后续回调也会带上这个值。
     */
    private String businessKey;

    /**
     * 业务类型标识，比如 leave_form、purchase_order。
     * 用于在回调时区分是哪个业务场景发起的。
     */
    private String businessType;

    /**
     * 租户 ID。
     */
    private String tenantId;

    /**
     * 发起人 ID。为空时引擎从 FlowCreatorProvider 获取。
     */
    private String startUserId;

    /**
     * 发起人名称。
     */
    private String startUserName;

    /**
     * 流程变量，自定义表单场景下会包含表单字段值。
     */
    private Map<String, Object> variables = new LinkedHashMap<>();

    /**
     * 流程实例详情页 URL，可覆盖流程定义上的默认值。
     */
    private String instanceUrl;

    /**
     * 发起人选择的下一步审批人，覆盖流程定义上的节点配置。
     * key 为节点 key，value 为审批人 ID 列表。
     */
    private Map<String, java.util.List<String>> nextApprovers = new LinkedHashMap<>();
}
