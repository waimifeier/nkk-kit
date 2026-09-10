package org.nkk.flow.web.extension;

import org.nkk.flow.web.model.FlowDesignerCategoryNode;

import java.util.List;

/**
 * 流程设计器分类数据提供者。
 *
 * <p>分类数据独立于组织架构，用于流程分类、模板分类、业务分组等场景。
 * 使用方应从自己的分类系统中查询数据并转换为设计器模型。</p>
 *
 * <p>与 {@link FlowDesignerOrgProvider} 的区别：</p>
 * <ul>
 *   <li>{@code FlowDesignerOrgProvider} 提供部门、员工、角色等组织架构数据</li>
 *   <li>{@code FlowDesignerCategoryProvider} 提供流程分类、业务分组等自定义分类数据</li>
 * </ul>
 */
public interface FlowDesignerCategoryProvider {

    /**
     * 查询分类树。
     *
     * <p>分类支持多级结构，通常用于流程分类、模板分类、业务分组等场景。</p>
     *
     * @return 分类树节点列表
     */
    List<FlowDesignerCategoryNode> listCategoryTree();
}
