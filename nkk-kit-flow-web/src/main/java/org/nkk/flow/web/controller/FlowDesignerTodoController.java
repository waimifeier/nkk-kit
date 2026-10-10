package org.nkk.flow.web.controller;

import org.nkk.core.beans.common.PageResult;
import org.nkk.core.beans.common.Result;
import org.nkk.flow.web.model.FlowApprovedPageRequest;
import org.nkk.flow.web.model.FlowCopiedPageRequest;
import org.nkk.flow.web.model.FlowMyAppliedPageRequest;
import org.nkk.flow.web.model.FlowPendingPageRequest;
import org.nkk.flow.web.model.FlowTodoRecordResponse;
import org.nkk.flow.web.service.FlowDesignerTodoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 流程设计器待办中心接口。
 *
 * <p>按查询场景拆分为 4 个独立接口，每个接口的"当前用户"语义清晰：
 * <ul>
 *     <li>{@code GET /my-applied} — 我发起的（当前用户 = 发起人）</li>
 *     <li>{@code GET /pending}    — 待我审批（当前用户 = 审批人）</li>
 *     <li>{@code GET /copied}     — 抄送给我（当前用户 = 抄送接收人）</li>
 *     <li>{@code GET /approved}   — 审批记录（当前用户 = 已审批人）</li>
 * </ul>
 * 所有接口均从 {@code FlowCreatorProvider} 获取当前用户，不暴露 userId 参数。</p>
 */
@RestController
@RequestMapping("${flow.web.api-prefix:/flow}/designer/todos")
public class FlowDesignerTodoController {

    private final FlowDesignerTodoService todoService;

    public FlowDesignerTodoController(FlowDesignerTodoService todoService) {
        this.todoService = todoService;
    }

    /**
     * 分页查询当前用户发起的流程实例。
     *
     * <p>请求示例：{@code GET /flow/designer/todos/my-applied?current=1&size=10&instanceState=0&startTime=2026-01-01 00:00:00&endTime=2026-09-17 23:59:59}</p>
     * <ul>
     *     <li>{@code current}：页码，从 1 开始，必填</li>
     *     <li>{@code size}：每页条数，必填</li>
     *     <li>{@code instanceState}：审批状态，可选；0-审批中、1-审批通过、2-审批拒绝、3-已撤回；不传查全部</li>
     *     <li>{@code startTime}：发起时间下限（含），格式 yyyy-MM-dd HH:mm:ss，可选</li>
     *     <li>{@code endTime}：发起时间上限（含），格式 yyyy-MM-dd HH:mm:ss，可选</li>
     * </ul>
     *
     * @param request 分页请求
     * @return 待办记录分页结果
     */
    @GetMapping("/my-applied")
    public Result<PageResult<FlowTodoRecordResponse>> myApplied(@Valid FlowMyAppliedPageRequest request) {
        return Result.ok(todoService.myApplied(request));
    }

    /**
     * 分页查询当前用户待审批的活动任务。
     *
     * <p>请求示例：{@code GET /flow/designer/todos/pending?current=1&size=10&startUserName=张&sourceType=business&processType=hr}</p>
     * <ul>
     *     <li>{@code current}：页码，从 1 开始，必填</li>
     *     <li>{@code size}：每页条数，必填</li>
     *     <li>{@code instanceId}：流程单号，精确匹配，可选</li>
     *     <li>{@code startUserName}：发起人名称，模糊匹配，可选</li>
     *     <li>{@code sourceType}：表单来源类型，可选；{@code form}（自定义表单）、{@code business}（业务表单）</li>
     *     <li>{@code processType}：流程分类，精确匹配，可选</li>
     *     <li>{@code startTime}：发起时间下限（含），格式 yyyy-MM-dd HH:mm:ss，可选</li>
     *     <li>{@code endTime}：发起时间上限（含），格式 yyyy-MM-dd HH:mm:ss，可选</li>
     * </ul>
     *
     * @param request 分页请求
     * @return 待办记录分页结果
     */
    @GetMapping("/pending")
    public Result<PageResult<FlowTodoRecordResponse>> pending(@Valid FlowPendingPageRequest request) {
        return Result.ok(todoService.pending(request));
    }

    /**
     * 分页查询抄送给当前用户的记录（活动抄送 + 历史抄送）。
     *
     * <p>请求示例：{@code GET /flow/designer/todos/copied?current=1&size=10&startUserName=张&sourceType=form&processType=hr&startTime=2026-01-01 00:00:00}</p>
     * <ul>
     *     <li>{@code current}：页码，从 1 开始，必填</li>
     *     <li>{@code size}：每页条数，必填</li>
     *     <li>{@code instanceId}：流程单号，精确匹配，可选</li>
     *     <li>{@code startUserName}：发起人名称，模糊匹配，可选</li>
     *     <li>{@code sourceType}：表单来源类型，可选；{@code form}（自定义表单）、{@code business}（业务表单）</li>
     *     <li>{@code processType}：流程分类，精确匹配，可选</li>
     *     <li>{@code startTime}：抄送时间下限（含，抄送任务 create_time），格式 yyyy-MM-dd HH:mm:ss，可选</li>
     *     <li>{@code endTime}：抄送时间上限（含，抄送任务 create_time），格式 yyyy-MM-dd HH:mm:ss，可选</li>
     * </ul>
     *
     * @param request 分页请求
     * @return 待办记录分页结果
     */
    @GetMapping("/copied")
    public Result<PageResult<FlowTodoRecordResponse>> copied(@Valid FlowCopiedPageRequest request) {
        return Result.ok(todoService.copied(request));
    }

    /**
     * 分页查询当前用户已审批过的历史任务。
     *
     * <p>请求示例：{@code GET /flow/designer/todos/approved?current=1&size=10&startUserName=张&sourceType=business&processType=hr&startTime=2026-01-01 00:00:00&endTime=2026-09-17 23:59:59}</p>
     * <ul>
     *     <li>{@code current}：页码，从 1 开始，必填</li>
     *     <li>{@code size}：每页条数，必填</li>
     *     <li>{@code instanceId}：流程单号，精确匹配，可选</li>
     *     <li>{@code startUserName}：发起人名称，模糊匹配，可选</li>
     *     <li>{@code sourceType}：表单来源类型，可选；{@code form}（自定义表单）、{@code business}（业务表单）</li>
     *     <li>{@code processType}：流程分类，精确匹配，可选</li>
     *     <li>{@code startTime}：审批时间下限（含，flow_his_task.finish_time），格式 yyyy-MM-dd HH:mm:ss，可选</li>
     *     <li>{@code endTime}：审批时间上限（含，flow_his_task.finish_time），格式 yyyy-MM-dd HH:mm:ss，可选</li>
     * </ul>
     *
     * <p>返回记录额外包含 {@code approveTime}（审批记录时间 = finish_time）。</p>
     *
     * @param request 分页请求
     * @return 待办记录分页结果
     */
    @GetMapping("/approved")
    public Result<PageResult<FlowTodoRecordResponse>> approved(@Valid FlowApprovedPageRequest request) {
        return Result.ok(todoService.approved(request));
    }
}
