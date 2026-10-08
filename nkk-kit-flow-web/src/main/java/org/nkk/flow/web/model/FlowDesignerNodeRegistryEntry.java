package org.nkk.flow.web.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 流程设计器节点注册表条目。
 *
 * <p>与前端 FlowNodeRegistryEntry 对齐：一条记录同时描述节点在「添加节点弹窗」
 * （picker）中的展示信息和「节点配置抽屉」（properties）中的可配置项。</p>
 */
@Data
public class FlowDesignerNodeRegistryEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 节点类型编码，与 FlowNodeTypeEnum 枚举值一致（-1 结束、0 发起人、1 审批、2 抄送等）。
     */
    private Integer type;

    /**
     * 节点展示名称。未配置时前端回退内置名称。
     */
    private String label;

    /**
     * 节点描述文案。未配置时前端回退内置描述。
     */
    private String description;

    /**
     * 节点图标地址，须为可在线访问的 http/https 图片地址（如 SVG/PNG）。
     * 为空时前端不展示图标（显式生效，不回退内置图标），可用于表达「不要图标」。
     */
    private String icon;

    /**
     * 节点主题色，十六进制色值（如 #f59e0b），用于图标、标题 hover 等视觉元素。
     * 为空时前端按中性灰色渲染。
     */
    private String color;

    /**
     * 节点配置抽屉的属性配置（选项子集、区块显隐、权限白名单），按字段级缺省回退。
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private FlowDesignerNodeProperties properties;

    public static FlowDesignerNodeRegistryEntry of(Integer type, String label, String description) {
        return of(type, label, description, null, null);
    }

    public static FlowDesignerNodeRegistryEntry of(Integer type, String label, String description,
                                                    String icon, String color) {
        FlowDesignerNodeRegistryEntry entry = new FlowDesignerNodeRegistryEntry();
        entry.setType(type);
        entry.setLabel(label);
        entry.setDescription(description);
        entry.setIcon(icon);
        entry.setColor(color);
        return entry;
    }
}
