package org.nkk.flow.enums.node;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nkk.core.enums.common.IEnum;

/**
 * 审批人设置类型。
 */
@Getter
@AllArgsConstructor
public enum FlowNodeSetTypeEnum implements IEnum<Integer> {

    SPECIFY_MEMBERS(1, "指定成员"),
    SUPERVISOR(2, "主管"),
    ROLE(3, "角色"),
    INITIATOR_SELECTED(4, "发起人自选"),
    INITIATOR_SELF(5, "发起人本人"),
    DEPARTMENT(7, "部门"),
    CANDIDATE(8, "候选人");

    private final Integer value;
    private final String label;

    @Override
    public Integer value() {
        return value;
    }

    @Override
    public String label() {
        return label;
    }
}

