package org.nkk.flow.core.extension.identity;

import cn.hutool.core.util.StrUtil;
import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.entity.FlowHisInstance;
import org.nkk.flow.entity.FlowInstance;
import org.nkk.flow.enums.runtime.FlowInstanceOperateEnum;

/**
 * 默认流程实例操作权限策略。
 *
 * <p>默认规则：</p>
 * <ul>
 *     <li>撤回流程：只允许流程发起人操作。</li>
 *     <li>挂起、激活、终止、作废、超时结束：只允许管理员操作。</li>
 * </ul>
 *
 * <p>默认管理员判断使用 {@link FlowCreator#ADMIN} 的用户 ID。业务系统如果有自己的管理员、
 * 角色或部门规则，请注册自定义 {@link FlowInstanceAccessStrategy} Bean 覆盖该实现。</p>
 */
public class DefaultFlowInstanceAccessStrategy implements FlowInstanceAccessStrategy {

    @Override
    public boolean isAllowed(FlowCreator creator, FlowInstance instance, FlowHisInstance hisInstance,
                             FlowInstanceOperateEnum operateType) {
        if (creator == null || StrUtil.isBlank(creator.getCreateId())) {
            return false;
        }
        if (FlowInstanceOperateEnum.REVOKE == operateType) {
            return isCreator(creator, instance, hisInstance);
        }
        return isAdmin(creator);
    }

    /**
     * 判断当前操作人是否为流程发起人。
     *
     * @param creator 当前操作人
     * @param instance 活动实例
     * @param hisInstance 历史实例
     * @return true 表示当前操作人就是流程发起人
     */
    protected boolean isCreator(FlowCreator creator, FlowInstance instance, FlowHisInstance hisInstance) {
        String createId = instance == null ? null : instance.getCreateId();
        if (StrUtil.isBlank(createId) && hisInstance != null) {
            createId = hisInstance.getCreateId();
        }
        return StrUtil.equals(creator.getCreateId(), createId);
    }

    /**
     * 判断当前操作人是否为流程管理员。
     *
     * <p>默认只识别 {@link FlowCreator#ADMIN}。如果业务系统使用角色、权限标识或用户表字段判断管理员，
     * 可以继承该类覆盖本方法，或直接注册自定义 {@link FlowInstanceAccessStrategy}。</p>
     *
     * @param creator 当前操作人
     * @return true 表示当前操作人是流程管理员
     */
    protected boolean isAdmin(FlowCreator creator) {
        return StrUtil.equals(FlowCreator.ADMIN.getCreateId(), creator.getCreateId());
    }
}
