package org.nkk.flow.autoconfigure;

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
import org.nkk.flow.core.extension.schedule.FlowJobLock;
import org.nkk.flow.core.extension.schedule.FlowTaskReminder;
import org.nkk.flow.core.extension.schedule.FlowTaskTrigger;
import org.nkk.flow.core.extension.schedule.LocalFlowJobLock;
import org.nkk.flow.core.extension.schedule.NkkFlowScheduler;
import org.nkk.flow.core.extension.subprocess.DefaultFlowSubProcessHandler;
import org.nkk.flow.core.extension.subprocess.FlowSubProcessHandler;
import org.nkk.flow.core.extension.task.DefaultFlowCreateTaskHandler;
import org.nkk.flow.core.extension.task.FlowCreateTaskHandler;
import org.nkk.flow.core.extension.task.FlowTaskCreateInterceptor;
import org.nkk.flow.core.extension.time.DefaultFlowCreateTimeHandler;
import org.nkk.flow.core.extension.time.FlowCreateTimeHandler;
import org.nkk.flow.dao.FlowExtInstanceDao;
import org.nkk.flow.dao.FlowHisInstanceDao;
import org.nkk.flow.dao.FlowHisTaskActorDao;
import org.nkk.flow.dao.FlowHisTaskDao;
import org.nkk.flow.dao.FlowInstanceDao;
import org.nkk.flow.dao.FlowProcessDao;
import org.nkk.flow.dao.FlowTaskActorDao;
import org.nkk.flow.dao.FlowTaskDao;
import org.nkk.flow.service.FlowProcessService;
import org.nkk.flow.service.FlowQueryService;
import org.nkk.flow.service.FlowRuntimeService;
import org.nkk.flow.service.FlowTaskService;
import org.nkk.flow.service.NkkFlowEngine;
import org.nkk.flow.service.impl.FlowProcessServiceImpl;
import org.nkk.flow.service.impl.FlowQueryServiceImpl;
import org.nkk.flow.service.impl.FlowRuntimeServiceImpl;
import org.nkk.flow.service.impl.FlowTaskServiceImpl;
import org.nkk.flow.service.impl.NkkFlowEngineImpl;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 审批流 starter 自动配置。
 */
@Configuration
@Import(NkkFlowMybatisPlusConfiguration.class)
@EnableConfigurationProperties(NkkFlowProperties.class)
public class NkkFlowAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FlowIdGenerator flowIdGenerator() {
        return new DefaultFlowIdGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowExpression flowExpression() {
        return new SimpleFlowExpression();
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowConditionHandler flowConditionHandler() {
        return new DefaultFlowConditionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowTaskActorProvider flowTaskActorProvider() {
        return new DefaultFlowTaskActorProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowActorAccessStrategy flowActorAccessStrategy() {
        return new DefaultFlowActorAccessStrategy();
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowTaskAccessStrategy flowTaskAccessStrategy(FlowActorAccessStrategy actorAccessStrategy) {
        return new DefaultFlowTaskAccessStrategy(actorAccessStrategy);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowStartAccessStrategy flowStartAccessStrategy(FlowActorAccessStrategy actorAccessStrategy) {
        return new DefaultFlowStartAccessStrategy(actorAccessStrategy);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowInstanceAccessStrategy flowInstanceAccessStrategy(
            ObjectProvider<FlowCreatorProvider> creatorProvider) {
        DefaultFlowInstanceAccessStrategy strategy = new DefaultFlowInstanceAccessStrategy();
        creatorProvider.ifAvailable(strategy::setCreatorProvider);
        return strategy;
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowSubProcessHandler flowSubProcessHandler() {
        return new DefaultFlowSubProcessHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowCreateTimeHandler flowCreateTimeHandler() {
        return new DefaultFlowCreateTimeHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowCreateTaskHandler flowCreateTaskHandler() {
        return new DefaultFlowCreateTaskHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowModelCache flowModelCache() {
        return new SimpleFlowModelCache();
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowJobLock flowJobLock() {
        return new LocalFlowJobLock();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "flow.eventing", name = "task", havingValue = "true")
    public FlowTaskListener springFlowTaskListener(ApplicationEventPublisher publisher) {
        return new SpringFlowTaskListener(publisher);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "flow.eventing", name = "instance", havingValue = "true")
    public FlowInstanceListener springFlowInstanceListener(ApplicationEventPublisher publisher) {
        return new SpringFlowInstanceListener(publisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowProcessService flowProcessService(FlowProcessDao processDao, FlowIdGenerator idGenerator) {
        return new FlowProcessServiceImpl(processDao, idGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowQueryService flowQueryService(FlowInstanceDao instanceDao, FlowHisInstanceDao hisInstanceDao,
                                             FlowExtInstanceDao extInstanceDao, FlowTaskDao taskDao,
                                             FlowTaskActorDao taskActorDao, FlowHisTaskDao hisTaskDao,
                                             FlowHisTaskActorDao hisTaskActorDao) {
        return new FlowQueryServiceImpl(instanceDao, hisInstanceDao, extInstanceDao, taskDao, taskActorDao,
                hisTaskDao, hisTaskActorDao);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowTaskService flowTaskService(FlowContext flowContext, FlowIdGenerator idGenerator, FlowTaskDao taskDao,
                                           FlowTaskActorDao taskActorDao, FlowHisTaskDao hisTaskDao,
                                           FlowHisTaskActorDao hisTaskActorDao) {
        return new FlowTaskServiceImpl(flowContext, idGenerator, taskDao, taskActorDao, hisTaskDao, hisTaskActorDao);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowRuntimeService flowRuntimeService(FlowContext flowContext, FlowIdGenerator idGenerator,
                                                 FlowInstanceDao instanceDao, FlowHisInstanceDao hisInstanceDao,
                                                 FlowExtInstanceDao extInstanceDao, FlowTaskDao taskDao,
                                                 FlowTaskService taskService) {
        return new FlowRuntimeServiceImpl(flowContext, idGenerator, instanceDao, hisInstanceDao, extInstanceDao,
                taskDao, taskService);
    }

    @Bean
    @ConditionalOnMissingBean
    public NkkFlowEngine nkkFlowEngine(FlowContext flowContext, FlowProcessService processService,
                                       FlowQueryService queryService, FlowRuntimeService runtimeService,
                                       FlowTaskService taskService) {
        flowContext.setProcessService(processService);
        flowContext.setQueryService(queryService);
        flowContext.setRuntimeService(runtimeService);
        flowContext.setTaskService(taskService);
        return new NkkFlowEngineImpl().configure(flowContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public NkkFlowScheduler nkkFlowScheduler(NkkFlowEngine flowEngine, FlowJobLock flowJobLock) {
        return new NkkFlowScheduler(flowEngine, flowJobLock);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowContext flowContext(FlowIdGenerator idGenerator, FlowExpression expression,
                                   FlowConditionHandler conditionHandler,
                                   FlowTaskActorProvider taskActorProvider,
                                   FlowActorAccessStrategy actorAccessStrategy,
                                   FlowTaskAccessStrategy taskAccessStrategy,
                                   FlowStartAccessStrategy startAccessStrategy,
                                   FlowInstanceAccessStrategy instanceAccessStrategy,
                                   FlowSubProcessHandler subProcessHandler,
                                   FlowCreateTimeHandler createTimeHandler,
                                   FlowCreateTaskHandler createTaskHandler,
                                   FlowModelCache modelCache,
                                   ObjectProvider<FlowJsonHandler> jsonHandler,
                                   ObjectProvider<FlowCreatorProvider> creatorProvider,
                                   ObjectProvider<FlowNodeListener> nodeListener,
                                   ObjectProvider<FlowTaskListener> taskListener,
                                   ObjectProvider<FlowInstanceListener> instanceListener,
                                   ObjectProvider<FlowTaskReminder> taskReminder,
                                   ObjectProvider<FlowTaskTrigger> taskTrigger,
                                   ObjectProvider<FlowTaskCreateInterceptor> taskCreateInterceptor,
                                   ObjectProvider<FlowAiHandler> flowAiHandler) {
        jsonHandler.ifAvailable(FlowContext::setJsonHandler);

        FlowContext context = new FlowContext();
        context.setIdGenerator(idGenerator);
        context.setExpression(expression);
        context.setConditionHandler(conditionHandler);
        context.setTaskActorProvider(taskActorProvider);
        context.setActorAccessStrategy(actorAccessStrategy);
        context.setTaskAccessStrategy(taskAccessStrategy);
        context.setStartAccessStrategy(startAccessStrategy);
        context.setInstanceAccessStrategy(instanceAccessStrategy);
        context.setSubProcessHandler(subProcessHandler);
        context.setCreateTimeHandler(createTimeHandler);
        context.setCreateTaskHandler(createTaskHandler);
        context.setModelCache(modelCache);
        creatorProvider.ifAvailable(context::setCreatorProvider);
        nodeListener.ifAvailable(context::setNodeListener);
        taskListener.ifAvailable(context::setTaskListener);
        instanceListener.ifAvailable(context::setInstanceListener);
        taskReminder.ifAvailable(context::setTaskReminder);
        taskTrigger.ifAvailable(context::setTaskTrigger);
        taskCreateInterceptor.ifAvailable(context::setTaskCreateInterceptor);
        flowAiHandler.ifAvailable(context::setFlowAiHandler);
        return context;
    }
}

