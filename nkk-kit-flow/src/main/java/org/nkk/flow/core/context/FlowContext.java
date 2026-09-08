package org.nkk.flow.core.context;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;
import org.nkk.flow.core.extension.ai.FlowAiHandler;
import org.nkk.flow.core.extension.condition.DefaultFlowConditionHandler;
import org.nkk.flow.core.extension.condition.FlowConditionHandler;
import org.nkk.flow.core.extension.condition.FlowExpression;
import org.nkk.flow.core.extension.condition.SimpleFlowExpression;
import org.nkk.flow.core.extension.cache.FlowModelCache;
import org.nkk.flow.core.extension.cache.SimpleFlowModelCache;
import org.nkk.flow.core.extension.id.DefaultFlowIdGenerator;
import org.nkk.flow.core.extension.id.FlowIdGenerator;
import org.nkk.flow.core.extension.identity.DefaultFlowActorAccessStrategy;
import org.nkk.flow.core.extension.identity.DefaultFlowTaskAccessStrategy;
import org.nkk.flow.core.extension.identity.DefaultFlowTaskActorProvider;
import org.nkk.flow.core.extension.identity.DefaultFlowInstanceAccessStrategy;
import org.nkk.flow.core.extension.identity.DefaultFlowStartAccessStrategy;
import org.nkk.flow.core.extension.identity.FlowActorAccessStrategy;
import org.nkk.flow.core.extension.identity.FlowCreatorProvider;
import org.nkk.flow.core.extension.identity.FlowInstanceAccessStrategy;
import org.nkk.flow.core.extension.identity.FlowStartAccessStrategy;
import org.nkk.flow.core.extension.identity.FlowTaskAccessStrategy;
import org.nkk.flow.core.extension.identity.FlowTaskActorProvider;
import org.nkk.flow.core.extension.json.FlowJsonHandler;
import org.nkk.flow.core.extension.json.JacksonFlowJsonHandler;
import org.nkk.flow.core.extension.listener.FlowInstanceListener;
import org.nkk.flow.core.extension.listener.FlowNodeEvent;
import org.nkk.flow.core.extension.listener.FlowNodeListener;
import org.nkk.flow.core.extension.listener.FlowTaskListener;
import org.nkk.flow.core.extension.schedule.FlowTaskReminder;
import org.nkk.flow.core.extension.schedule.FlowTaskTrigger;
import org.nkk.flow.core.extension.subprocess.DefaultFlowSubProcessHandler;
import org.nkk.flow.core.extension.subprocess.FlowSubProcessHandler;
import org.nkk.flow.core.extension.task.DefaultFlowCreateTaskHandler;
import org.nkk.flow.core.extension.task.FlowCreateTaskHandler;
import org.nkk.flow.core.extension.task.FlowTaskCreateInterceptor;
import org.nkk.flow.core.extension.time.DefaultFlowCreateTimeHandler;
import org.nkk.flow.core.extension.time.FlowCreateTimeHandler;
import org.nkk.flow.model.FlowNodeModel;
import org.nkk.flow.service.FlowProcessService;
import org.nkk.flow.service.FlowQueryService;
import org.nkk.flow.service.FlowRuntimeService;
import org.nkk.flow.service.FlowTaskService;

import java.util.List;

/**
 * 流程引擎上下文。
 */
@Getter
@Setter
public class FlowContext {

    private static FlowJsonHandler jsonHandler = new JacksonFlowJsonHandler();

    private FlowProcessService processService;

    private FlowRuntimeService runtimeService;

    private FlowTaskService taskService;

    private FlowQueryService queryService;

    private FlowIdGenerator idGenerator = new DefaultFlowIdGenerator();

    private FlowExpression expression = new SimpleFlowExpression();

    private FlowConditionHandler conditionHandler = new DefaultFlowConditionHandler();

    private FlowTaskActorProvider taskActorProvider = new DefaultFlowTaskActorProvider();

    private FlowActorAccessStrategy actorAccessStrategy = new DefaultFlowActorAccessStrategy();

    private FlowTaskAccessStrategy taskAccessStrategy = new DefaultFlowTaskAccessStrategy(actorAccessStrategy);

    private FlowStartAccessStrategy startAccessStrategy = new DefaultFlowStartAccessStrategy(actorAccessStrategy);

    private FlowInstanceAccessStrategy instanceAccessStrategy = new DefaultFlowInstanceAccessStrategy();

    private FlowCreatorProvider creatorProvider;

    private FlowNodeListener nodeListener;

    private FlowTaskListener taskListener;

    private FlowInstanceListener instanceListener;

    private FlowTaskReminder taskReminder;

    private FlowTaskTrigger taskTrigger;

    private FlowSubProcessHandler subProcessHandler = new DefaultFlowSubProcessHandler();

    private FlowCreateTimeHandler createTimeHandler = new DefaultFlowCreateTimeHandler();

    private FlowTaskCreateInterceptor taskCreateInterceptor;

    private FlowCreateTaskHandler createTaskHandler = new DefaultFlowCreateTaskHandler();

    private FlowModelCache modelCache = new SimpleFlowModelCache();

    private FlowAiHandler flowAiHandler;

    public static void setJsonHandler(FlowJsonHandler handler) {
        jsonHandler = handler == null ? new JacksonFlowJsonHandler() : handler;
    }

    public static <T> T fromJson(String json, Class<T> type) {
        return jsonHandler.fromJson(json, type);
    }

    public static String toJson(Object value) {
        return jsonHandler.toJson(value);
    }

    public boolean createTask(FlowExecution execution, FlowNodeModel nodeModel) {
        if (nodeListener != null) {
            nodeListener.beforeCreateTask(FlowNodeEvent.of(this, execution, nodeModel));
        }
        if (taskCreateInterceptor != null) {
            taskCreateInterceptor.before(this, execution);
        }
        List<org.nkk.flow.entity.FlowTask> tasks = taskService.createTask(nodeModel, execution);
        execution.addTasks(tasks);
        if (taskCreateInterceptor != null) {
            taskCreateInterceptor.after(this, execution);
        }
        if (nodeListener != null) {
            nodeListener.afterCreateTask(FlowNodeEvent.of(this, execution, nodeModel, tasks));
        }
        if (flowAiHandler != null && StrUtil.isNotBlank(nodeModel.getCallAi())) {
            return flowAiHandler.handle(this, execution, nodeModel);
        }
        return tasks != null;
    }

    public FlowCreator resolveCreator(FlowCreator creator) {
        if (creator != null) {
            return creator;
        }
        FlowCreator current = creatorProvider == null ? null : creatorProvider.getCurrentCreator();
        if (current == null || StrUtil.isBlank(current.getCreateId())) {
            throw new IllegalStateException("未传入流程操作人，也未配置 FlowCreatorProvider 获取当前操作人");
        }
        return current;
    }

    public void cacheProcessModel(String key, Object model) {
        if (modelCache != null && key != null && model != null) {
            modelCache.put(key, model);
        }
    }

    public <T> T getCachedProcessModel(String key) {
        return modelCache == null || key == null ? null : modelCache.get(key);
    }

    public void invalidateProcessModel(String key) {
        if (modelCache != null && key != null) {
            modelCache.remove(key);
        }
    }
}

