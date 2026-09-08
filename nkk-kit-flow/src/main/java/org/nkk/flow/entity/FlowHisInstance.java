package org.nkk.flow.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;
import org.nkk.flow.enums.core.FlowInstanceEnum.InstanceState;

import java.util.Date;

/**
 * 历史流程实例。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flow_his_instance")
public class FlowHisInstance extends FlowInstance {

    private static final long serialVersionUID = 1L;

    /**
     * 流程实例最终状态，取值见 {@link InstanceState}。
     */
    private Integer instanceState;

    /**
     * 流程实例结束时间。
     */
    private Date endTime;

    /**
     * 流程实例耗时，单位毫秒。
     */
    private Long duration;

    /**
     * 根据活动流程实例创建历史流程实例。
     *
     * @param instance 活动流程实例
     * @param state 历史实例状态
     * @return 历史流程实例
     */
    public static FlowHisInstance of(FlowInstance instance, InstanceState state) {
        FlowHisInstance his = new FlowHisInstance();
        his.setId(instance.getId());
        his.setTenantId(instance.getTenantId());
        his.setCreateId(instance.getCreateId());
        his.setCreateBy(instance.getCreateBy());
        his.setCreateTime(instance.getCreateTime());
        his.setProcessId(instance.getProcessId());
        his.setProcessKey(instance.getProcessKey());
        his.setParentInstanceId(instance.getParentInstanceId());
        his.setPriority(instance.getPriority());
        his.setInstanceNo(instance.getInstanceNo());
        his.setBusinessKey(instance.getBusinessKey());
        his.setVariable(instance.getVariable());
        his.setCurrentNodeName(instance.getCurrentNodeName());
        his.setCurrentNodeKey(instance.getCurrentNodeKey());
        his.setExpireTime(instance.getExpireTime());
        his.setLastUpdateBy(instance.getLastUpdateBy());
        his.setLastUpdateTime(instance.getLastUpdateTime());
        his.setInstanceState(state.value());
        return his;
    }
}


