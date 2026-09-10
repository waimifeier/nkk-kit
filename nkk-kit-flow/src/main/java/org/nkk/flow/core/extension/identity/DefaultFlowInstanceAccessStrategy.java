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
 *     <li>系统操作（定时超时、触发器、子流程级联回调等以系统操作人身份执行的自动动作）：直接放行。</li>
 *     <li>撤回、终止、挂起、激活、作废、超时结束等人工操作：只允许流程发起人（实例创建人）操作。</li>
 * </ul>
 *
 * <p>系统操作人由 {@link FlowCreatorProvider#getSystemCreator()} 提供。业务系统如果有自己的管理员、
 * 角色或部门规则（如流程管理员、部门主管也可操作实例），请注册自定义
 * {@link FlowInstanceAccessStrategy} Bean 覆盖该实现。</p>
 */
public class DefaultFlowInstanceAccessStrategy implements FlowInstanceAccessStrategy {

    private FlowCreatorProvider creatorProvider;

    public void setCreatorProvider(FlowCreatorProvider creatorProvider) {
        this.creatorProvider = creatorProvider;
    }

    @Override
    public boolean isAllowed(FlowCreator creator, FlowInstance instance, FlowHisInstance hisInstance,
                             FlowInstanceOperateEnum operateType) {
        if (creator == null || StrUtil.isBlank(creator.getCreateId())) {
            return false;
        }
        // 系统自动动作（超时扫描、触发器、子流程级联回调等）不受业务权限约束
        if (isSystem(creator)) {
            return true;
        }
        // 所有人工实例操作（撤回、终止、挂起、激活、作废等）只允许流程发起人
        return isCreator(creator, instance, hisInstance);
    }

    /**
     * 判断当前操作人是否为系统操作人。
     *
     * <p>系统操作人由 {@link FlowCreatorProvider#getSystemCreator()} 提供，
     * 所有以系统身份执行的自动动作直接放行。</p>
     *
     * @param creator 当前操作人
     * @return true 表示当前操作人是系统操作人
     */
    protected boolean isSystem(FlowCreator creator) {
        if (creatorProvider == null) {
            return false;
        }
        FlowCreator systemCreator = creatorProvider.getSystemCreator();
        return systemCreator != null && StrUtil.equals(systemCreator.getCreateId(), creator.getCreateId());
    }

    /**
     * 判断当前操作人是否为流程发起人（实例创建人）。
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
}
