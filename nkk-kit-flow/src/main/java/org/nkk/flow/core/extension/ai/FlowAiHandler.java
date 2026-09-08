package org.nkk.flow.core.extension.ai;

import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.core.context.FlowExecution;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.enums.ai.FlowAiFallbackStrategyEnum;
import org.nkk.flow.enums.core.FlowInstanceEnum.InstanceState;
import org.nkk.flow.enums.core.FlowTaskEnum.TaskState;
import org.nkk.flow.model.FlowAiConfig;
import org.nkk.flow.model.FlowAiResponse;
import org.nkk.flow.model.FlowNodeModel;

import java.util.List;
import java.util.Map;

/**
 * AI 节点处理器。使用方实现 execute 方法即可接入自己的 AI 服务。
 */
public interface FlowAiHandler {

    default boolean handle(FlowContext context, FlowExecution execution, FlowNodeModel nodeModel) {
        FlowAiResponse response = execute(context, execution, nodeModel);
        return processAiResponse(context, execution, nodeModel, response);
    }

    /**
     * 调用外部 AI 服务并返回结构化结果。
     */
    FlowAiResponse execute(FlowContext context, FlowExecution execution, FlowNodeModel nodeModel);

    /**
     * 处理 AI 结果。默认实现支持变量合并、通过、拒绝、异步等待和失败降级。
     */
    default boolean processAiResponse(FlowContext context, FlowExecution execution, FlowNodeModel nodeModel,
                                      FlowAiResponse response) {
        FlowAiConfig aiConfig = nodeModel.getAiConfig();
        if (response == null) {
            return fallback(context, execution, nodeModel, aiConfig);
        }
        mergeAiVariables(execution, response, aiConfig);
        if (response.isAsync()) {
            return true;
        }
        if (response.isSuccess() && response.getConfidenceOrDefault() >= aiConfig.getConfidenceThresholdOrDefault()) {
            if (response.isPass()) {
                return autoCompleteCreatedTasks(context, execution, nodeModel);
            }
            if (response.isReject()) {
                return execution.endInstance(nodeModel, InstanceState.AUTO_REJECT);
            }
            return true;
        }
        return fallback(context, execution, nodeModel, aiConfig);
    }

    /**
     * 合并 AI 提取变量到当前流程变量。
     */
    default void mergeAiVariables(FlowExecution execution, FlowAiResponse response, FlowAiConfig aiConfig) {
        Map<String, Object> aiVariables = response.getVariables();
        Map<String, Object> args = execution.getArgs();
        if (aiVariables == null || aiVariables.isEmpty() || args == null) {
            return;
        }
        Map<String, String> outputMapping = aiConfig == null ? null : aiConfig.getOutputMapping();
        for (Map.Entry<String, Object> entry : aiVariables.entrySet()) {
            String key = entry.getKey();
            if (outputMapping != null && outputMapping.containsKey(key)) {
                key = outputMapping.get(key);
            }
            args.put(key, entry.getValue());
        }
    }

    /**
     * AI 条件分支决策。返回 null 时走普通条件判断。
     */
    default String decideRoute(FlowContext context, FlowExecution execution, FlowNodeModel nodeModel,
                               Map<String, Object> args) {
        return null;
    }

    /**
     * AI 包容分支决策。返回空时走普通条件判断。
     */
    default List<String> decideInclusiveRoutes(FlowContext context, FlowExecution execution, FlowNodeModel nodeModel,
                                               Map<String, Object> args) {
        return null;
    }

    /**
     * 异步 AI 完成后的回调入口，默认只返回成功，实际恢复逻辑由使用方按业务实现。
     */
    default boolean onAsyncComplete(FlowContext context, String asyncToken, FlowAiResponse response) {
        return true;
    }

    default boolean fallback(FlowContext context, FlowExecution execution, FlowNodeModel nodeModel,
                             FlowAiConfig aiConfig) {
        FlowAiFallbackStrategyEnum strategy = FlowAiFallbackStrategyEnum.of(
                aiConfig == null ? null : aiConfig.getFallbackStrategyOrDefault());
        if (FlowAiFallbackStrategyEnum.DEFAULT_PASS == strategy) {
            return autoCompleteCreatedTasks(context, execution, nodeModel);
        }
        if (FlowAiFallbackStrategyEnum.DEFAULT_REJECT == strategy) {
            return execution.endInstance(nodeModel, InstanceState.AUTO_REJECT);
        }
        return true;
    }

    default boolean autoCompleteCreatedTasks(FlowContext context, FlowExecution execution, FlowNodeModel nodeModel) {
        List<FlowTask> tasks = execution.getFlowTasks();
        if (tasks != null) {
            for (FlowTask task : tasks) {
                if (task.getId() != null && nodeModel.getNodeKey().equals(task.getTaskKey())) {
                    context.getTaskService().executeTask(task.getId(), FlowCreator.ADMIN, execution.getArgs(),
                            TaskState.AUTO_COMPLETED);
                }
            }
        }
        return execution.executeNodeModel(nodeModel.getNodeKey());
    }
}

