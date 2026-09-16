package org.nkk.flow.web.controller;

import org.nkk.core.beans.common.PageResult;
import org.nkk.core.beans.common.Result;
import org.nkk.flow.web.model.FlowTodoPageRequest;
import org.nkk.flow.web.model.FlowTodoRecordResponse;
import org.nkk.flow.web.service.FlowDesignerTodoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 流程设计器待办中心接口。
 */
@RestController
@RequestMapping("${flow.web.api-prefix:/flow}/designer/todos")
public class FlowDesignerTodoController {

    private final FlowDesignerTodoService todoService;

    public FlowDesignerTodoController(FlowDesignerTodoService todoService) {
        this.todoService = todoService;
    }

    /**
     * 分页查询待办中心记录。
     *
     * <p>查询参数示例：{@code GET /flow/designer/todos?current=1&size=10&type=2}</p>
     * <ul>
     *     <li>{@code current}：页码，从 1 开始，必填</li>
     *     <li>{@code size}：每页条数，必填</li>
     *     <li>{@code type}：查询类型，必填；取值见 {@link org.nkk.flow.enums.core.FlowTodoTypeEnum}：
     *          1-我申请的、2-待我审批、3-我已审批、4-抄送消息</li>
     *     <li>{@code userId}：用户 ID，可选；为空时使用 {@code FlowCreatorProvider} 获取当前用户</li>
     *     <li>{@code instanceState}：审批状态，仅 {@code type=1}（我申请的）时生效，可选；
     *          取值见 {@link org.nkk.flow.enums.core.FlowInstanceEnum.InstanceState}：
     *          0-审批中、1-审批通过、2-审批拒绝、3-已撤销；不传表示查询全部状态</li>
     * </ul>
     *
     * @param request 分页查询请求
     * @return 待办中心记录分页结果
     */
    @GetMapping
    public Result<PageResult<FlowTodoRecordResponse>> list(@Valid FlowTodoPageRequest request) {
        return Result.ok(todoService.page(request));
    }
}
