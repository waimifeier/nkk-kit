package org.nkk.flow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nkk.flow.enums.ai.FlowAiStatusEnum;

import java.io.Serializable;
import java.util.Map;

/**
 * AI 节点处理结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowAiResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * AI 执行状态，取值见 {@link FlowAiStatusEnum}。
     */
    private FlowAiStatusEnum status;

    /**
     * 决策结果。审批节点建议使用 PASS 或 REJECT；路由节点可返回目标分支 nodeKey。
     */
    private String decision;

    /**
     * AI 审批意见或处理说明。
     */
    private String advice;

    /**
     * AI 提取出的流程变量。
     */
    private Map<String, Object> variables;

    /**
     * 置信度评分，范围由使用方实现自行约定，默认建议 0.0 - 1.0。
     */
    private Double confidence;

    /**
     * 调用指标。
     */
    private FlowAiMetrics metrics;

    /**
     * 原始响应，便于排查。
     */
    private String rawContent;

    /**
     * 错误信息。
     */
    private String errorMessage;

    /**
     * 异步处理凭证。
     */
    private String asyncToken;

    /**
     * 创建 AI 处理成功响应。
     *
     * @param decision 决策结果
     * @param advice 处理意见
     * @param confidence 置信度
     * @return AI 节点处理结果
     */
    public static FlowAiResponse success(String decision, String advice, Double confidence) {
        return FlowAiResponse.builder()
                .status(FlowAiStatusEnum.SUCCESS)
                .decision(decision)
                .advice(advice)
                .confidence(confidence)
                .build();
    }

    /**
     * 创建携带流程变量的 AI 处理成功响应。
     *
     * @param decision 决策结果
     * @param advice 处理意见
     * @param confidence 置信度
     * @param variables AI 提取出的流程变量
     * @return AI 节点处理结果
     */
    public static FlowAiResponse success(String decision, String advice, Double confidence, Map<String, Object> variables) {
        return FlowAiResponse.builder()
                .status(FlowAiStatusEnum.SUCCESS)
                .decision(decision)
                .advice(advice)
                .confidence(confidence)
                .variables(variables)
                .build();
    }

    /**
     * 创建 AI 处理失败响应。
     *
     * @param errorMessage 错误信息
     * @return AI 节点处理结果
     */
    public static FlowAiResponse failure(String errorMessage) {
        return FlowAiResponse.builder()
                .status(FlowAiStatusEnum.FAILURE)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 创建低置信度响应。
     *
     * @param decision 决策结果
     * @param advice 处理意见
     * @param confidence 置信度
     * @return AI 节点处理结果
     */
    public static FlowAiResponse lowConfidence(String decision, String advice, Double confidence) {
        return FlowAiResponse.builder()
                .status(FlowAiStatusEnum.LOW_CONFIDENCE)
                .decision(decision)
                .advice(advice)
                .confidence(confidence)
                .build();
    }

    /**
     * 创建异步处理中响应。
     *
     * @param asyncToken 异步处理凭证
     * @return AI 节点处理结果
     */
    public static FlowAiResponse async(String asyncToken) {
        return FlowAiResponse.builder()
                .status(FlowAiStatusEnum.ASYNC)
                .asyncToken(asyncToken)
                .build();
    }

    /**
     * 创建降级处理响应。
     *
     * @param reason 降级原因
     * @return AI 节点处理结果
     */
    public static FlowAiResponse fallback(String reason) {
        return FlowAiResponse.builder()
                .status(FlowAiStatusEnum.FALLBACK)
                .errorMessage(reason)
                .build();
    }

    /**
     * 判断当前响应是否需要转人工复核。
     *
     * @return true 表示需要人工复核
     */
    public boolean needManualReview() {
        return status != null && status.needManualReview();
    }

    /**
     * 判断 AI 是否处理成功。
     *
     * @return true 表示处理成功
     */
    public boolean isSuccess() {
        return status != null && status.isSuccess();
    }

    /**
     * 判断 AI 是否仍在异步处理中。
     *
     * @return true 表示异步处理中
     */
    public boolean isAsync() {
        return status != null && status.isAsync();
    }

    /**
     * 获取置信度，未返回时默认 0。
     *
     * @return 置信度
     */
    public double getConfidenceOrDefault() {
        return confidence == null ? 0.0D : confidence;
    }

    /**
     * 判断决策结果是否为通过。
     *
     * @return true 表示通过
     */
    public boolean isPass() {
        return "PASS".equalsIgnoreCase(decision);
    }

    /**
     * 判断决策结果是否为拒绝。
     *
     * @return true 表示拒绝
     */
    public boolean isReject() {
        return "REJECT".equalsIgnoreCase(decision);
    }

    /**
     * AI 调用指标。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlowAiMetrics implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * AI 模型名称。
         */
        private String modelName;

        /**
         * 提示词 token 数。
         */
        private Long promptTokens;

        /**
         * 补全文本 token 数。
         */
        private Long completionTokens;

        /**
         * AI 调用总耗时，单位毫秒。
         */
        private Long totalTimeMs;

        /**
         * 外部 AI 请求 ID。
         */
        private String requestId;

        /**
         * 获取总 token 数。
         *
         * @return promptTokens 与 completionTokens 之和
         */
        public long getTotalTokens() {
            long prompt = promptTokens == null ? 0L : promptTokens;
            long completion = completionTokens == null ? 0L : completionTokens;
            return prompt + completion;
        }
    }
}

