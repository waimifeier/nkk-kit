package org.nkk.flow.core.engine;

import org.nkk.flow.dao.FlowExtInstanceDao;
import org.nkk.flow.dao.FlowHisInstanceDao;
import org.nkk.flow.dao.FlowHisTaskActorDao;
import org.nkk.flow.dao.FlowHisTaskDao;
import org.nkk.flow.dao.FlowInstanceDao;
import org.nkk.flow.dao.FlowProcessDao;
import org.nkk.flow.dao.FlowTaskActorDao;
import org.nkk.flow.dao.FlowTaskDao;
import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.core.extension.ai.FlowAiHandler;
import org.nkk.flow.core.extension.cache.FlowModelCache;
import org.nkk.flow.core.extension.cache.SimpleFlowModelCache;
import org.nkk.flow.core.extension.condition.DefaultFlowConditionHandler;
import org.nkk.flow.core.extension.condition.FlowConditionHandler;
import org.nkk.flow.core.extension.condition.FlowExpression;
import org.nkk.flow.core.extension.condition.SimpleFlowExpression;
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
import org.nkk.flow.core.extension.listener.FlowInstanceListener;
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
import org.nkk.flow.service.NkkFlowEngine;
import org.nkk.flow.service.impl.FlowProcessServiceImpl;
import org.nkk.flow.service.impl.FlowQueryServiceImpl;
import org.nkk.flow.service.impl.FlowRuntimeServiceImpl;
import org.nkk.flow.service.impl.FlowTaskServiceImpl;
import org.nkk.flow.service.impl.NkkFlowEngineImpl;

/**
 * 流程引擎构建器。
 */
public class FlowEngineBuilder {

    private FlowProcessDao processDao;
    private FlowInstanceDao instanceDao;
    private FlowHisInstanceDao hisInstanceDao;
    private FlowExtInstanceDao extInstanceDao;
    private FlowTaskDao taskDao;
    private FlowHisTaskDao hisTaskDao;
    private FlowTaskActorDao taskActorDao;
    private FlowHisTaskActorDao hisTaskActorDao;
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
    private FlowJsonHandler jsonHandler;
    private FlowTaskReminder taskReminder;
    private FlowTaskTrigger taskTrigger;
    private FlowSubProcessHandler subProcessHandler = new DefaultFlowSubProcessHandler();
    private FlowCreateTimeHandler createTimeHandler = new DefaultFlowCreateTimeHandler();
    private FlowTaskCreateInterceptor taskCreateInterceptor;
    private FlowCreateTaskHandler createTaskHandler = new DefaultFlowCreateTaskHandler();
    private FlowModelCache modelCache = new SimpleFlowModelCache();
    private FlowAiHandler flowAiHandler;

    public static FlowEngineBuilder create() {
        return new FlowEngineBuilder();
    }

    public FlowEngineBuilder processDao(FlowProcessDao processDao) {
        this.processDao = processDao;
        return this;
    }

    public FlowEngineBuilder instanceDao(FlowInstanceDao instanceDao) {
        this.instanceDao = instanceDao;
        return this;
    }

    public FlowEngineBuilder hisInstanceDao(FlowHisInstanceDao hisInstanceDao) {
        this.hisInstanceDao = hisInstanceDao;
        return this;
    }

    public FlowEngineBuilder extInstanceDao(FlowExtInstanceDao extInstanceDao) {
        this.extInstanceDao = extInstanceDao;
        return this;
    }

    public FlowEngineBuilder taskDao(FlowTaskDao taskDao) {
        this.taskDao = taskDao;
        return this;
    }

    public FlowEngineBuilder hisTaskDao(FlowHisTaskDao hisTaskDao) {
        this.hisTaskDao = hisTaskDao;
        return this;
    }

    public FlowEngineBuilder taskActorDao(FlowTaskActorDao taskActorDao) {
        this.taskActorDao = taskActorDao;
        return this;
    }

    public FlowEngineBuilder hisTaskActorDao(FlowHisTaskActorDao hisTaskActorDao) {
        this.hisTaskActorDao = hisTaskActorDao;
        return this;
    }

    public FlowEngineBuilder idGenerator(FlowIdGenerator idGenerator) {
        this.idGenerator = idGenerator;
        return this;
    }

    public FlowEngineBuilder expression(FlowExpression expression) {
        this.expression = expression;
        return this;
    }

    public FlowEngineBuilder conditionHandler(FlowConditionHandler conditionHandler) {
        this.conditionHandler = conditionHandler;
        return this;
    }

    public FlowEngineBuilder taskActorProvider(FlowTaskActorProvider taskActorProvider) {
        this.taskActorProvider = taskActorProvider;
        return this;
    }

    public FlowEngineBuilder actorAccessStrategy(FlowActorAccessStrategy actorAccessStrategy) {
        this.actorAccessStrategy = actorAccessStrategy == null
                ? new DefaultFlowActorAccessStrategy()
                : actorAccessStrategy;
        this.taskAccessStrategy = new DefaultFlowTaskAccessStrategy(this.actorAccessStrategy);
        this.startAccessStrategy = new DefaultFlowStartAccessStrategy(this.actorAccessStrategy);
        return this;
    }

    public FlowEngineBuilder taskAccessStrategy(FlowTaskAccessStrategy taskAccessStrategy) {
        this.taskAccessStrategy = taskAccessStrategy;
        return this;
    }

    public FlowEngineBuilder startAccessStrategy(FlowStartAccessStrategy startAccessStrategy) {
        this.startAccessStrategy = startAccessStrategy;
        return this;
    }

    public FlowEngineBuilder instanceAccessStrategy(FlowInstanceAccessStrategy instanceAccessStrategy) {
        this.instanceAccessStrategy = instanceAccessStrategy;
        return this;
    }

    public FlowEngineBuilder creatorProvider(FlowCreatorProvider creatorProvider) {
        this.creatorProvider = creatorProvider;
        return this;
    }

    public FlowEngineBuilder nodeListener(FlowNodeListener nodeListener) {
        this.nodeListener = nodeListener;
        return this;
    }

    public FlowEngineBuilder taskListener(FlowTaskListener taskListener) {
        this.taskListener = taskListener;
        return this;
    }

    public FlowEngineBuilder instanceListener(FlowInstanceListener instanceListener) {
        this.instanceListener = instanceListener;
        return this;
    }

    public FlowEngineBuilder jsonHandler(FlowJsonHandler jsonHandler) {
        this.jsonHandler = jsonHandler;
        return this;
    }

    public FlowEngineBuilder taskReminder(FlowTaskReminder taskReminder) {
        this.taskReminder = taskReminder;
        return this;
    }

    public FlowEngineBuilder taskTrigger(FlowTaskTrigger taskTrigger) {
        this.taskTrigger = taskTrigger;
        return this;
    }

    public FlowEngineBuilder subProcessHandler(FlowSubProcessHandler subProcessHandler) {
        this.subProcessHandler = subProcessHandler;
        return this;
    }

    public FlowEngineBuilder createTimeHandler(FlowCreateTimeHandler createTimeHandler) {
        this.createTimeHandler = createTimeHandler;
        return this;
    }

    public FlowEngineBuilder taskCreateInterceptor(FlowTaskCreateInterceptor taskCreateInterceptor) {
        this.taskCreateInterceptor = taskCreateInterceptor;
        return this;
    }

    public FlowEngineBuilder createTaskHandler(FlowCreateTaskHandler createTaskHandler) {
        this.createTaskHandler = createTaskHandler;
        return this;
    }

    public FlowEngineBuilder modelCache(FlowModelCache modelCache) {
        this.modelCache = modelCache;
        return this;
    }

    public FlowEngineBuilder flowAiHandler(FlowAiHandler flowAiHandler) {
        this.flowAiHandler = flowAiHandler;
        return this;
    }

    public NkkFlowEngine build() {
        requireDaos();
        if (jsonHandler != null) {
            FlowContext.setJsonHandler(jsonHandler);
        }
        FlowContext context = new FlowContext();
        context.setIdGenerator(idGenerator);
        context.setExpression(expression);
        context.setConditionHandler(conditionHandler);
        context.setTaskActorProvider(taskActorProvider);
        context.setActorAccessStrategy(actorAccessStrategy);
        context.setTaskAccessStrategy(taskAccessStrategy);
        context.setStartAccessStrategy(startAccessStrategy);
        context.setInstanceAccessStrategy(instanceAccessStrategy);
        context.setCreatorProvider(creatorProvider);
        context.setNodeListener(nodeListener);
        context.setTaskListener(taskListener);
        context.setInstanceListener(instanceListener);
        context.setTaskReminder(taskReminder);
        context.setTaskTrigger(taskTrigger);
        context.setSubProcessHandler(subProcessHandler);
        context.setCreateTimeHandler(createTimeHandler);
        context.setTaskCreateInterceptor(taskCreateInterceptor);
        context.setCreateTaskHandler(createTaskHandler);
        context.setModelCache(modelCache);
        context.setFlowAiHandler(flowAiHandler);

        FlowTaskServiceImpl taskService = new FlowTaskServiceImpl(context, idGenerator, taskDao, taskActorDao, hisTaskDao, hisTaskActorDao);
        FlowProcessServiceImpl processService = new FlowProcessServiceImpl(processDao, idGenerator);
        FlowRuntimeServiceImpl runtimeService = new FlowRuntimeServiceImpl(context, idGenerator, instanceDao, hisInstanceDao, extInstanceDao, taskDao, taskService);
        FlowQueryServiceImpl queryService = new FlowQueryServiceImpl(instanceDao, hisInstanceDao, extInstanceDao,
                taskDao, taskActorDao, hisTaskDao, hisTaskActorDao);

        context.setTaskService(taskService);
        context.setProcessService(processService);
        context.setRuntimeService(runtimeService);
        context.setQueryService(queryService);
        return new NkkFlowEngineImpl().configure(context);
    }

    private void requireDaos() {
        if (processDao == null || instanceDao == null || hisInstanceDao == null || extInstanceDao == null
                || taskDao == null || hisTaskDao == null || taskActorDao == null || hisTaskActorDao == null) {
            throw new IllegalStateException("流程 DAO 实现不能为空");
        }
    }
}

