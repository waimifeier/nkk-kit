package org.nkk.flow.web.controller;

import org.nkk.core.beans.common.Result;
import org.nkk.flow.web.model.FlowTodoRecordResponse;
import org.nkk.flow.web.service.FlowDesignerTodoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 流程设计器待办中心接口。
 */
@RestController
@RequestMapping("${nkk.flow.web.api-prefix:/nkk/flow}/designer/todos")
public class FlowDesignerTodoController {

    private final FlowDesignerTodoService todoService;

    public FlowDesignerTodoController(FlowDesignerTodoService todoService) {
        this.todoService = todoService;
    }

    /**
     * 查询待办中心记录。
     *
     * <p>type 示例值：1 我申请的，2 待我审批，3 我已审批，4 抄送消息。</p>
     * <p>请求示例：{@code GET /nkk/flow/designer/todos?type=2&userId=1}</p>
     *
     * @param type 查询类型；示例值：{@code 2}
     * @param userId 用户 ID；示例值：{@code 1}。为空时使用 {@code FlowCreatorProvider} 获取当前用户。
     * @return 待办中心记录列表
     */
    @GetMapping
    public Result<List<FlowTodoRecordResponse>> list(@RequestParam Integer type,
                                                     @RequestParam(required = false) String userId) {
        return Result.ok(todoService.list(type, userId));
    }
}
