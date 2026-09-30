package org.nkk.flow.web.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 流程设计器节点选择器选项。
 *
 * <p>用于设计器添加节点弹窗的节点类型列表，与前端 FlowFlowNodePickerOption 对齐。</p>
 */
@Data
public class FlowDesignerNodePickerItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 节点类型编码，与 FlowNodeTypeEnum 枚举值一致。
     */
    private Integer type;

    /**
     * 展示名称。
     */
    private String label;

    /**
     * 描述文案。
     */
    private String description;

    /**
     * 节点图标地址，须为可在线访问的 http/https 图片地址（如 SVG/PNG）。
     * 为空时前端回退到内置图标。
     */
    private String icon;

    /**
     * 节点主题色，十六进制色值（如 #f59e0b），用于图标、标题 hover 等视觉元素。
     * 为空时前端回退到内置主题色。
     */
    private String color;

    public static FlowDesignerNodePickerItem of(Integer type, String label, String description) {
        return of(type, label, description, null, null);
    }

    public static FlowDesignerNodePickerItem of(Integer type, String label, String description,
                                                String icon, String color) {
        FlowDesignerNodePickerItem item = new FlowDesignerNodePickerItem();
        item.setType(type);
        item.setLabel(label);
        item.setDescription(description);
        item.setIcon(icon);
        item.setColor(color);
        return item;
    }
}
