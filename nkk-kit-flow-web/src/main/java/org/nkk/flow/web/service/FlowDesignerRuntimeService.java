package org.nkk.flow.web.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.entity.FlowExtInstance;
import org.nkk.flow.entity.FlowHisInstance;
import org.nkk.flow.entity.FlowHisTask;
import org.nkk.flow.entity.FlowInstance;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.entity.FlowTaskActor;
import org.nkk.flow.enums.core.FlowInstanceEnum.InstanceState;
import org.nkk.flow.enums.core.FlowTaskEnum.TaskType;
import org.nkk.flow.enums.core.FlowTaskEnum.PerformType;
import org.nkk.flow.enums.node.FlowNodeSetTypeEnum;
import org.nkk.flow.enums.node.FlowNodeTypeEnum;
import org.nkk.flow.model.node.task.ApprovalNodeModel;
import org.nkk.flow.model.node.task.FlowNodeAssignee;
import org.nkk.flow.model.node.FlowNodeModel;
import org.nkk.flow.service.NkkFlowEngine;
import org.nkk.flow.web.model.FlowInstanceApprovalRecordResponse;
import org.nkk.flow.web.model.FlowInstanceDetailResponse;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
        FlowInstance instance = optional.orElseThrow(() -> new IllegalStateException("发起流程失败"));
        return runtimeResponse(instance.getId());
    }

    /**
     * 查询流程实例详情。
     *
     * @param instanceId 流程实例 ID
     * @return 实例详情
     */
    public FlowInstanceDetailResponse detail(Long instanceId) {
        if (instanceId == null) {
            throw new IllegalArgumentException("流程实例 ID 不能为空");
        }
        FlowInstance instance = flowEngine.queryService().getInstance(instanceId);
        FlowHisInstance hisInstance = flowEngine.queryService().getHisInstance(instanceId);
        if (instance == null && hisInstance == null) {
            throw new IllegalArgumentException("流程实例不存在，instanceId=" + instanceId);
        }
        FlowExtInstance extInstance = flowEngine.queryService().getExtInstance(instanceId);
        List<FlowTask> tasks = flowEngine.queryService().getTasksByInstanceId(instanceId);
        List<FlowHisTask> hisTasks = flowEngine.queryService().getHisTasksByInstanceId(instanceId);
        FlowInstanceDetailResponse response = new FlowInstanceDetailResponse();
        response.setInstance(instance);
        response.setHisInstance(hisInstance);
        response.setExtInstance(extInstance);
        response.setTasks(tasks);
        response.setTaskActors(taskActors(tasks));
        response.setHisTasks(hisTasks);
        return response;
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
        FlowTask task = task(taskId);
        flowEngine.viewTask(taskId);
        return runtimeResponse(task.getInstanceId());
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

    public FlowInstanceDetailResponse terminateByTask(Long taskId, FlowTaskOperateRequest request) {
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
     * @param instanceId 流程实例 ID
     * @return 审批轨迹
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

        List<FlowInstanceApprovalRecordResponse> records = new ArrayList<>();
        for (FlowHisTask hisTask : flowEngine.queryService().getHisTasksByInstanceId(instanceId)) {
            records.add(toApprovalRecord(hisTask,
                    flowEngine.queryService().getHisTaskActorsByTaskId(hisTask.getId()),
                    hisTask.getTaskState(), hisTask.getFinishTime()));
        }
        for (FlowTask task : flowEngine.queryService().getTasksByInstanceId(instanceId)) {
            records.add(toApprovalRecord(task,
                    flowEngine.queryService().getTaskActorsByTaskId(task.getId()),
                    null, null));
        }
        records.sort(Comparator
                .comparing(FlowInstanceApprovalRecordResponse::getCreateTime, Comparator.nullsLast(Date::compareTo))
                .thenComparing(FlowInstanceApprovalRecordResponse::getId, Comparator.nullsLast(Long::compareTo)));
        return records;
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
        FlowInstance source = currentInstance == null ? hisInstance : currentInstance;
        List<FlowTask> tasks = flowEngine.queryService().getTasksByInstanceId(instanceId);
        FlowRuntimeResponse response = new FlowRuntimeResponse();
        response.setInstanceId(instanceId);
        response.setTenantId(source == null ? null : source.getTenantId());
        response.setBusinessKey(source == null ? null : source.getBusinessKey());
        response.setCurrentNodeName(currentNodeName(currentInstance, hisInstance, source));
        response.setCurrentNodeKey(currentNodeKey(currentInstance, hisInstance, source));
        response.setInstanceState(hisInstance == null ? null : hisInstance.getInstanceState());
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

    private FlowInstanceApprovalRecordResponse toApprovalRecord(FlowTask task, List<? extends FlowTaskActor> actors,
                                                               Integer taskState, Date finishTime) {
        FlowInstanceApprovalRecordResponse response = new FlowInstanceApprovalRecordResponse();
        response.setId(task.getId());
        response.setCreateId(task.getCreateId());
        response.setCreateBy(task.getCreateBy());
        response.setCreateTime(task.getCreateTime());
        response.setInstanceId(task.getInstanceId());
        response.setTaskId(task.getId());
        response.setTaskName(task.getTaskName());
        response.setTaskKey(task.getTaskKey());
        response.setType(task.getTaskType());
        response.setTaskType(task.getTaskType());
        response.setTaskState(taskState);
        response.setFinishTime(finishTime);
        response.setContent(approvalRecordContent(task, actors));
        return response;
    }

    private FlowInstanceApprovalRecordResponse.Content approvalRecordContent(FlowTask task,
                                                                            List<? extends FlowTaskActor> actors) {
        if (TaskType.START.value().equals(task.getTaskType())) {
            return null;
        }
        List<FlowInstanceApprovalRecordResponse.NodeUser> nodeUsers = nodeUsers(actors);
        if (task.getOpinion() == null && nodeUsers.isEmpty()) {
            return null;
        }
        FlowInstanceApprovalRecordResponse.Content content = new FlowInstanceApprovalRecordResponse.Content();
        content.setOpinion(task.getOpinion());
        content.setNodeUserList(nodeUsers);
        return content;
    }

    private List<FlowInstanceApprovalRecordResponse.NodeUser> nodeUsers(List<? extends FlowTaskActor> actors) {
        List<FlowInstanceApprovalRecordResponse.NodeUser> users = new ArrayList<>();
        if (actors == null) {
            return users;
        }
        for (FlowTaskActor actor : actors) {
            if (actor == null) {
                continue;
            }
            FlowInstanceApprovalRecordResponse.NodeUser user = new FlowInstanceApprovalRecordResponse.NodeUser();
            user.setId(actor.getActorId());
            user.setName(actor.getActorName());
            user.setWeight(actor.getWeight());
            user.setActorType(actor.getActorType());
            users.add(user);
        }
        return users;
    }

}
