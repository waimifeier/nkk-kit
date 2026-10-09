package org.nkk.flow.service;

import org.nkk.core.beans.common.PageResult;
import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.entity.FlowHisInstance;
import org.nkk.flow.entity.FlowProcess;

import java.util.Date;
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
     * 分页查询流程定义原始记录（包含全部版本、草稿和历史记录）。
     *
     * @param current     页码，从 1 开始
     * @param size        每页条数
     * @param tenantId    租户 ID，为空时只查无租户数据
     * @param processName 流程名称，模糊匹配，为空不过滤
     * @param processKey  流程 key，精确匹配，为空不过滤
     * @param processType 流程分类，精确匹配，为空不过滤
     * @param processState 流程状态列表（多值 IN 查询），为空不过滤
     * @param formSourceType 表单来源类型（form/business），精确匹配，为空不过滤
     * @return 流程定义分页结果
     */
    PageResult<FlowProcess> pageProcesses(long current, long size, String tenantId, String processName,
                                          String processKey, String processType, List<Integer> processState,
                                          String formSourceType);

    /**
     * 按流程定义 ID 分页查询已发起的流程实例（含审批中、已通过、已驳回、已撤回等全部状态）。
     *
     * <p>实例记录来自历史实例表，覆盖实例全生命周期。返回结果含流程定义名称（由 processId 反查填充）。</p>
     *
     * @param current      页码，从 1 开始
     * @param size         每页条数
     * @param processId    流程定义 ID，必填
     * @param starterName  发起人名称，模糊匹配，为空不过滤
     * @param startTime    发起开始时间（提交时间下限，含），为空不过滤
     * @param endTime      发起结束时间（提交时间上限，含），为空不过滤
     * @param instanceState 实例状态，精确匹配，为空不过滤；取值见
     *                      {@link org.nkk.flow.enums.core.FlowInstanceEnum.InstanceState}：
     *                      0-审批中、1-审批通过、2-审批驳回、3-已撤销
     * @param instanceId   实例 ID，精确匹配，为空不过滤
     * @return 流程实例分页结果，每条记录含流程名称
     */
    PageResult<FlowHisInstance> pageInstances(long current, long size, Long processId, String starterName,
                                              Date startTime, Date endTime, Integer instanceState, Long instanceId);

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
     * 删除草稿流程定义。
     *
     * <p>仅草稿状态（{@link org.nkk.flow.enums.core.FlowProcessEnum.ProcessState#DRAFT}）
     * 且没有发起过流程实例时允许删除；已发布、已停用、历史版本或已有实例的流程均不允许删除。</p>
     *
     * @param id 流程定义 ID
     * @return 是否删除成功
     */
    boolean deleteDraft(Long id);

    /**
     * 更新流程定义基础资料，不修改流程模型和版本。
     *
     * @param process 包含流程定义 ID 和待更新资料的对象
     * @return 是否更新成功
     */
    boolean updateProcessInfo(FlowProcess process);
}

