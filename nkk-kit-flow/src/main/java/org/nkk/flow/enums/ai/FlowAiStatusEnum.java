package org.nkk.flow.enums.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nkk.core.enums.common.IEnum;

/**
 * AI 节点处理状态。
 */
@Getter
@AllArgsConstructor
public enum FlowAiStatusEnum implements IEnum<Integer> {

    SUCCESS(0, "处理成功"),
    FAILURE(1, "处理失败"),
    LOW_CONFIDENCE(2, "低置信度"),
    TIMEOUT(3, "处理超时"),
    ASYNC(4, "异步处理中"),
    FALLBACK(5, "降级处理");

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

    public boolean isSuccess() {
        return this == SUCCESS;
    }

    public boolean needManualReview() {
        return this == LOW_CONFIDENCE || this == FALLBACK || this == FAILURE || this == TIMEOUT;
    }

    public boolean isAsync() {
        return this == ASYNC;
    }
}
