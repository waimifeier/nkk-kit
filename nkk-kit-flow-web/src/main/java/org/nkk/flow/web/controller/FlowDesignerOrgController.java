package org.nkk.flow.web.controller;

import org.nkk.core.beans.common.Result;
import org.nkk.flow.web.model.FlowDesignerCategoryNode;
import org.nkk.flow.web.model.FlowDesignerOption;
import org.nkk.flow.web.model.FlowDesignerTreeNode;
import org.nkk.flow.web.service.FlowDesignerOrgService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 流程设计器组织数据接口。
 */
@RestController
@RequestMapping("${nkk.flow.web.api-prefix:/nkk/flow}/designer/org")
public class FlowDesignerOrgController {

    private final FlowDesignerOrgService orgService;

    public FlowDesignerOrgController(FlowDesignerOrgService orgService) {
        this.orgService = orgService;
    }

    /**
     * 获取部门树。
     *
     * @return 部门树，只包含部门节点
     */
    @GetMapping("/departments/tree")
    public Result<List<FlowDesignerTreeNode>> departments() {
        return Result.ok(orgService.listDepartmentTree());
    }

    /**
     * 获取员工树。
     *
     * @return 部门加员工树，部门作为分组节点，员工作为叶子节点
     */
    @GetMapping("/employees/tree")
    public Result<List<FlowDesignerTreeNode>> employees() {
        return Result.ok(orgService.listEmployeeTree());
    }

    /**
     * 获取分类树。
     *
     * <p>分类支持多级结构，用于流程分类、模板分类等自定义分组数据。</p>
     *
     * @return 分类树节点列表
     */
    @GetMapping("/categories/tree")
    public Result<List<FlowDesignerCategoryNode>> categories() {
        return Result.ok(orgService.listCategoryTree());
    }

    /**
     * 获取角色列表。
     *
     * @return 角色选项列表
     */
    @GetMapping("/roles")
    public Result<List<FlowDesignerOption>> roles() {
        return Result.ok(orgService.listRoles());
    }
}
