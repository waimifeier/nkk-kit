package org.nkk.flow.web.extension;

import org.nkk.flow.web.model.FlowDesignerOption;
import org.nkk.flow.web.model.FlowDesignerCategoryNode;
import org.nkk.flow.web.model.FlowDesignerTreeNode;

import java.util.List;

/**
 * 流程设计器组织数据提供者。
 *
 * <p>该扩展点只服务于设计器选择数据，不负责审批权限判断，也不参与任务办理人解析。
 * 使用方应从自己的组织系统中查询部门、员工、角色数据并转换为设计器模型。</p>
 */
public interface FlowDesignerOrgProvider {

    /**
     * 查询部门树。
     *
     * @return 部门树节点列表，只包含部门节点
     */
    List<FlowDesignerTreeNode> listDepartmentTree();

    /**
     * 查询员工树。
     *
     * <p>返回结构通常是“部门节点 + 员工叶子节点”。部门节点用于分组展示，员工节点用于选择具体办理人。</p>
     *
     * @return 员工树节点列表
     */
    List<FlowDesignerTreeNode> listEmployeeTree();

    /**
     * 查询分类树。
     *
     * <p>分类支持多级结构，通常用于流程分类、模板分类、业务分组等场景。</p>
     *
     * @return 分类树节点列表
     */
    List<FlowDesignerCategoryNode> listCategoryTree();

    /**
     * 查询角色列表。
     *
     * @return 角色选项列表
     */
    List<FlowDesignerOption> listRoles();
}
