package org.nkk.flow.web.extension;

import org.nkk.flow.web.model.FlowBusinessLaunchRequest;

/**
 * 业务审批发起器 SPI。
 *
 * <p>业务系统发起审批的桥接入口。业务表单场景下，
 * 业务单据的数据存储在用户自己的数据库，审批引擎只负责流程流转，
 * 两者通过 businessKey 关联、通过回调同步状态。</p>
 *
 * <h3>两种发起方式</h3>
 * <ul>
 *   <li><b>自定义表单</b>：前端选择流程 → 填写表单 → 直接调引擎的 startInstance 接口，
 *       表单字段作为 variables 传入即可。不需要 Launcher。</li>
 *   <li><b>业务表单</b>：业务系统保存单据 → 拿到单据 ID → 调 Launcher.launch() →
 *       审批引擎通过 businessKey 关联业务单据，后续通过
 *       {@link FlowApprovalCallbackHandler} 回调业务系统同步状态。</li>
 * </ul>
 *
 * <h3>未实现 Launcher 时</h3>
 * <p>默认实现直接委托给引擎的 startInstance，适用于最简单的场景：
 * 业务系统只需要一个便捷入口把 businessKey 和 variables 传进来。
 * 如果业务系统需要在发起前做额外校验（比如单据状态校验、权限校验），
 * 可以覆盖此 Bean。</p>
 */
public interface FlowBusinessApprovalLauncher {

    /**
     * 发起审批流程。
     *
     * @param request 发起请求
     * @return 流程实例 ID
     */
    Long launch(FlowBusinessLaunchRequest request);
}
