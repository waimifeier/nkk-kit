package org.nkk.flow.web.controller;

import org.nkk.core.beans.common.Result;
import org.nkk.flow.web.model.FlowDesignerFormOption;
import org.nkk.flow.web.model.FlowFormFieldVO;
import org.nkk.flow.web.model.FlowFormFieldSaveRequest;
import org.nkk.flow.web.service.FlowDesignerFormService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 流程设计器表单字段接口。
 */
@RestController
@RequestMapping("${nkk.flow.web.api-prefix:/nkk/flow}/designer/forms")
public class FlowDesignerFormController {

    private final FlowDesignerFormService formService;

    public FlowDesignerFormController(FlowDesignerFormService formService) {
        this.formService = formService;
    }

    /**
     * 查询表单字段元数据。
     *
     * <p>version 为空时返回最新版本的字段列表。</p>
     *
     * @param formKey 表单编码；示例值：{@code leave-form}
     * @param tenantId 租户 ID；示例值：{@code tenant-001}
     * @param formVersion 表单版本；示例值：{@code 1}
     * @return 字段元数据列表
     */
    @GetMapping("/{formKey}/fields")
    public Result<List<FlowFormFieldVO>> fields(@PathVariable String formKey,
                                                @RequestParam(required = false) String tenantId,
                                                @RequestParam(required = false) Integer formVersion,
                                                @RequestParam(required = false) String sourceType) {
        return Result.ok(formService.list(tenantId, formKey, formVersion, sourceType));
    }

    /**
     * 保存表单字段元数据。
     *
     * <p>同一个 formKey + formVersion 会覆盖旧字段配置。</p>
     *
     * @param formKey 表单编码；示例值：{@code leave-form}
     * @param request 保存请求
     * @return 保存后的字段元数据列表
     */
    @PostMapping("/{formKey}/fields")
    public Result<List<FlowFormFieldVO>> save(@PathVariable String formKey,
                                              @RequestBody FlowFormFieldSaveRequest request) {
        return Result.ok(formService.save(formKey, request));
    }

    /**
     * 查询设计器可选表单下拉数据。
     *
     * @param sourceType 表单来源类型；示例值：{@code form}、{@code business}
     * @return 表单下拉选项列表
     */
    @GetMapping("/options")
    public Result<List<FlowDesignerFormOption>> options(@RequestParam(required = false) String sourceType) {
        return Result.ok(formService.listForms(sourceType));
    }
}
