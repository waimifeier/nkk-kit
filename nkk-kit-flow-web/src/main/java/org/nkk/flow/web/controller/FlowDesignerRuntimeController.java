package org.nkk.flow.web.controller;

import org.nkk.core.beans.common.Result;
import org.nkk.flow.entity.FlowHisTask;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.entity.FlowTaskActor;
import org.nkk.flow.web.model.FlowInstanceApprovalRecordResponse;
import org.nkk.flow.web.model.FlowRuntimeResponse;
import org.nkk.flow.web.model.FlowInstanceDetailResponse;
import org.nkk.flow.web.model.FlowStartProcessRequest;
import org.nkk.flow.web.model.FlowTaskAddSignRequest;
import org.nkk.flow.web.model.FlowTaskAssignRequest;
import org.nkk.flow.web.model.FlowTaskCopyRequest;
import org.nkk.flow.web.model.FlowTaskOperateRequest;
import org.nkk.flow.web.model.FlowTaskRemoveSignRequest;
import org.nkk.flow.web.model.FlowTaskRollbackRequest;
import org.nkk.flow.web.service.FlowDesignerRuntimeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 流程设计器运行时接口。
 */
@RestController
@RequestMapping("${flow.web.api-prefix:/flow}/designer/runtime")
public class FlowDesignerRuntimeController {

    private final FlowDesignerRuntimeService runtimeService;

    public FlowDesignerRuntimeController(FlowDesignerRuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    /**
     * 发起流程实例。
     *
     * <p>请求体示例：{@code {"processId":1785467714628,"businessKey":"leave-001","variables":{"day":3}}}</p>
     *
     * @param request 发起流程请求
     * @return 发起结果，包含实例和当前活动任务
     */
    @PostMapping("/instances/start")
    public Result<FlowRuntimeResponse> start(@RequestBody FlowStartProcessRequest request) {
        return Result.ok(runtimeService.start(request));
    }

    /**
     * 查询流程实例详情。
     *
     * @param instanceId 流程实例 ID；示例值：{@code 1785467714628}
     * @return 实例、当前任务、任务参与人和历史任务
     */
    @GetMapping("/instances/{instanceId}")
    public Result<FlowInstanceDetailResponse> detail(@PathVariable Long instanceId) {
        return Result.ok(runtimeService.detail(instanceId));
    }

    /**
     * 提交草稿流程实例并继续流转。
     *
     * <p>请求体示例：{@code {"variables":{"day":3,"reason":"事假"}}}</p>
     *
     * @param instanceId 流程实例 ID；示例值：{@code 1785467714628}
     * @param request 提交请求，可补充或覆盖草稿阶段保存的流程变量
     * @return 提交后的实例详情
     */
    @PostMapping("/instances/{instanceId}/submit")
    public Result<FlowRuntimeResponse> submit(@PathVariable Long instanceId,
                                              @RequestBody(required = false) FlowTaskOperateRequest request) {
        return Result.ok(runtimeService.submit(instanceId, request));
    }

    /**
     * 查询流程实例当前活动任务。
     *
     * @param instanceId 流程实例 ID；示例值：{@code 1785467714628}
     * @return 当前活动任务列表
     */
    @GetMapping("/instances/{instanceId}/tasks")
    public Result<List<FlowTask>> tasks(@PathVariable Long instanceId) {
        return Result.ok(runtimeService.tasks(instanceId));
    }

    /**
     * 查询流程实例历史任务。
     *
     * @param instanceId 流程实例 ID；示例值：{@code 1785467714628}
     * @return 历史任务列表
     */
    @GetMapping("/instances/{instanceId}/history/tasks")
    public Result<List<FlowHisTask>> historyTasks(@PathVariable Long instanceId) {
        return Result.ok(runtimeService.historyTasks(instanceId));
    }

    /**
     * 查询流程实例审批轨迹。
     *
     * <p>返回从发起节点到当前节点的流程轨迹，包含节点名称、任务 ID、操作人、审批意见和节点参与人。</p>
     *
     * @param instanceId 流程实例 ID；示例值：{@code 1785467714628}
     * @return 审批轨迹列表
     */
    @GetMapping("/instances/{instanceId}/approval-records")
    public Result<List<FlowInstanceApprovalRecordResponse>> approvalRecords(@PathVariable Long instanceId) {
        return Result.ok(runtimeService.approvalRecords(instanceId));
    }

    /**
     * 撤回流程实例。
     *
     * @param instanceId 流程实例 ID；示例值：{@code 1785467714628}
     * @return 撤回后的实例详情
     */
    @PostMapping("/instances/{instanceId}/revoke")
    public Result<FlowRuntimeResponse> revoke(@PathVariable Long instanceId) {
        return Result.ok(runtimeService.revoke(instanceId));
    }

    /**
     * 挂起流程实例。
     *
     * @param instanceId 流程实例 ID；示例值：{@code 1785467714628}
     * @return 挂起后的实例详情
     */
    @PostMapping("/instances/{instanceId}/suspend")
    public Result<FlowRuntimeResponse> suspend(@PathVariable Long instanceId) {
        return Result.ok(runtimeService.suspend(instanceId));
    }

    /**
     * 激活流程实例。
     *
     * @param instanceId 流程实例 ID；示例值：{@code 1785467714628}
     * @return 激活后的实例详情
     */
    @PostMapping("/instances/{instanceId}/active")
    public Result<FlowRuntimeResponse> active(@PathVariable Long instanceId) {
        return Result.ok(runtimeService.active(instanceId));
    }

    /**
     * 作废流程实例。
     *
     * <p>请求体示例：{@code {"variables":{"comment":"业务单据已取消"}}}</p>
     *
     * @param instanceId 流程实例 ID；示例值：{@code 1785467714628}
     * @param request 作废请求
     * @return 作废后的实例详情
     */
    @PostMapping("/instances/{instanceId}/destroy")
    public Result<FlowRuntimeResponse> destroy(@PathVariable Long instanceId,
                                               @RequestBody(required = false) FlowTaskOperateRequest request) {
        return Result.ok(runtimeService.destroy(instanceId, request));
    }

    /**
     * 查询任务详情。
     *
     * @param taskId 任务 ID；示例值：{@code 1785467714628}
     * @return 任务详情
     */
    @GetMapping("/tasks/{taskId}")
    public Result<FlowTask> task(@PathVariable Long taskId) {
        return Result.ok(runtimeService.task(taskId));
    }

    /**
     * 查询任务参与人。
     *
     * @param taskId 任务 ID；示例值：{@code 1785467714628}
     * @return 任务参与人列表
     */
    @GetMapping("/tasks/{taskId}/actors")
    public Result<List<FlowTaskActor>> taskActors(@PathVariable Long taskId) {
        return Result.ok(runtimeService.taskActors(taskId));
    }

    /**
     * 办理通过任务。
     *
     * <p>请求体示例：{@code {"opinion":"同意","variables":{"day":3}}}</p>
     *
     * @param taskId 任务 ID；示例值：{@code 1785467714628}
     * @param request 办理请求
     * @return 办理后的实例详情
     */
    @PostMapping("/tasks/{taskId}/complete")
    public Result<FlowRuntimeResponse> complete(@PathVariable Long taskId,
                                                @RequestBody(required = false) FlowTaskOperateRequest request) {
        return Result.ok(runtimeService.complete(taskId, request));
    }

    /**
     * 驳回任务。
     *
     * <p>请求体示例：{@code {"nodeKey":"flk_start","opinion":"资料不完整","variables":{"day":3}}}</p>
     *
     * @param taskId 任务 ID；示例值：{@code 1785467714628}
     * @param request 驳回请求，nodeKey 为空时按默认驳回规则处理
     * @return 驳回后的实例详情
     */
    @PostMapping("/tasks/{taskId}/reject")
    public Result<FlowRuntimeResponse> reject(@PathVariable Long taskId,
                                              @RequestBody(required = false) FlowTaskOperateRequest request) {
        return Result.ok(runtimeService.reject(taskId, request));
    }

    /**
     * 回退审批到指定节点。
     *
     * <p>该接口用于前端“回退审批”弹窗：用户选择一个可回退节点后，后端按 nodeKey 跳回该节点继续审批。</p>
     * <p>请求体示例：{@code {"nodeKey":"flk_start","opinion":"资料不完整","variables":{"day":3}}}</p>
     *
     * @param taskId 当前待办任务 ID；示例值：{@code 1785467714628}
     * @param request 回退请求，nodeKey 必填
     * @return 回退后的运行时信息
     */
    @PostMapping("/tasks/{taskId}/rollback")
    public Result<FlowRuntimeResponse> rollback(@PathVariable Long taskId,
                                                @RequestBody FlowTaskRollbackRequest request) {
        return Result.ok(runtimeService.rollback(taskId, request));
    }

    /**
     * 审批转交。
     *
     * <p>转交方式：1 转办，2 委派，3 代理。</p>
     * <p>请求体示例：{@code {"transferType":1,"actors":[{"actorId":"u2","actorName":"李四","actorType":0}],"opinion":"请李四处理"}}</p>
     *
     * @param taskId 任务 ID；示例值：{@code 1785467714628}
     * @param request 转交请求
     * @return 转交后的运行时信息
     */
    @PostMapping("/tasks/{taskId}/transfer")
    public Result<FlowRuntimeResponse> transfer(@PathVariable Long taskId,
                                                @RequestBody FlowTaskAssignRequest request) {
        return Result.ok(runtimeService.transfer(taskId, request));
    }

    /**
     * 阅读抄送任务。
     *
     * @param taskId 任务 ID；示例值：{@code 1785467714628}
     * @return 阅读后的实例详情
     */
    @PostMapping("/tasks/{taskId}/view")
    public Result<FlowRuntimeResponse> view(@PathVariable Long taskId) {
        return Result.ok(runtimeService.view(taskId));
    }

    /**
     * 手动抄送任务。
     *
     * <p>请求体示例：{@code {"copyReason":"请关注该审批","actors":[{"actorId":"u2","actorName":"李四","actorType":0}]}}</p>
     *
     * @param taskId 当前任务 ID；示例值：{@code 1785467714628}
     * @param request 抄送请求
     * @return 抄送后的运行时信息
     */
    @PostMapping("/tasks/{taskId}/copy")
    public Result<FlowRuntimeResponse> copy(@PathVariable Long taskId,
                                            @RequestBody(required = false) FlowTaskCopyRequest request) {
        return Result.ok(runtimeService.copy(taskId, request));
    }

    /**
     * 审批加签。
     *
     * <p>加签方式：1 前加签，2 后加签。</p>
     * <p>请求体示例：{@code {"signType":1,"nodeKey":"sign_u2","nodeName":"前加签","opinion":"请补充审批","actors":[{"actorId":"u2","actorName":"李四","actorType":0}]}}</p>
     *
     * @param taskId 任务 ID；示例值：{@code 1785467714628}
     * @param request 加签请求
     * @return 加签后的运行时信息
     */
    @PostMapping("/tasks/{taskId}/add-sign")
    public Result<FlowRuntimeResponse> addSign(@PathVariable Long taskId,
                                               @RequestBody(required = false) FlowTaskAddSignRequest request) {
        return Result.ok(runtimeService.addSign(taskId, request));
    }

    /**
     * 审批减签。
     *
     * <p>请求体示例：{@code {"nodeKey":"sign_abc","opinion":"取消该加签节点"}}</p>
     *
     * @param taskId 任务 ID；示例值：{@code 1785467714628}
     * @param request 减签请求
     * @return 减签后的运行时信息
     */
    @PostMapping("/tasks/{taskId}/remove-sign")
    public Result<FlowRuntimeResponse> removeSign(@PathVariable Long taskId,
                                                  @RequestBody(required = false) FlowTaskRemoveSignRequest request) {
        return Result.ok(runtimeService.removeSign(taskId, request));
    }

    /**
     * 撤回某条已办理历史任务，重新回到该历史任务对应节点继续流转。
     *
     * <p>该接口按 hisTaskId 定位目标节点，不用于前端“选择回退节点”的回退审批场景。</p>
     * <p>请求体示例：{@code {"variables":{"day":3}}}</p>
     *
     * @param hisTaskId 历史任务 ID；示例值：{@code 1785467714628}
     * @param request 回退请求
     * @return 回退后的运行时信息
     */
    @PostMapping("/history/tasks/{hisTaskId}/withdraw")
    public Result<FlowRuntimeResponse> withdraw(@PathVariable Long hisTaskId,
                                                @RequestBody(required = false) FlowTaskOperateRequest request) {
        return Result.ok(runtimeService.withdraw(hisTaskId, request));
    }

    /**
     * 拿回历史任务并重新发起办理。
     *
     * <p>请求体示例：{@code {"variables":{"day":3}}}</p>
     *
     * @param hisTaskId 历史任务 ID；示例值：{@code 1785467714628}
     * @param request 拿回请求
     * @return 拿回后的运行时信息
     */
    @PostMapping("/history/tasks/{hisTaskId}/reclaim")
    public Result<FlowRuntimeResponse> reclaim(@PathVariable Long hisTaskId,
                                               @RequestBody(required = false) FlowTaskOperateRequest request) {
        return Result.ok(runtimeService.reclaim(hisTaskId, request));
    }

    /**
     * 终止任务所在流程。
     *
     * <p>请求体示例：{@code {"variables":{"comment":"审批终止"}}}</p>
     *
     * @param taskId 任务 ID；示例值：{@code 1785467714628}
     * @param request 终止请求
     * @return 终止后的实例详情
     */
    @PostMapping("/tasks/{taskId}/terminate")
    public Result<FlowInstanceDetailResponse> terminate(@PathVariable Long taskId,
                                                        @RequestBody(required = false) FlowTaskOperateRequest request) {
        return Result.ok(runtimeService.terminateByTask(taskId, request));
    }
}
