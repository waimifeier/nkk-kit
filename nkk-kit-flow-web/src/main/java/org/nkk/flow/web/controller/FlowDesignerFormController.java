package org.nkk.flow.web.controller;

import org.nkk.core.beans.common.Result;
import org.nkk.flow.web.model.FlowDesignerFormOption;
import org.nkk.flow.model.FlowFieldMeta;
import org.nkk.flow.web.service.FlowDesignerFormService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 流程设计器表单字段接口。
 *
 * <p>字段元数据不再落独立表，统一走 {@code FlowDesignerFormProvider} SPI 获取。</p>
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
     * <p>设计器条件分支节点需要可选字段时调用。优先走 {@code FlowDesignerFormProvider}，
     * Provider 未实现时返回空列表。</p>
     *
     * @param formKey 表单编码；示例值：{@code leave-form}
     * @param sourceType 表单来源类型；示例值：{@code form}、{@code business}
     * @param formVersion 表单版本；示例值：{@code 1}
     * @return 字段元数据列表
     */
    @GetMapping("/{formKey}/fields")
    public Result<List<FlowFieldMeta>> fields(@PathVariable String formKey,
                                               @RequestParam(required = false) String sourceType,
                                               @RequestParam(required = false) Integer formVersion) {
        return Result.ok(formService.listFormFields(formKey, formVersion, sourceType));
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
