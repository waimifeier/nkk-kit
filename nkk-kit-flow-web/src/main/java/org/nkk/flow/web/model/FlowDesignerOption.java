package org.nkk.flow.web.model;

import lombok.Data;
import org.nkk.flow.enums.core.FlowTaskActorEnum.ActorType;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 流程设计器普通选项。
 *
 * <p>当前主要用于角色列表，也可用于后续岗位、用户组等平铺数据。</p>
 */
@Data
public class FlowDesignerOption implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 选项值，通常是角色 ID。
     */
    private String id;

    /**
     * 选项名称。
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
     * 扩展字段，预留给前端设计器使用。
     */
    private Map<String, Object> extra = new LinkedHashMap<>();

    public static FlowDesignerOption role(String id, String name) {
        return of(id, name, ActorType.ROLE.value());
    }

    public static FlowDesignerOption of(String id, String name, Integer actorType) {
        FlowDesignerOption option = new FlowDesignerOption();
        option.setId(id);
        option.setName(name);
        option.setLabel(name);
        option.setActorType(actorType);
        return option;
    }
}
