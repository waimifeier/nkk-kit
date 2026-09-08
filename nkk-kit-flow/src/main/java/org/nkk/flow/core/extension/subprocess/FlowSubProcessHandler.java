package org.nkk.flow.core.extension.subprocess;

import cn.hutool.core.util.StrUtil;
import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.core.context.FlowExecution;
import org.nkk.flow.entity.FlowHisInstance;
import org.nkk.flow.entity.FlowInstance;
import org.nkk.flow.entity.FlowProcess;
import org.nkk.flow.enums.core.FlowInstanceEnum.InstanceState;
import org.nkk.flow.model.FlowNodeModel;

/**
 * 子流程处理扩展。
 */
public interface FlowSubProcessHandler {

    boolean start(FlowContext context, FlowExecution execution, FlowNodeModel nodeModel);

    /**
     * 解析子流程定义。默认支持 processKey、processKey:version、processId 三种写法。
     */
    default FlowProcess resolveProcess(FlowContext context, FlowExecution execution, FlowNodeModel nodeModel) {
        String callProcess = nodeModel.getCallProcess();
        if (StrUtil.isBlank(callProcess)) {
            throw new IllegalArgumentException("子流程定义不能为空，nodeKey=" + nodeModel.getNodeKey());
        }
        String[] parts = callProcess.split(":");
        String processKeyOrId = parts[0];
        if (isLong(processKeyOrId)) {
            return context.getProcessService().getProcessById(Long.valueOf(processKeyOrId)).checkState();
        }
        Integer version = null;
        if (parts.length > 1 && isInteger(parts[1])) {
            version = Integer.valueOf(parts[1]);
        }
        return context.getProcessService()
                .getProcessByVersion(execution.getFlowCreator().getTenantId(), processKeyOrId, version)
                .checkState();
    }

    /**
     * 子流程结束后的父流程联动。默认策略：通过则推进父流程，拒绝/终止/撤销/超时/作废则结束父流程。
     */
    default boolean onChildFinished(FlowContext context, FlowInstance childInstance, FlowHisInstance childHis,
                                    InstanceState state) {
        return true;
    }

    default boolean isLong(String value) {
        return StrUtil.isNotBlank(value) && StrUtil.isNumeric(value);
    }

    default boolean isInteger(String value) {
        if (!isLong(value)) {
            return false;
        }
        try {
            return Long.valueOf(value) <= Integer.MAX_VALUE;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}

