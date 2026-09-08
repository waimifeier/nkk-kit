package org.nkk.flow.web.model;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程设计器分类树节点。
 *
 * <p>用于流程分类、模板分类等多级分组数据。分类节点不承载参与人语义，只描述层级结构。</p>
 */
@Data
public class FlowDesignerCategoryNode implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 节点 ID。
     */
    private String id;

    /**
     * 父节点 ID。
     */
    private String parentId;

    /**
     * 节点名称。
     */
    private String name;

    /**
     * 前端展示文本。
     */
    private String label;

    /**
     * 是否禁用选择。
     */
    private Boolean disabled = false;

    /**
     * 是否叶子节点。
     */
    private Boolean leaf = false;

    /**
     * 子节点。
     */
    private List<FlowDesignerCategoryNode> children = new ArrayList<>();

    /**
     * 扩展字段，预留给前端设计器使用。
     */
    private Map<String, Object> extra = new LinkedHashMap<>();

    public static FlowDesignerCategoryNode of(String id, String parentId, String name, boolean leaf) {
        FlowDesignerCategoryNode node = new FlowDesignerCategoryNode();
        node.setId(id);
        node.setParentId(parentId);
        node.setName(name);
        node.setLabel(name);
        node.setLeaf(leaf);
        return node;
    }

    public static FlowDesignerCategoryNode group(String id, String parentId, String name) {
        return of(id, parentId, name, false);
    }

    public static FlowDesignerCategoryNode leaf(String id, String parentId, String name) {
        return of(id, parentId, name, true);
    }
}
