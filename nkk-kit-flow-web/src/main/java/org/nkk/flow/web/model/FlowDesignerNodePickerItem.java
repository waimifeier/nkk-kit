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

    public static FlowDesignerNodePickerItem of(Integer type, String label, String description) {
        FlowDesignerNodePickerItem item = new FlowDesignerNodePickerItem();
        item.setType(type);
        item.setLabel(label);
        item.setDescription(description);
        return item;
    }
}
