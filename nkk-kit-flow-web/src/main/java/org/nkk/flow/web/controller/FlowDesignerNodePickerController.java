package org.nkk.flow.web.controller;

import org.nkk.core.beans.common.Result;
import org.nkk.flow.web.model.FlowDesignerNodePickerItem;
import org.nkk.flow.web.service.FlowDesignerNodePickerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 流程设计器节点选择器配置接口。
 */
@RestController
@RequestMapping("${flow.web.api-prefix:/flow}/designer/node-picker")
public class FlowDesignerNodePickerController {

    private final FlowDesignerNodePickerService pickerService;

    public FlowDesignerNodePickerController(FlowDesignerNodePickerService pickerService) {
        this.pickerService = pickerService;
    }

    /**
     * 获取节点选择器选项列表。
     *
     * <p>返回设计器添加节点弹窗中可用的节点类型（type/label/description）。
     * 使用方通过注册 {@link org.nkk.flow.web.extension.FlowDesignerNodePickerProvider} Bean
     * 自定义返回内容。</p>
     *
     * @return 节点选择器选项列表
     */
    @GetMapping
    public Result<List<FlowDesignerNodePickerItem>> items() {
        return Result.ok(pickerService.listNodePickerItems());
    }
}
