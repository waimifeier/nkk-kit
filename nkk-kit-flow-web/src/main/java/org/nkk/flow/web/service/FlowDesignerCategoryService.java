package org.nkk.flow.web.service;

import cn.hutool.core.util.StrUtil;
import org.nkk.flow.web.extension.FlowDesignerCategoryProvider;
import org.nkk.flow.web.model.FlowDesignerCategoryNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 构建「流程分类 id → 中文名称」映射，来源为分类树（支持多级）。
     *
     * @return 分类 id 与中文名称的映射；分类树为空时返回空 Map
     */
    public Map<String, String> categoryNameMap() {
        Map<String, String> names = new LinkedHashMap<>();
        collectCategoryNames(categoryProvider.listCategoryTree(), names);
        return names;
    }

    private void collectCategoryNames(List<FlowDesignerCategoryNode> nodes, Map<String, String> names) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        for (FlowDesignerCategoryNode node : nodes) {
            if (StrUtil.isNotBlank(node.getId())) {
                names.put(node.getId(), StrUtil.blankToDefault(node.getName(), node.getLabel()));
            }
            collectCategoryNames(node.getChildren(), names);
        }
    }
}
