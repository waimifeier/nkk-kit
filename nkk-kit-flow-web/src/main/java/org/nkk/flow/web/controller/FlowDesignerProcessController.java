package org.nkk.flow.web.controller;

import org.nkk.core.beans.common.Result;
import org.nkk.flow.entity.FlowProcess;
import org.nkk.flow.web.model.FlowProcessCategoryResponse;
import org.nkk.flow.web.model.FlowProcessInfoUpdateRequest;
import org.nkk.flow.web.model.FlowProcessPublishRequest;
import org.nkk.flow.web.model.FlowProcessVO;
import org.nkk.flow.web.service.FlowDesignerProcessService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 流程设计器流程定义接口。
 */
@RestController
@RequestMapping("${flow.web.api-prefix:/flow}/designer/processes")
public class FlowDesignerProcessController {

    private final FlowDesignerProcessService processService;

    public FlowDesignerProcessController(FlowDesignerProcessService processService) {
        this.processService = processService;
    }

    /**
     * 发布流程定义。
     *
     * <p>请求体示例：{@code {"processName":"请假审批","processKey":"leave","processIcon":"icon-leave","processType":"hr","instanceUrl":"/flow/instances/{instanceId}","remark":"请假流程","formSourceType":"FORM","formId":1001,"formKey":"leave-form","formVersion":1,"formName":"请假表单","saveAsDraft":false,"modelContent":{"nodeKey":"start","nodeName":"发起人","type":0,"childNode":{...}}}}</p>
     * <p>{@code formSourceType/formId/formKey/formVersion/formName} 为表单元数据，发布时会写入 flow_process 表的独立列；表单字段定义放在 {@code modelContent.metaFields} 中。</p>
     * <p>{@code saveAsDraft=true} 时只保存草稿，不强校验流程模型，也不影响当前启用版本。</p>
     *
     * @param request 流程发布请求
     * @return 发布成功后的流程定义
     */
    @PostMapping("/publish")
    public Result<FlowProcess> publish(@RequestBody FlowProcessPublishRequest request) {
        return Result.ok(processService.publish(request));
    }

    /**
     * 按流程分类查询当前流程定义列表。
     *
     * <p>该接口不分页，返回每个 {@code processType} 下的流程定义。</p>
     * <p>{@code mode=latest} 表示按流程 key 返回最新编辑版本；{@code mode=published} 表示只返回已发布版本。</p>
     *
     * @param tenantId 租户 ID；示例值：{@code tenant-001}
     * @param mode 查询模式；示例值：{@code latest}、{@code published}
     * @return 分类分组流程列表
     */
    @GetMapping("/categories")
    public Result<List<FlowProcessCategoryResponse>> categories(@RequestParam(required = false) String tenantId,
                                                                @RequestParam(defaultValue = "latest") String mode) {
        return Result.ok(processService.listByCategory(tenantId, mode));
    }

    /**
     * 查询流程定义详情。
     *
     * @param processId 流程定义 ID；示例值：{@code 1785467714628}
     * @return 流程定义详情
     */
    @GetMapping("/{processId}")
    public Result<FlowProcess> get(@PathVariable Long processId) {
        return Result.ok(processService.getProcess(processId));
    }

    /**
     * 启用流程定义。
     *
     * @param processId 流程定义 ID；示例值：{@code 1785467714628}
     * @return 更新后的流程定义
     */
    @PostMapping("/{processId}/enable")
    public Result<Void> enable(@PathVariable Long processId) {
        processService.enable(processId);
        return Result.ok("成功");
    }

    /**
     * 禁用流程定义。
     *
     * @param processId 流程定义 ID；示例值：{@code 1785467714628}
     * @return 更新后的流程定义
     */
    @PostMapping("/{processId}/disable")
    public Result<Void> disable(@PathVariable Long processId) {
        processService.disable(processId);
        return Result.ok("成功");
    }

    /**
     * 修改流程基础信息。
     *
     * <p>基础信息只允许修改流程名称、图标、类型和描述，不修改流程 key、版本和模型 JSON。</p>
     *
     * @param processId 流程定义 ID；示例值：{@code 1785467714628}
     * @param request 修改请求；示例值：{@code {"processName":"请假审批","processIcon":"icon-leave","processType":"hr","remark":"请假流程"}}
     * @return 更新后的流程定义
     */
    @PutMapping("/{processId}/info")
    public Result<FlowProcess> updateInfo(@PathVariable Long processId,
                                          @RequestBody FlowProcessInfoUpdateRequest request) {
        return Result.ok(processService.updateInfo(processId, request));
    }

    /**
     * 查询流程定义版本记录。
     *
     * @param processKey 流程 key；示例值：{@code leave}
     * @param tenantId 租户 ID；示例值：{@code tenant-001}
     * @return 版本记录，按版本倒序返回
     */
    @GetMapping("/key/{processKey}/versions")
    public Result<List<FlowProcessVO>> versions(@PathVariable String processKey,
                                                @RequestParam(required = false) String tenantId) {
        return Result.ok(processService.listVersions(tenantId, processKey));
    }
}
