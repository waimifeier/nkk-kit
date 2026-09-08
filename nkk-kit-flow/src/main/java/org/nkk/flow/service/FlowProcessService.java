package org.nkk.flow.service;

import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.entity.FlowProcess;

import java.util.List;

/**
 * 流程定义服务。
 */
public interface FlowProcessService {

    /**
     * 部署流程定义。
     *
     * @param jsonString 流程模型 JSON
     * @param creator 操作人
     * @param repeat 是否允许重复部署新版本
     * @return 流程定义 ID
     */
    Long deploy(String jsonString, FlowCreator creator, boolean repeat);

    /**
     * 部署或保存流程定义。
     *
     * @param jsonString 流程模型 JSON
     * @param creator 操作人
     * @param repeat 是否允许重复部署新版本
     * @param saveAsDraft 是否保存为草稿
     * @return 流程定义 ID
     */
    default Long deploy(String jsonString, FlowCreator creator, boolean repeat, boolean saveAsDraft) {
        return deploy(jsonString, creator, repeat);
    }

    /**
     * 根据流程定义 ID 查询流程定义。
     *
     * @param id 流程定义 ID
     * @return 流程定义
     */
    FlowProcess getProcessById(Long id);

    /**
     * 根据流程 key 和版本查询流程定义。
     *
     * @param tenantId 租户 ID
     * @param processKey 流程 key
     * @param version 流程版本，传空时返回最新版本
     * @return 流程定义
     */
    FlowProcess getProcessByVersion(String tenantId, String processKey, Integer version);

    /**
     * 查询当前流程定义列表，不包含历史版本。
     *
     * @param tenantId 租户 ID
     * @return 当前流程定义列表
     */
    List<FlowProcess> listCurrentProcesses(String tenantId);

    /**
     * 查询流程定义的全部版本记录。
     *
     * @param tenantId 租户 ID
     * @param processKey 流程 key
     * @return 版本记录，按版本倒序返回
     */
    List<FlowProcess> getProcessVersions(String tenantId, String processKey);

    /**
     * 启用流程定义。
     *
     * @param id 流程定义 ID
     * @return 是否启用成功
     */
    boolean enableProcess(Long id);

    /**
     * 禁用流程定义。
     *
     * @param id 流程定义 ID
     * @return 是否禁用成功
     */
    boolean disableProcess(Long id);

    /**
     * 更新流程定义基础资料，不修改流程模型和版本。
     *
     * @param process 包含流程定义 ID 和待更新资料的对象
     * @return 是否更新成功
     */
    boolean updateProcessInfo(FlowProcess process);
}

