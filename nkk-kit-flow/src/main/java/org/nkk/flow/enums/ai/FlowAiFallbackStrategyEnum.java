package org.nkk.flow.enums.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nkk.core.enums.common.IEnum;

/**
 * AI 处理失败后的降级策略。
 */
@Getter
@AllArgsConstructor
public enum FlowAiFallbackStrategyEnum implements IEnum<String> {

    DEFAULT_PASS("DEFAULT_PASS", "默认通过"),
    DEFAULT_REJECT("DEFAULT_REJECT", "默认拒绝"),
    MANUAL("MANUAL", "转人工处理");

    private final String value;
    private final String label;

    @Override
    public String value() {
        return value;
    }

    @Override
    public String label() {
        return label;
    }

    public static FlowAiFallbackStrategyEnum of(String value) {
        FlowAiFallbackStrategyEnum strategy = IEnum.resolveKeyOfNullable(FlowAiFallbackStrategyEnum.class, value);
        return strategy == null ? MANUAL : strategy;
    }
}
