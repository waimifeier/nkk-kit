package org.nkk.flow.web.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.entity.FlowExtInstance;
import org.nkk.flow.entity.FlowHisInstance;
import org.nkk.flow.entity.FlowHisTask;
import org.nkk.flow.entity.FlowInstance;
import org.nkk.flow.entity.FlowProcess;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.entity.FlowTaskActor;
import org.nkk.flow.enums.core.FlowInstanceEnum.InstanceState;
import org.nkk.flow.enums.core.FlowTaskEnum;
import org.nkk.flow.enums.core.FlowTaskEnum.TaskType;
import org.nkk.flow.enums.core.FlowTaskEnum.PerformType;
import org.nkk.flow.enums.core.FlowTaskActorEnum;
import org.nkk.flow.enums.node.FlowNodeSetTypeEnum;
import org.nkk.flow.enums.node.FlowNodeTypeEnum;
import org.nkk.flow.model.FlowProcessModel;
import org.nkk.flow.model.node.task.ApprovalNodeModel;
import org.nkk.flow.model.node.task.FlowNodeAssignee;
import org.nkk.flow.model.node.task.TaskNodeModel;
import org.nkk.flow.model.node.FlowNodeModel;
import org.nkk.flow.service.NkkFlowEngine;
import org.nkk.flow.web.enums.ApprovalNodeStatusEnum;
import org.nkk.flow.web.model.FlowInstanceApprovalRecordResponse;
import org.nkk.flow.web.model.FlowRuntimeResponse;
import org.nkk.flow.web.model.FlowStartProcessRequest;
import org.nkk.flow.web.model.FlowTaskAddSignRequest;
import org.nkk.flow.web.model.FlowTaskActorParam;
import org.nkk.flow.web.model.FlowTaskAssignRequest;
import org.nkk.flow.web.model.FlowTaskCopyRequest;
import org.nkk.flow.web.model.FlowTaskOperateRequest;
import org.nkk.flow.web.model.FlowTaskRemoveSignRequest;
import org.nkk.flow.web.model.FlowTaskRollbackRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 流程设计器运行时服务。
 */
public class FlowDesignerRuntimeService {

    private final NkkFlowEngine flowEngine;

    public FlowDesignerRuntimeService(NkkFlowEngine flowEngine) {
        this.flowEngine = flowEngine;
    }

    /**
     * 发起流程。
     *
     * @param request 发起请求
     * @return 发起结果
     */
    public FlowRuntimeResponse start(FlowStartProcessRequest request) {
        assertStartRequest(request);
        Map<String, Object> variables = variables(request.getVariables());
        boolean saveAsDraft = Boolean.TRUE.equals(request.getSaveAsDraft());
        Optional<FlowInstance> optional;
        if (request.getProcessId() != null) {
            optional = flowEngine.startInstanceById(request.getProcessId(), null, variables, saveAsDraft,
                    null, instanceSupplier(request.getBusinessKey()));
        } else {
            optional = flowEngine.startInstanceByProcessKey(StrUtil.trim(request.getProcessKey()),
                    request.getProcessVersion(), null, variables, saveAsDraft,
                null, instanceSupplier(request.getBusinessKey()));
        }
        // 业务审批触发条件不满足时引擎返回 empty：未创建实例，引擎已直接派发「审批通过」业务回调
        if (!optional.isPresent()) {
            return triggerSkippedResponse(request);
        }
        FlowInstance instance = optional.get();
        return runtimeResponse(instance.getId());
    }

    /**
     * 构造触发条件不满足（跳过审批）的响应。
     *
     * <p>无实例数据：instanceId 为空、started=false、instanceState=审批通过，
     * 业务方据此直接按审批通过处理，无需再查询流程实例。</p>
     */
    private FlowRuntimeResponse triggerSkippedResponse(FlowStartProcessRequest request) {
        FlowRuntimeResponse response = new FlowRuntimeResponse();
        response.setStarted(false);
        response.setBusinessKey(request.getBusinessKey());
        response.setInstanceState(InstanceState.COMPLETED.value());
        return response;
    }

    /**
     * 查询流程实例详情。
     *
     * @param instanceId 流程实例 ID
     * @return 实例详情
     */
    public FlowRuntimeResponse detail(Long instanceId) {
        if (instanceId == null) {
            throw new IllegalArgumentException("流程实例 ID 不能为空");
        }
        return runtimeResponse(instanceId);
    }

    public List<FlowTask> tasks(Long instanceId) {
        if (instanceId == null) {
            throw new IllegalArgumentException("流程实例 ID 不能为空");
        }
        return flowEngine.queryService().getTasksByInstanceId(instanceId);
    }

    public FlowTask task(Long taskId) {
        FlowTask task = flowEngine.queryService().getTask(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在，taskId=" + taskId);
        }
        return task;
    }

    public List<FlowTaskActor> taskActors(Long taskId) {
        return flowEngine.queryService().getTaskActorsByTaskId(task(taskId).getId());
    }

    /**
     * 认领任务。
     *
     * <p>当审批人配置为角色或部门时，任务参与者为角色/部门，需由具体用户认领后方可办理。
     * 认领后任务参与者替换为当前用户。根据参与者类型自动选择认领方式：
     * 角色参与者走 {@code claimRole}，部门参与者走 {@code claimDepartment}，其他走 {@code claimTask}。</p>
     *
     * @param taskId 任务 ID
     * @return 认领后的运行时信息
     */
    public FlowRuntimeResponse claim(Long taskId) {
        FlowTask task = task(taskId);
        List<FlowTaskActor> actors = flowEngine.queryService().getTaskActorsByTaskId(task.getId());
        boolean hasRole = actors.stream()
                .anyMatch(a -> FlowTaskActorEnum.ActorType.ROLE.value().equals(a.getActorType()));
        boolean hasDepartment = actors.stream()
                .anyMatch(a -> FlowTaskActorEnum.ActorType.DEPARTMENT.value().equals(a.getActorType()));
        if (hasRole) {
            flowEngine.claimRole(taskId, null);
        } else if (hasDepartment) {
            flowEngine.claimDepartment(taskId, null);
        } else {
            flowEngine.claimTask(taskId, null);
        }
        return runtimeResponse(task.getInstanceId());
    }

    public FlowRuntimeResponse complete(Long taskId, FlowTaskOperateRequest request) {
        FlowTask task = task(taskId);
        flowEngine.executeTask(taskId, null, variables(request == null ? null : request.getVariables()),
                request == null ? null : request.getOpinion());
        return runtimeResponse(task.getInstanceId());
    }

    public FlowRuntimeResponse reject(Long taskId, FlowTaskOperateRequest request) {
        FlowTask task = task(taskId);
        Map<String, Object> variables = variables(request == null ? null : request.getVariables());
        String opinion = request == null ? null : request.getOpinion();
        if (request != null && StrUtil.isNotBlank(request.getNodeKey())) {
            assertNodeOperationAllowed(task, NodeOperation.ROLLBACK);
            flowEngine.rejectTask(taskId, StrUtil.trim(request.getNodeKey()), null, variables, opinion);
        } else {
            flowEngine.rejectTask(taskId, null, variables, opinion);
        }
        return runtimeResponse(task.getInstanceId());
    }

    public FlowRuntimeResponse rollback(Long taskId, FlowTaskRollbackRequest request) {
        if (request == null || StrUtil.isBlank(request.getNodeKey())) {
            throw new IllegalArgumentException("回退目标节点编码不能为空");
        }
        FlowTask task = task(taskId);
        assertNodeOperationAllowed(task, NodeOperation.ROLLBACK);
        flowEngine.rejectTask(taskId, StrUtil.trim(request.getNodeKey()), null,
                variables(request.getVariables()), request.getOpinion());
        return runtimeResponse(task.getInstanceId());
    }

    public FlowRuntimeResponse transfer(Long taskId, FlowTaskAssignRequest request) {
        FlowTask task = task(taskId);
        assertNodeOperationAllowed(task, NodeOperation.TRANSFER);
        Integer transferType = request == null || request.getTransferType() == null ? 1 : request.getTransferType();
        if (Integer.valueOf(1).equals(transferType)) {
            flowEngine.transferTask(taskId, null, firstAssignee(request),
                    variables(request.getVariables()), request.getOpinion());
        } else if (Integer.valueOf(2).equals(transferType)) {
            flowEngine.delegateTask(taskId, null, firstAssignee(request),
                    variables(request.getVariables()), request.getOpinion());
        } else if (Integer.valueOf(3).equals(transferType)) {
            flowEngine.agentTask(taskId, null, assignees(request),
                    variables(request.getVariables()), request.getOpinion());
        } else {
            throw new IllegalArgumentException("转交方式不支持，transferType=" + transferType);
        }
        return runtimeResponse(task.getInstanceId());
    }

    public FlowRuntimeResponse delegate(Long taskId, FlowTaskAssignRequest request) {
        FlowTask task = task(taskId);
        flowEngine.delegateTask(taskId, firstAssignee(request), variables(request == null ? null : request.getVariables()));
        return runtimeResponse(task.getInstanceId());
    }

    public FlowRuntimeResponse resolve(Long taskId) {
        FlowTask task = task(taskId);
        flowEngine.resolveTask(taskId);
        return runtimeResponse(task.getInstanceId());
    }

    public FlowRuntimeResponse view(Long taskId) {
        // 抄送任务即建即归档，活动表可能不存在，回退历史任务获取实例 ID
        FlowTask task = flowEngine.queryService().getTask(taskId);
        Long instanceId = task != null ? task.getInstanceId() : hisTask(taskId).getInstanceId();
        flowEngine.viewTask(taskId);
        return runtimeResponse(instanceId);
    }

    public FlowRuntimeResponse copy(Long taskId, FlowTaskCopyRequest request) {
        FlowTask task = task(taskId);
        assertNodeOperationAllowed(task, NodeOperation.CC);
        flowEngine.copyTask(taskId, buildTaskActors(request == null ? null : request.getActors()), null,
                variables(request == null ? null : request.getVariables()),
                request == null ? null : request.getCopyReason());
        return runtimeResponse(task.getInstanceId());
    }

    public FlowRuntimeResponse addSign(Long taskId, FlowTaskAddSignRequest request) {
        FlowTask task = task(taskId);
        assertNodeOperationAllowed(task, NodeOperation.APPEND_NODE);
        FlowNodeModel nodeModel = addSignNode(request);
        boolean before = request == null || request.getSignType() == null || Integer.valueOf(1).equals(request.getSignType());
        if (!flowEngine.executeAppendNodeModel(taskId, nodeModel, null,
                variables(request == null ? null : request.getVariables()), before)) {
            throw new IllegalStateException("审批加签失败，taskId=" + taskId);
        }
        return runtimeResponse(task.getInstanceId());
    }

    public FlowRuntimeResponse removeSign(Long taskId, FlowTaskRemoveSignRequest request) {
        FlowTask task = task(taskId);
        assertNodeOperationAllowed(task, NodeOperation.APPEND_NODE);
        if (request == null || StrUtil.isBlank(request.getNodeKey())) {
            throw new IllegalArgumentException("加签节点编码不能为空");
        }
        if (!flowEngine.executeRemoveNodeModel(task.getInstanceId(), StrUtil.trim(request.getNodeKey()))) {
            throw new IllegalStateException("审批减签失败，nodeKey=" + request.getNodeKey());
        }
        return runtimeResponse(task.getInstanceId());
    }

    public FlowRuntimeResponse withdraw(Long hisTaskId, FlowTaskOperateRequest request) {
        FlowHisTask hisTask = hisTask(hisTaskId);
        if (!flowEngine.withdrawTask(hisTaskId, null, variables(request == null ? null : request.getVariables())).isPresent()) {
            throw new IllegalStateException("回退撤回失败，hisTaskId=" + hisTaskId);
        }
        return runtimeResponse(hisTask.getInstanceId());
    }

    public FlowRuntimeResponse reclaim(Long hisTaskId, FlowTaskOperateRequest request) {
        FlowHisTask hisTask = hisTask(hisTaskId);
        if (!flowEngine.reclaimTask(hisTaskId, null, variables(request == null ? null : request.getVariables())).isPresent()) {
            throw new IllegalStateException("回退拿回失败，hisTaskId=" + hisTaskId);
        }
        return runtimeResponse(hisTask.getInstanceId());
    }

    public FlowRuntimeResponse terminateByTask(Long taskId, FlowTaskOperateRequest request) {
        FlowTask task = task(taskId);
        assertHisInstanceState(task.getInstanceId(), InstanceState.ACTIVE, "只有审批中的流程才能终止");
        flowEngine.terminateByTask(taskId, null, variables(request == null ? null : request.getVariables()));
        return detail(task.getInstanceId());
    }

    public FlowRuntimeResponse revoke(Long instanceId) {
        assertHisInstanceState(instanceId, InstanceState.ACTIVE, "只有审批中的流程才能撤回");
        if (!flowEngine.revokeInstanceById(instanceId, null)) {
            throw new IllegalStateException("撤回流程失败，instanceId=" + instanceId);
        }
        return runtimeResponse(instanceId);
    }

    public FlowRuntimeResponse suspend(Long instanceId) {
        assertHisInstanceState(instanceId, InstanceState.ACTIVE, "只有审批中的流程才能挂起");
        if (!flowEngine.suspendInstanceById(instanceId, null)) {
            throw new IllegalStateException("挂起流程失败，instanceId=" + instanceId);
        }
        return runtimeResponse(instanceId);
    }

    public FlowRuntimeResponse active(Long instanceId) {
        assertHisInstanceState(instanceId, InstanceState.SUSPENDED, "只有已暂停的流程才能激活");
        if (!flowEngine.activeInstanceById(instanceId, null)) {
            throw new IllegalStateException("激活流程失败，instanceId=" + instanceId);
        }
        return runtimeResponse(instanceId);
    }

    /**
     * 提交草稿并继续执行流程。
     *
     * @param instanceId 流程实例 ID
     * @param request 提交请求
     * @return 提交后的实例详情
     */
    public FlowRuntimeResponse submit(Long instanceId, FlowTaskOperateRequest request) {
        if (instanceId == null) {
            throw new IllegalArgumentException("流程实例 ID 不能为空");
        }
        FlowHisInstance hisInstance = flowEngine.queryService().getHisInstance(instanceId);
        if (hisInstance == null) {
            throw new IllegalArgumentException("流程实例不存在，instanceId=" + instanceId);
        }
        if (!InstanceState.DRAFT.value().equals(hisInstance.getInstanceState())) {
            throw new IllegalStateException("只有暂存状态的流程实例才能提交，instanceId=" + instanceId);
        }
        flowEngine.resumeInstance(instanceId, null, variables(request == null ? null : request.getVariables()));
        return runtimeResponse(instanceId);
    }

    public FlowRuntimeResponse destroy(Long instanceId, FlowTaskOperateRequest request) {
        assertNotHisInstanceState(instanceId, InstanceState.DESTROYED, "流程已作废，不能重复作废");
        if (!flowEngine.destroyByInstanceId(instanceId, variables(request == null ? null : request.getVariables()))) {
            throw new IllegalStateException("作废流程失败，instanceId=" + instanceId);
        }
        return runtimeResponse(instanceId);
    }

    public List<FlowHisTask> historyTasks(Long instanceId) {
        if (instanceId == null) {
            throw new IllegalArgumentException("流程实例 ID 不能为空");
        }
        return flowEngine.queryService().getHisTasksByInstanceId(instanceId);
    }

    /**
     * 查询流程实例审批轨迹。
     *
     * <p>同一 {@code nodeKey} 下多条任务（会签、或签、票签等并行场景）会合并为单条 record，
     * 每人各自的办理详情（状态、意见、完成时间）挂在 {@link Assignee} 内部。</p>
     */
    public List<FlowInstanceApprovalRecordResponse> approvalRecords(Long instanceId) {
        if (instanceId == null) {
            throw new IllegalArgumentException("流程实例 ID 不能为空");
        }
        FlowInstance instance = flowEngine.queryService().getInstance(instanceId);
        FlowHisInstance hisInstance = flowEngine.queryService().getHisInstance(instanceId);
        if (instance == null && hisInstance == null) {
            throw new IllegalArgumentException("流程实例不存在，instanceId=" + instanceId);
        }

        // 一次拉取流程模型，按 nodeKey 解析节点 setType，复用于历史/活动任务
        FlowProcessModel processModel = flowEngine.runtimeService().getProcessModelByInstanceId(instanceId);

        // taskId → {taskState, finishTime, opinion}，用于填充合并后每个 assignee 的个人办理详情
        Map<Long, TaskDetail> taskDetailMap = new LinkedHashMap<>();

        List<FlowInstanceApprovalRecordResponse> records = new ArrayList<>();
        for (FlowHisTask hisTask : flowEngine.queryService().getHisTasksByInstanceId(instanceId)) {
            if (isManualCopyTask(hisTask, processModel)) {
                continue;
            }
            taskDetailMap.put(hisTask.getId(), new TaskDetail(hisTask.getTaskState(), hisTask.getFinishTime(), hisTask.getOpinion(), hisTask.getCreateId()));
            records.add(toApprovalRecord(hisTask,
                    flowEngine.queryService().getHisTaskActorsByTaskId(hisTask.getId()),
                    hisTask.getTaskState(), hisTask.getFinishTime(), processModel));
        }
        for (FlowTask task : flowEngine.queryService().getTasksByInstanceId(instanceId)) {
            if (isManualCopyTask(task, processModel)) {
                continue;
            }
            taskDetailMap.put(task.getId(), new TaskDetail(null, null, null, null));
            records.add(toApprovalRecord(task,
                    flowEngine.queryService().getTaskActorsByTaskId(task.getId()),
                    null, null, processModel));
        }

        // 按 createTime 升序
        records.sort(Comparator.comparing(
                FlowInstanceApprovalRecordResponse::getCreateTime,
                Comparator.nullsLast(Comparator.naturalOrder())));

        // 合并同 nodeKey + 同 nodeStatus 的记录（会签/或签/票签场景）
        records = mergeRecords(records, taskDetailMap);

        // 终态实例追加合成结束节点（审批中/草稿/暂停不展示）
        appendEndRecord(records, hisInstance);
        return records;
    }

    /**
     * 合并同 nodeKey 的审批记录（顺序/会签/或签/票签），保留每人的 task 级办理详情。
     *
     * <p>合并规则：</p>
     * <ul>
     *     <li>审批节点（performType=1 顺序 / 2 会签 / 3 或签 / 4 票签）按 {@code nodeKey} 合并，
     *     不区分 nodeStatus —— 允许 1 人已完成 + 1 人待办合并在同一节点下。</li>
     *     <li>其他节点（发起、抄送等非任务节点）按 {@code nodeKey + "#" + nodeStatus} 合并，
     *     不同状态保持独立。</li>
     * </ul>
     *
     * <p>合并后 nodeStatus 优先级：IN_PROGRESS &gt; REJECTED &gt; COMPLETED，
     * 即只要还有人待办就是"待处理"，全部驳回才是"已驳回"。</p>
     */
    private List<FlowInstanceApprovalRecordResponse> mergeRecords(
            List<FlowInstanceApprovalRecordResponse> records,
            Map<Long, TaskDetail> taskDetailMap) {

        // mergeKey → 合并结果
        Map<String, FlowInstanceApprovalRecordResponse> merged = new LinkedHashMap<>();

        for (FlowInstanceApprovalRecordResponse r : records) {
            if (r.getType() != null && r.getType() == -1) {
                continue;
            }
            // 审批节点（顺序=1 / 会签=2 / 或签=3 / 票签=4）按 nodeKey 合并；其他按 nodeKey + nodeStatus
            boolean isApprovalNode = r.getPerformType() != null
                    && (r.getPerformType() == 1 || r.getPerformType() == 2
                            || r.getPerformType() == 3 || r.getPerformType() == 4);
            String key = isApprovalNode ? r.getNodeKey() : r.getNodeKey() + "#" + r.getNodeStatus();

            FlowInstanceApprovalRecordResponse existing = merged.get(key);
            if (existing == null) {
                merged.put(key, r);
            } else {
                // 合并 assignees（按 taskId 去重）
                Set<Long> existingTaskIds = existing.getAssignees().stream()
                        .map(FlowInstanceApprovalRecordResponse.Assignee::getTaskId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                for (FlowInstanceApprovalRecordResponse.Assignee a : r.getAssignees()) {
                    if (a.getTaskId() == null || !existingTaskIds.contains(a.getTaskId())) {
                        existing.getAssignees().add(a);
                    }
                }
                // finishTime 取最晚
                if (r.getFinishTime() != null
                        && (existing.getFinishTime() == null || r.getFinishTime().after(existing.getFinishTime()))) {
                    existing.setFinishTime(r.getFinishTime());
                }
                // 聚合 nodeStatus：IN_PROGRESS(1) > REJECTED(2) > COMPLETED(0)
                existing.setNodeStatus(aggregateNodeStatus(existing.getNodeStatus(), r.getNodeStatus()));
                ApprovalNodeStatusEnum aggStatus = ApprovalNodeStatusEnum.fromValue(existing.getNodeStatus());
                if (aggStatus != null) {
                    existing.setNodeStatusText(resolveNodeStatusText(existing.getType(), aggStatus));
                }
            }
        }

        // 为每个 assignee 填充 task 级办理详情
        fillAssigneeDetails(merged.values(), taskDetailMap);

        // 按 createTime 排序回原顺序
        List<FlowInstanceApprovalRecordResponse> result = new ArrayList<>(merged.values());
        result.sort(Comparator.comparing(
                FlowInstanceApprovalRecordResponse::getCreateTime,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return result;
    }

    /**
     * 为合并后的每条记录的 assignee 填充 task 级办理详情（状态、意见、完成时间）。
     *
     * <p>按节点类型分派不同的解析策略，核心区别在于 {@code detail.actorId}
     * （即任务 createId）在不同节点类型下语义不同：</p>
     * <ul>
     *     <li>抄送节点（COPY）→ createId 是抄送发起人，所有 assignee 直接取 task 详情</li>
     *     <li>或签节点（performType=3）→ 同一 task 多人共享，按 createId 区分操作人/未操作人</li>
     *     <li>其他节点（顺序/会签/票签/发起人/单人审批）→ 每人独立 task，直接取详情</li>
     * </ul>
     */
    private void fillAssigneeDetails(
            Collection<FlowInstanceApprovalRecordResponse> records,
            Map<Long, TaskDetail> taskDetailMap) {
        for (FlowInstanceApprovalRecordResponse r : records) {
            for (FlowInstanceApprovalRecordResponse.Assignee a : r.getAssignees()) {
                if (a.getTaskId() == null) {
                    continue;
                }
                TaskDetail detail = taskDetailMap.get(a.getTaskId());
                if (detail == null) {
                    continue;
                }
                fillAssigneeDetail(a, r, detail);
            }
        }
    }

    /**
     * 按 nodeType + performType 分派策略，填充单个 assignee 的办理详情。
     *
     * <p>策略优先级：</p>
     * <ol>
     *     <li>活动任务（detail.actorId 为空）→ 待处理</li>
     *     <li>或签 + 非操作人 → 已跳过</li>
     *     <li>其他（含抄送/顺序/会签/票签/发起人/操作人）→ 直接取 task 详情</li>
     * </ol>
     */
    private void fillAssigneeDetail(
            FlowInstanceApprovalRecordResponse.Assignee a,
            FlowInstanceApprovalRecordResponse r,
            TaskDetail detail) {
        // 活动任务（在活动表中，无人操作）→ 待处理
        if (detail.actorId == null) {
            a.setTaskStatus(null);
            a.setTaskStatusText(ApprovalNodeStatusEnum.IN_PROGRESS.getLabel());
            return;
        }
        // 或签节点：同一 task 多人共享，createId 是实际操作人，其余人标记跳过
        if (isOrSign(r.getPerformType()) && !detail.actorId.equals(a.getId())) {
            a.setTaskStatus(null);
            a.setTaskStatusText(ApprovalNodeStatusEnum.SKIPPED.getLabel());
            return;
        }
        // 抄送/顺序/会签/票签/发起人/单人审批 → 直接取 task 详情
        applyTaskDetail(a, r.getType(), detail);
    }

    /** 或签：performType=3，同一 task 多个 assignee 共享，1 人办理后其余人跳过。 */
    private boolean isOrSign(Integer performType) {
        return performType != null && performType == 3;
    }

    /** 将 TaskDetail 的状态、意见、完成时间写入 assignee。 */
    private void applyTaskDetail(
            FlowInstanceApprovalRecordResponse.Assignee a,
            Integer nodeType,
            TaskDetail detail) {
        ApprovalNodeStatusEnum status = mapTaskStateToNodeStatus(detail.taskState);
        a.setTaskStatus(detail.taskState);
        a.setTaskStatusText(resolveNodeStatusText(nodeType, status));
        a.setOpinion(detail.opinion);
        a.setFinishTime(detail.finishTime);
    }

    /**
     * 判断任务是否为手动抄送（taskType=抄送 且 nodeKey 不在流程定义中）。
     *
     * <p>手动抄送生成的任务从审批任务 clone 而来，taskKey 指向原审批节点
     * （而非抄送节点），因此 taskType=COPY 但 taskKey 对应的节点类型不是 COPY。
     * 流程定义中的抄送节点（CopyNodeModel）taskKey 就是抄送节点 key，类型为 COPY，
     * 仍然展示在审批轨迹中。</p>
     */
    private boolean isManualCopyTask(FlowTask task, FlowProcessModel processModel) {
        if (task == null || !TaskType.COPY.eq(task.getTaskType())) {
            return false;
        }
        if (processModel == null) {
            return true;
        }
        FlowNodeModel node = processModel.getNode(task.getTaskKey());
        // taskKey 对应的节点不是抄送节点类型 → 手动抄送（从审批任务 clone 而来，taskKey 指向审批节点）
        return node == null || !FlowNodeTypeEnum.COPY.eq(node.getType());
    }

    /**
     * 按节点类型解析 nodeStatusText，COMPLETED 时区分发起/抄送/审批。
     */
    private String resolveNodeStatusText(Integer nodeType, ApprovalNodeStatusEnum status) {
        if (status == ApprovalNodeStatusEnum.COMPLETED && nodeType != null) {
            if (nodeType == FlowNodeTypeEnum.START.value()) {
                return "提交申请";
            }
            if (nodeType == FlowNodeTypeEnum.COPY.value()) {
                return "已抄送";
            }
        }
        return status.getLabel();
    }

    /**
     * 聚合两个 nodeStatus，优先级：
     * <ul>
     *     <li>IN_PROGRESS(1) — 只要有人在办，节点就是"待处理"</li>
     *     <li>SKIPPED(5) — 最低，被任何其他状态覆盖</li>
     *     <li>COMPLETED(0) — 低于所有非跳过终态</li>
     *     <li>REJECTED(2) / REVOKED(3) / TERMINATED(4) — 任何一人驳回/终止，节点就是该终态</li>
     * </ul>
     */
    private Integer aggregateNodeStatus(Integer a, Integer b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        // IN_PROGRESS 优先（节点仍在进行）
        if (a == 1 || b == 1) {
            return 1;
        }
        // SKIPPED 最低，取另一个
        if (a == 5) {
            return b;
        }
        if (b == 5) {
            return a;
        }
        // COMPLETED 低于其他终态（REJECTED / REVOKED / TERMINATED）
        if (a == 0) {
            return b;
        }
        if (b == 0) {
            return a;
        }
        // 两个都是非完成终态，取第一个
        return a;
    }

    /** task 级办理详情（内部用，不对外暴露）。 */
    private static class TaskDetail {
        final Integer taskState;
        final Date finishTime;
        final String opinion;
        /** 实际操作人 ID（来自 hisTask.createId），活动任务为 null。 */
        final String actorId;
        TaskDetail(Integer taskState, Date finishTime, String opinion, String actorId) {
            this.taskState = taskState;
            this.finishTime = finishTime;
            this.opinion = opinion;
            this.actorId = actorId;
        }
    }

    /**
     * 按流程实例终态追加合成结束节点。
     *
     * <p>映射规则（对齐前端 FlowApprovalTimeline 展示）：</p>
     * <ul>
     *     <li>ACTIVE / DRAFT / SUSPENDED → 不展示结束节点</li>
     *     <li>REVOKED → "结束"，副标题 "流程已撤回"</li>
     *     <li>COMPLETED / AUTO_PASS → "结束"，副标题 "审批通过，流程结束"</li>
     *     <li>REJECTED / AUTO_REJECT / TIMEOUT / TERMINATED / DESTROYED → "结束"，副标题 "审批被驳回，流程结束"</li>
     * </ul>
     *
     * @param records 已组装的审批轨迹列表（按到达时间升序）
     * @param hisInstance 历史实例（审批中为 null）
     */
    private void appendEndRecord(List<FlowInstanceApprovalRecordResponse> records, FlowHisInstance hisInstance) {
        if (hisInstance == null) {
            // 活动实例（审批中），不展示结束节点
            return;
        }
        Integer stateValue = hisInstance.getInstanceState();
        if (stateValue == null) {
            return;
        }
        // 流程未终结（审批中 / 草稿 / 暂停）：不展示结束节点
        if (InstanceState.ACTIVE.eq(stateValue)
                || InstanceState.DRAFT.eq(stateValue)
                || InstanceState.SUSPENDED.eq(stateValue)) {
            return;
        }

        FlowInstanceApprovalRecordResponse end = new FlowInstanceApprovalRecordResponse();
        end.setNodeName("结束");
        end.setType(FlowNodeTypeEnum.END.value());
        end.setCreateTime(hisInstance.getEndTime());
        end.setFinishTime(hisInstance.getEndTime());

        if (InstanceState.REVOKED.eq(stateValue)) {
            end.setNodeStatus(ApprovalNodeStatusEnum.REVOKED.getValue());
            end.setNodeStatusText(ApprovalNodeStatusEnum.REVOKED.getLabel());
            end.setSubtitle("流程已撤回");
        } else if (InstanceState.COMPLETED.eq(stateValue) || InstanceState.AUTO_PASS.eq(stateValue)) {
            end.setNodeStatus(ApprovalNodeStatusEnum.COMPLETED.getValue());
            end.setNodeStatusText(ApprovalNodeStatusEnum.COMPLETED.getLabel());
            end.setSubtitle("审批通过，流程结束");
        } else {
            // REJECTED / AUTO_REJECT / TIMEOUT / TERMINATED / DESTROYED 统一按"已终止"语义展示
            end.setNodeStatus(ApprovalNodeStatusEnum.TERMINATED.getValue());
            end.setNodeStatusText(ApprovalNodeStatusEnum.TERMINATED.getLabel());
            end.setSubtitle("审批被驳回，流程结束");
        }
        records.add(end);
    }

    private void assertStartRequest(FlowStartProcessRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("发起流程请求不能为空");
        }
        if (request.getProcessId() == null && StrUtil.isBlank(request.getProcessKey())) {
            throw new IllegalArgumentException("流程定义 ID 和流程 key 不能同时为空");
        }
    }

    private java.util.function.Supplier<FlowInstance> instanceSupplier(String businessKey) {
        return StrUtil.isBlank(businessKey) ? null : () -> FlowInstance.of(StrUtil.trim(businessKey));
    }

    private Map<String, Object> variables(Map<String, Object> variables) {
        return CollUtil.isEmpty(variables) ? Collections.emptyMap() : variables;
    }

    private FlowHisTask hisTask(Long hisTaskId) {
        if (hisTaskId == null) {
            throw new IllegalArgumentException("历史任务 ID 不能为空");
        }
        FlowHisTask hisTask = flowEngine.queryService().getHisTask(hisTaskId);
        if (hisTask == null) {
            throw new IllegalArgumentException("历史任务不存在，hisTaskId=" + hisTaskId);
        }
        return hisTask;
    }

    private FlowCreator firstAssignee(FlowTaskAssignRequest request) {
        List<FlowCreator> creators = assignees(request);
        return creators.get(0);
    }

    private List<FlowCreator> assignees(FlowTaskAssignRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("转交人员不能为空");
        }
        List<FlowCreator> creators = new ArrayList<>();
        if (CollUtil.isNotEmpty(request.getActors())) {
            for (FlowTaskActorParam actor : request.getActors()) {
                if (actor == null || StrUtil.isBlank(actor.getActorId())) {
                    continue;
                }
                creators.add(FlowCreator.of(StrUtil.trim(actor.getActorId()),
                        StrUtil.blankToDefault(StrUtil.trim(actor.getActorName()), "")));
            }
        }
        if (creators.isEmpty()) {
            throw new IllegalArgumentException("转交人员不能为空");
        }
        return creators;
    }

    private List<FlowTaskActor> buildTaskActors(List<FlowTaskActorParam> params) {
        if (CollUtil.isEmpty(params)) {
            return Collections.emptyList();
        }
        List<FlowTaskActor> actors = new ArrayList<>();
        for (FlowTaskActorParam param : params) {
            if (param == null || StrUtil.isBlank(param.getActorId())) {
                continue;
            }
            FlowTaskActor actor = new FlowTaskActor();
            actor.setActorId(StrUtil.trim(param.getActorId()));
            actor.setActorName(StrUtil.blankToDefault(StrUtil.trim(param.getActorName()), ""));
            actor.setActorType(param.getActorType());
            actors.add(actor);
        }
        return actors;
    }

    private FlowNodeModel addSignNode(FlowTaskAddSignRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("审批加签请求不能为空");
        }
        if (StrUtil.isBlank(request.getNodeName())) {
            throw new IllegalArgumentException("加签节点名称不能为空");
        }
        List<FlowNodeAssignee> assignees = nodeAssignees(request.getActors());
        if (CollUtil.isEmpty(assignees)) {
            throw new IllegalArgumentException("加签人员不能为空");
        }
        ApprovalNodeModel nodeModel = new ApprovalNodeModel();
        nodeModel.setNodeKey(StrUtil.blankToDefault(StrUtil.trim(request.getNodeKey()), signNodeKey()));
        nodeModel.setNodeName(StrUtil.trim(request.getNodeName()));
        nodeModel.setType(FlowNodeTypeEnum.APPROVAL.value());
        nodeModel.setSetType(FlowNodeSetTypeEnum.SPECIFY_MEMBERS.value());
        nodeModel.setExamineMode(PerformType.SEQUENTIAL.value());
        nodeModel.setNodeAssigneeList(assignees);
        return nodeModel;
    }

    private List<FlowNodeAssignee> nodeAssignees(List<FlowTaskActorParam> params) {
        if (CollUtil.isEmpty(params)) {
            return Collections.emptyList();
        }
        List<FlowNodeAssignee> assignees = new ArrayList<>();
        for (FlowTaskActorParam param : params) {
            if (param == null || StrUtil.isBlank(param.getActorId())) {
                continue;
            }
            FlowNodeAssignee assignee = new FlowNodeAssignee();
            assignee.setId(StrUtil.trim(param.getActorId()));
            assignee.setName(StrUtil.blankToDefault(StrUtil.trim(param.getActorName()), ""));
            assignee.setActorType(param.getActorType());
            assignees.add(assignee);
        }
        return assignees;
    }

    private String signNodeKey() {
        return "sign_" + UUID.randomUUID().toString().replace("-", "");
    }

    private void assertNodeOperationAllowed(FlowTask task, NodeOperation operation) {
        FlowNodeModel node = currentTaskNode(task);
        if (!FlowNodeTypeEnum.APPROVAL.eq(node.getType())) {
            throw new IllegalStateException(operation.label + "只能在审批节点操作，nodeKey=" + task.getTaskKey());
        }
        Boolean allowed = nodeOperationAllowed(node, operation);
        if (Boolean.FALSE.equals(allowed)) {
            throw new IllegalStateException("当前节点不允许" + operation.label + "，nodeKey=" + task.getTaskKey());
        }
    }

    private FlowNodeModel currentTaskNode(FlowTask task) {
        if (task == null) {
            throw new IllegalArgumentException("任务不能为空");
        }
        FlowNodeModel node = flowEngine.runtimeService()
                .getProcessModelByInstanceId(task.getInstanceId())
                .getNode(task.getTaskKey());
        if (node == null) {
            throw new IllegalStateException("当前任务节点不存在，nodeKey=" + task.getTaskKey());
        }
        return node;
    }

    private Boolean nodeOperationAllowed(FlowNodeModel node, NodeOperation operation) {
        if (!(node instanceof ApprovalNodeModel)) {
            return null;
        }
        ApprovalNodeModel approvalNode = (ApprovalNodeModel) node;
        if (operation == NodeOperation.TRANSFER) {
            return approvalNode.getAllowTransfer();
        }
        if (operation == NodeOperation.APPEND_NODE) {
            return approvalNode.getAllowAppendNode();
        }
        if (operation == NodeOperation.ROLLBACK) {
            return approvalNode.getAllowRollback();
        }
        if (operation == NodeOperation.CC) {
            return approvalNode.getAllowCc();
        }
        return null;
    }

    private enum NodeOperation {
        TRANSFER("审批转交"),
        APPEND_NODE("审批加签/减签"),
        ROLLBACK("审批回退"),
        CC("手动抄送");

        private final String label;

        NodeOperation(String label) {
            this.label = label;
        }
    }

    private FlowHisInstance hisInstance(Long instanceId) {
        if (instanceId == null) {
            throw new IllegalArgumentException("流程实例 ID 不能为空");
        }
        FlowHisInstance hisInstance = flowEngine.queryService().getHisInstance(instanceId);
        if (hisInstance == null) {
            throw new IllegalArgumentException("流程实例不存在，instanceId=" + instanceId);
        }
        return hisInstance;
    }

    private void assertHisInstanceState(Long instanceId, InstanceState expectedState, String message) {
        FlowHisInstance hisInstance = hisInstance(instanceId);
        if (!expectedState.value().equals(hisInstance.getInstanceState())) {
            throw new IllegalStateException(message + "，当前状态=" + instanceStateLabel(hisInstance.getInstanceState()));
        }
    }

    private void assertNotHisInstanceState(Long instanceId, InstanceState state, String message) {
        FlowHisInstance hisInstance = hisInstance(instanceId);
        if (state.value().equals(hisInstance.getInstanceState())) {
            throw new IllegalStateException(message);
        }
    }

    private String instanceStateLabel(Integer state) {
        if (state == null) {
            return "未知";
        }
        for (InstanceState item : InstanceState.values()) {
            if (item.value().equals(state)) {
                return item.label();
            }
        }
        return String.valueOf(state);
    }

    private Map<Long, List<FlowTaskActor>> taskActors(List<FlowTask> tasks) {
        Map<Long, List<FlowTaskActor>> data = new LinkedHashMap<>();
        if (tasks == null) {
            return data;
        }
        for (FlowTask task : tasks) {
            data.put(task.getId(), flowEngine.queryService().getTaskActorsByTaskId(task.getId()));
        }
        return data;
    }

    private FlowRuntimeResponse runtimeResponse(Long instanceId) {
        FlowInstance currentInstance = flowEngine.queryService().getInstance(instanceId);
        FlowHisInstance hisInstance = flowEngine.queryService().getHisInstance(instanceId);
        if (currentInstance == null && hisInstance == null) {
            throw new IllegalArgumentException("流程实例不存在，instanceId=" + instanceId);
        }
        FlowInstance source = currentInstance != null ? currentInstance : hisInstance;
        List<FlowTask> tasks = flowEngine.queryService().getTasksByInstanceId(instanceId);
        FlowRuntimeResponse response = new FlowRuntimeResponse();
        response.setInstanceId(instanceId);
        // source 已通过上方 throw 保证非 null，直接取值
        response.setTenantId(source.getTenantId());
        response.setBusinessKey(source.getBusinessKey());
        response.setCreateId(source.getCreateId());
        response.setCreateBy(source.getCreateBy());
        response.setCreateTime(source.getCreateTime());
        response.setCurrentNodeName(currentNodeName(currentInstance, hisInstance, source));
        response.setCurrentNodeKey(currentNodeKey(currentInstance, hisInstance, source));
        // hisInstance 在审批中（未结束）为 null，用 Optional 兜底
        java.util.Optional.ofNullable(hisInstance)
                .ifPresent(h -> {
                    response.setInstanceState(h.getInstanceState());
                    response.setEndTime(h.getEndTime());
                });
        // process 信息（从 FlowProcess 定义表组装）
        java.util.Optional.ofNullable(source.getProcessId())
                .ifPresent(pid -> {
                    try {
                        FlowProcess p = flowEngine.processService().getProcessById(pid);
                        FlowRuntimeResponse.Process process = new FlowRuntimeResponse.Process();
                        process.setProcessId(p.getId());
                        process.setProcessName(p.getProcessName());
                        process.setProcessIcon(p.getProcessIcon());
                        process.setProcessType(p.getProcessType());
                        process.setProcessVersion(p.getProcessVersion());
                        process.setInstanceUrl(p.getInstanceUrl());
                        process.setRemark(p.getRemark());
                        process.setFormSourceType(p.getFormSourceType());
                        response.setProcess(process);
                    } catch (Exception ignore) {
                        // process 查不到时保持 null，不影响实例主信息
                    }
                });
        response.setTask(currentTasks(tasks));
        return response;
    }

    private List<FlowRuntimeResponse.Task> currentTasks(List<FlowTask> tasks) {
        List<FlowRuntimeResponse.Task> currentTasks = new ArrayList<>();
        if (tasks == null) {
            return currentTasks;
        }
        for (FlowTask task : tasks) {
            currentTasks.add(FlowRuntimeResponse.Task.of(task,
                    flowEngine.queryService().getTaskActorsByTaskId(task.getId())));
        }
        return currentTasks;
    }

    private String currentNodeName(FlowInstance currentInstance, FlowHisInstance hisInstance, FlowInstance fallback) {
        if (currentInstance != null) {
            return currentInstance.getCurrentNodeName();
        }
        if (hisInstance != null) {
            return hisInstance.getCurrentNodeName();
        }
        return fallback == null ? null : fallback.getCurrentNodeName();
    }

    private String currentNodeKey(FlowInstance currentInstance, FlowHisInstance hisInstance, FlowInstance fallback) {
        if (currentInstance != null) {
            return currentInstance.getCurrentNodeKey();
        }
        if (hisInstance != null) {
            return hisInstance.getCurrentNodeKey();
        }
        return fallback == null ? null : fallback.getCurrentNodeKey();
    }

    /**
     * 引擎 TaskState → 审批轨迹节点状态映射。
     *
     * <p>引擎 TaskState 有 18 种取值（COMPLETED=2, AUTO_COMPLETED=8,
     * REJECTED=3, REJECT_END=7, AUTO_REJECTED=9, REVOKED=4, WITHDRAWN=16,
     * RECLAIMED=15, TERMINATED=6, DESTROYED=18, JUMP=1, AUTO_JUMP=10 …），
     * 审批轨迹统一归并为 {@link ApprovalNodeStatusEnum} 6 种终态。</p>
     */
    private static ApprovalNodeStatusEnum mapTaskStateToNodeStatus(Integer taskState) {
        if (taskState == null) {
            return ApprovalNodeStatusEnum.IN_PROGRESS;
        }
        switch (taskState) {
            // 正常完成
            case 2:  // COMPLETED
            case 8:  // AUTO_COMPLETED
                return ApprovalNodeStatusEnum.COMPLETED;
            // 驳回
            case 3:  // REJECTED
            case 7:  // REJECT_END
            case 9:  // AUTO_REJECTED
                return ApprovalNodeStatusEnum.REJECTED;
            // 撤回
            case 4:  // REVOKED
            case 16: // WITHDRAWN
            case 15: // RECLAIMED
                return ApprovalNodeStatusEnum.REVOKED;
            // 终止/作废
            case 6:  // TERMINATED
            case 18: // DESTROYED
                return ApprovalNodeStatusEnum.TERMINATED;
            // 跳过类：跳转、自动通过等
            case 1:  // JUMP
            case 10: // AUTO_JUMP
            case 11: // REJECT_JUMP
            case 12: // RE_APPROVE_JUMP
            case 14: // TRIGGER_JUMP
            case 17: // RESUMED
                return ApprovalNodeStatusEnum.SKIPPED;
            default:
                return ApprovalNodeStatusEnum.COMPLETED;
        }
    }

    private FlowInstanceApprovalRecordResponse toApprovalRecord(FlowTask task,
                                                               List<? extends FlowTaskActor> actors,
                                                               Integer taskState, Date finishTime,
                                                               FlowProcessModel processModel) {
        FlowInstanceApprovalRecordResponse r = new FlowInstanceApprovalRecordResponse();
        r.setNodeKey(task.getTaskKey());
        r.setNodeName(task.getTaskName());
        // taskType 与 FlowNodeMap 的 FlowNodeTypeEnum 值完全一致，直接用
        r.setType(task.getTaskType());

        ApprovalNodeStatusEnum status = mapTaskStateToNodeStatus(taskState);
        r.setNodeStatus(status.getValue());
        r.setNodeStatusText(resolveNodeStatusText(task.getTaskType(), status));

        r.setCreateTime(task.getCreateTime());
        r.setFinishTime(finishTime);

        // 最后办理人（活动节点取 createBy，历史节点取他处）
        r.setActorId(task.getCreateId());
        r.setActorName(task.getCreateBy());

        // 节点 setType：从流程模型取任务节点配置，映射出枚举值与中文名
        applySetType(r, processModel, task.getTaskKey());

        // 审批方式（执行类型）：从 task 直接取 performType，映射枚举中文名
        applyPerformType(r, task.getPerformType());

        // 完整办理人列表
        r.setAssignees(toAssignees(actors));
        return r;
    }

    /** 从 FlowTaskEnum.PerformType 映射审批方式值与中文名到响应。 */
    private void applyPerformType(FlowInstanceApprovalRecordResponse response, Integer performType) {
        if (performType == null) {
            return;
        }
        response.setPerformType(performType);
        FlowTaskEnum.PerformType pt = FlowTaskEnum.PerformType.of(performType);
        if (pt != null) {
            response.setPerformTypeName(pt.label());
        }
    }

    /**
     * 从流程模型解析节点 setType，写入响应。
     *
     * <p>仅 {@link TaskNodeModel} 子类（审批/发起/抄送等任务节点）携带 setType；
     * 路由/结束等节点 setType 保持 null。枚举值映射见 {@link FlowNodeSetTypeEnum}。</p>
     *
     * @param response 审批轨迹响应
     * @param processModel 流程模型
     * @param nodeKey 任务节点编码
     */
    private void applySetType(FlowInstanceApprovalRecordResponse response,
                              FlowProcessModel processModel, String nodeKey) {
        if (processModel == null || StrUtil.isBlank(nodeKey)) {
            return;
        }
        FlowNodeModel node = processModel.getNode(nodeKey);
        if (!(node instanceof TaskNodeModel)) {
            return;
        }
        Integer setType = ((TaskNodeModel) node).getSetType();
        if (setType == null) {
            return;
        }
        response.setSetType(setType);
        for (FlowNodeSetTypeEnum item : FlowNodeSetTypeEnum.values()) {
            if (item.value().equals(setType)) {
                response.setSetTypeName(item.label());
                return;
            }
        }
    }

    private List<FlowInstanceApprovalRecordResponse.Assignee> toAssignees(List<? extends FlowTaskActor> actors) {
        List<FlowInstanceApprovalRecordResponse.Assignee> result = new ArrayList<>();
        if (actors == null) {
            return result;
        }
        for (FlowTaskActor actor : actors) {
            if (actor == null) {
                continue;
            }
            FlowInstanceApprovalRecordResponse.Assignee a = new FlowInstanceApprovalRecordResponse.Assignee();
            a.setId(actor.getActorId());
            a.setName(actor.getActorName());
            a.setActorType(actor.getActorType());
            a.setTaskId(actor.getTaskId());
            result.add(a);
        }
        return result;
    }

}
