package org.nkk.flow.web.controller;

import org.nkk.core.beans.common.Result;
import org.nkk.flow.web.model.FlowDesignerCategoryNode;
import org.nkk.flow.web.service.FlowDesignerCategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 流程设计器分类数据接口。
 */
@RestController
@RequestMapping("${nkk.flow.web.api-prefix:/nkk/flow}/designer/categories")
public class FlowDesignerCategoryController {

    private final FlowDesignerCategoryService categoryService;

    public FlowDesignerCategoryController(FlowDesignerCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 获取分类树。
     *
     * <p>分类支持多级结构，用于流程分类、模板分类等自定义分组数据。</p>
     *
     * @return 分类树节点列表
     */
    @GetMapping("/tree")
    public Result<List<FlowDesignerCategoryNode>> categories() {
        return Result.ok(categoryService.listCategoryTree());
    }
}
