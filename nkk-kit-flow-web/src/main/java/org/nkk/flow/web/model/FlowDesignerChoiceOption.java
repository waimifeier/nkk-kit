package org.nkk.flow.web.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 流程设计器节点属性单选项。
 *
 * <p>与前端 FlowFlowNodeDrawerChoiceOption 对齐，用于 setType / examineMode /
 * approveSelf / rejectStrategy / termMode 等枚举型属性的配置下发。</p>
 */
@Data
public class FlowDesignerChoiceOption implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 选项值。
     *
     * <p>当前所有节点属性枚举均为整数；允许为 {@code null}，仅发起人节点 setType
     * 的「所有人」选项使用（不限制发起人）。</p>
     */
    private Integer value;

    /**
     * 选项名称。
     */
    private String label;

    /**
     * 选项说明文案，用于设计器中的辅助提示，可为空。
     */
    private String description;

    public static FlowDesignerChoiceOption of(Integer value, String label) {
        return of(value, label, null);
    }

    public static FlowDesignerChoiceOption of(Integer value, String label, String description) {
        FlowDesignerChoiceOption option = new FlowDesignerChoiceOption();
        option.setValue(value);
        option.setLabel(label);
        option.setDescription(description);
        return option;
    }
}
