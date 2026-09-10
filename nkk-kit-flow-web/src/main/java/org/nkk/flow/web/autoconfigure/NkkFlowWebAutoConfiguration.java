package org.nkk.flow.web.autoconfigure;

import org.nkk.flow.web.controller.FlowDesignerCategoryController;
import org.nkk.flow.web.controller.FlowDesignerOrgController;
import org.nkk.flow.web.controller.FlowDesignerFormController;
import org.nkk.flow.web.controller.FlowDesignerProcessController;
import org.nkk.flow.web.controller.FlowDesignerRuntimeController;
import org.nkk.flow.web.controller.FlowDesignerTodoController;
import org.nkk.flow.web.extension.*;
import org.nkk.flow.core.extension.identity.FlowCreatorProvider;
import org.nkk.flow.dao.FlowHisInstanceDao;
import org.nkk.flow.dao.FlowHisTaskActorDao;
import org.nkk.flow.dao.FlowHisTaskDao;
import org.nkk.flow.dao.FlowProcessDao;
import org.nkk.flow.dao.FlowTaskActorDao;
import org.nkk.flow.dao.FlowTaskDao;
import org.nkk.flow.service.NkkFlowEngine;
import org.nkk.flow.web.service.FlowDesignerCategoryService;
import org.nkk.flow.web.service.FlowDesignerOrgService;
import org.nkk.flow.web.service.FlowDesignerFormService;
import org.nkk.flow.web.service.FlowDesignerProcessService;
import org.nkk.flow.web.service.FlowDesignerRuntimeService;
import org.nkk.flow.web.service.FlowDesignerTodoService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 审批流 Web 扩展自动配置。
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "flow.web", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NkkFlowWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FlowDesignerOrgProvider flowDesignerOrgProvider() {
        return new DefaultFlowDesignerOrgProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowDesignerOrgService flowDesignerOrgService(FlowDesignerOrgProvider orgProvider) {
        return new FlowDesignerOrgService(orgProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowDesignerOrgController flowDesignerOrgController(FlowDesignerOrgService orgService) {
        return new FlowDesignerOrgController(orgService);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowDesignerCategoryProvider flowDesignerCategoryProvider() {
        return new DefaultFlowDesignerCategoryProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowDesignerCategoryService flowDesignerCategoryService(FlowDesignerCategoryProvider categoryProvider) {
        return new FlowDesignerCategoryService(categoryProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowDesignerCategoryController flowDesignerCategoryController(FlowDesignerCategoryService categoryService) {
        return new FlowDesignerCategoryController(categoryService);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowDesignerFormProvider flowDesignerFormProvider() {
        return new DefaultFlowDesignerFormProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowDesignerFormService flowDesignerFormService(FlowDesignerFormProvider formProvider) {
        return new FlowDesignerFormService(formProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowDesignerFormController flowDesignerFormController(FlowDesignerFormService formService) {
        return new FlowDesignerFormController(formService);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowDesignerProcessService flowDesignerProcessService(NkkFlowEngine flowEngine,
                                                                 ObjectProvider<FlowCreatorProvider> creatorProvider) {
        return new FlowDesignerProcessService(flowEngine, creatorProvider.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowDesignerProcessController flowDesignerProcessController(FlowDesignerProcessService processService) {
        return new FlowDesignerProcessController(processService);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowDesignerRuntimeService flowDesignerRuntimeService(NkkFlowEngine flowEngine) {
        return new FlowDesignerRuntimeService(flowEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowDesignerRuntimeController flowDesignerRuntimeController(FlowDesignerRuntimeService runtimeService) {
        return new FlowDesignerRuntimeController(runtimeService);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowDesignerTodoService flowDesignerTodoService(NkkFlowEngine flowEngine,
                                                           ObjectProvider<FlowCreatorProvider> creatorProvider,
                                                           FlowProcessDao processDao,
                                                           FlowHisInstanceDao hisInstanceDao,
                                                           FlowTaskDao taskDao,
                                                           FlowTaskActorDao taskActorDao,
                                                           FlowHisTaskDao hisTaskDao,
                                                           FlowHisTaskActorDao hisTaskActorDao) {
        return new FlowDesignerTodoService(flowEngine, creatorProvider.getIfAvailable(), processDao, hisInstanceDao,
                taskDao, taskActorDao, hisTaskDao, hisTaskActorDao);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowDesignerTodoController flowDesignerTodoController(FlowDesignerTodoService todoService) {
        return new FlowDesignerTodoController(todoService);
    }

    /**
     * 自动发起审批切面。
     *
     * <p>拦截 {@code @FlowApproval} 注解的业务方法，方法成功后自动发起审批实例。
     * 需要 classpath 中有 AspectJ 依赖（spring-boot-starter-aop）。</p>
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
    public FlowApprovalAutoStartAspect flowApprovalAutoStartAspect() {
        return new FlowApprovalAutoStartAspect();
    }

    /**
     * 审批回调适配器。
     *
     * <p>监听引擎的 {@code NkkFlowInstanceEvent} 和 {@code NkkFlowTaskEvent} Spring 事件，
     * 翻译为 {@code FlowApprovalCallbackHandler} 的业务友好回调方法。
     * 使用方只需实现一个或多个 {@code FlowApprovalCallbackHandler} Bean，
     * 就能在审批生命周期节点收到通知并更新自己的业务表状态。</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public FlowApprovalCallbackAdapter flowApprovalCallbackAdapter() {
        return new FlowApprovalCallbackAdapter();
    }
}
