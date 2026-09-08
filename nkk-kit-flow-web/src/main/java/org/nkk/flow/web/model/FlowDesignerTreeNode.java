package org.nkk.flow.web.model;

import lombok.Data;
import org.nkk.flow.enums.core.FlowTaskActorEnum.ActorType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程设计器树节点。
 *
 * <p>用于部门树、部门加员工树等选择器数据。核心流程只关心最终选中的 id、name、actorType，
 * 真实组织结构由使用方系统提供。</p>
 */
@Data
public class FlowDesignerTreeNode implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 节点 ID，通常是部门 ID 或用户 ID。
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
     * 参与者类型，取值见 {@link ActorType}。
     */
    private Integer actorType;

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
    private List<FlowDesignerTreeNode> children = new ArrayList<>();

    /**
     * 扩展字段，预留给前端设计器使用。
     */
    private Map<String, Object> extra = new LinkedHashMap<>();

    public static FlowDesignerTreeNode department(String id, String parentId, String name) {
        return of(id, parentId, name, ActorType.DEPARTMENT.value(), false);
    }

    public static FlowDesignerTreeNode employee(String id, String parentId, String name) {
        return of(id, parentId, name, ActorType.USER.value(), true);
    }

    public static FlowDesignerTreeNode of(String id, String parentId, String name, Integer actorType, boolean leaf) {
        FlowDesignerTreeNode node = new FlowDesignerTreeNode();
        node.setId(id);
        node.setParentId(parentId);
        node.setName(name);
        node.setLabel(name);
        node.setActorType(actorType);
        node.setLeaf(leaf);
        return node;
    }
}
