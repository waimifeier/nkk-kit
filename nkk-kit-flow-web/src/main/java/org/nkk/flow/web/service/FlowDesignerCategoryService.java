package org.nkk.flow.web.service;

import org.nkk.flow.web.extension.FlowDesignerCategoryProvider;
import org.nkk.flow.web.model.FlowDesignerCategoryNode;

import java.util.List;

/**
 * 流程设计器分类数据服务。
 */
public class FlowDesignerCategoryService {

    private final FlowDesignerCategoryProvider categoryProvider;

    public FlowDesignerCategoryService(FlowDesignerCategoryProvider categoryProvider) {
        this.categoryProvider = categoryProvider;
    }

    /**
     * 查询分类树。
     *
     * @return 分类树节点列表
     */
    public List<FlowDesignerCategoryNode> listCategoryTree() {
        return categoryProvider.listCategoryTree();
    }
}
