package org.nkk.flow.web.autoconfigure;

import org.nkk.flow.web.controller.FlowDesignerOrgController;
import org.nkk.flow.web.controller.FlowDesignerFormController;
import org.nkk.flow.web.controller.FlowDesignerProcessController;
import org.nkk.flow.web.controller.FlowDesignerRuntimeController;
import org.nkk.flow.web.controller.FlowDesignerTodoController;
import org.nkk.flow.web.extension.DefaultFlowDesignerOrgProvider;
import org.nkk.flow.web.extension.FlowDesignerOrgProvider;
import org.nkk.flow.core.extension.id.FlowIdGenerator;
import org.nkk.flow.core.extension.identity.FlowCreatorProvider;
import org.nkk.flow.dao.FlowFormFieldDao;
import org.nkk.flow.dao.FlowHisInstanceDao;
import org.nkk.flow.dao.FlowHisTaskActorDao;
import org.nkk.flow.dao.FlowHisTaskDao;
import org.nkk.flow.dao.FlowProcessDao;
import org.nkk.flow.dao.FlowTaskActorDao;
import org.nkk.flow.dao.FlowTaskDao;
import org.nkk.flow.service.NkkFlowEngine;
import org.nkk.flow.web.service.FlowDesignerOrgService;
import org.nkk.flow.web.service.FlowDesignerFormService;
import org.nkk.flow.web.service.FlowDesignerProcessService;
import org.nkk.flow.web.service.FlowDesignerRuntimeService;
import org.nkk.flow.web.service.FlowDesignerTodoService;
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
@ConditionalOnProperty(prefix = "nkk.flow.web", name = "enabled", havingValue = "true", matchIfMissing = true)
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
    public FlowDesignerFormService flowDesignerFormService(FlowFormFieldDao formFieldDao,
                                                           ObjectProvider<FlowCreatorProvider> creatorProvider,
                                                           FlowIdGenerator idGenerator) {
        return new FlowDesignerFormService(formFieldDao, creatorProvider.getIfAvailable(), idGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowDesignerFormController flowDesignerFormController(FlowDesignerFormService formService) {
        return new FlowDesignerFormController(formService);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowDesignerProcessService flowDesignerProcessService(NkkFlowEngine flowEngine,
                                                                 ObjectProvider<FlowCreatorProvider> creatorProvider,
                                                                 FlowDesignerFormService formService) {
        return new FlowDesignerProcessService(flowEngine, creatorProvider.getIfAvailable(), formService);
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
}
