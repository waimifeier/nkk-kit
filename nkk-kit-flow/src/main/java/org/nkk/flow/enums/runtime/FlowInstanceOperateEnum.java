package org.nkk.flow.enums.runtime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nkk.core.enums.common.IEnum;

/**
 * 流程实例操作类型。
 *
 * <p>该枚举不对应数据库字段，只用于运行时权限策略区分当前正在执行的实例级操作。</p>
 */
@Getter
@AllArgsConstructor
public enum FlowInstanceOperateEnum implements IEnum<Integer> {

    REVOKE(1, "撤回流程"),
    TERMINATE(2, "终止流程"),
    SUSPEND(3, "挂起流程"),
    ACTIVE(4, "激活流程"),
    DESTROY(5, "作废流程"),
    TIMEOUT(6, "超时结束流程"),
    REJECT(7, "拒绝结束流程");

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
