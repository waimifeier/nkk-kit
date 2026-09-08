package org.nkk.flow.model;

import lombok.Data;
import org.nkk.flow.enums.ai.FlowAiFallbackStrategyEnum;

import java.io.Serializable;
import java.util.Map;

/**
 * AI 节点配置。
 */
@Data
public class FlowAiConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 外部 AI 智能体标识。
     */
    private String agentId;

    /**
     * 提示词模板。具体模板解析由使用方的 FlowAiHandler 决定。
     */
    private String promptTemplate;

    /**
     * 置信度阈值，低于阈值时转人工或执行降级策略。
     */
    private Double confidenceThreshold;

    /**
     * 超时时间，单位秒。
     */
    private Integer timeoutSeconds;

    /**
     * 降级策略，取值见 {@link FlowAiFallbackStrategyEnum}。
     */
    private String fallbackStrategy;

    /**
     * 是否异步处理。
     */
    private Boolean asyncMode;

    /**
     * 最大重试次数。
     */
    private Integer maxRetries;

    /**
     * AI 输出变量映射：AI 字段名 -> 流程变量名。
     */
    private Map<String, String> outputMapping;

    /**
     * 外部模型参数，例如 temperature、maxTokens 等。
     */
    private Map<String, Object> modelParams;

    /**
     * 获取置信度阈值，未配置时默认 0.8。
     *
     * @return 置信度阈值
     */
    public double getConfidenceThresholdOrDefault() {
        return confidenceThreshold == null ? 0.8D : confidenceThreshold;
    }

    /**
     * 获取超时时间，未配置时默认 30 秒。
     *
     * @return 超时时间，单位秒
     */
    public int getTimeoutSecondsOrDefault() {
        return timeoutSeconds == null ? 30 : timeoutSeconds;
    }

    /**
     * 判断是否启用异步 AI 处理。
     *
     * @return true 表示异步处理
     */
    public boolean isAsyncModeEnabled() {
        return Boolean.TRUE.equals(asyncMode);
    }

    /**
     * 获取最大重试次数，未配置时默认 3 次。
     *
     * @return 最大重试次数
     */
    public int getMaxRetriesOrDefault() {
        return maxRetries == null ? 3 : maxRetries;
    }

    /**
     * 获取降级策略，未配置时默认转人工处理。
     *
     * @return 降级策略，取值见 {@link FlowAiFallbackStrategyEnum}
     */
    public String getFallbackStrategyOrDefault() {
        return fallbackStrategy == null ? "MANUAL" : fallbackStrategy;
    }
}

