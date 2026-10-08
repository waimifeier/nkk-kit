package org.nkk.flow.web.service;

import org.nkk.flow.web.extension.FlowDesignerConfigProvider;
import org.nkk.flow.web.model.FlowDesignerConfig;

/**
 * 流程设计器节点配置数据服务。
 */
public class FlowDesignerConfigService {

    private final FlowDesignerConfigProvider configProvider;

    public FlowDesignerConfigService(FlowDesignerConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    /**
     * 获取设计器节点配置（节点注册表）。
     *
     * @return 节点注册表；提供者返回 {@code null} 时回退默认注册表
     */
    public FlowDesignerConfig getDesignerConfig() {
        FlowDesignerConfig config = configProvider.getDesignerConfig();
        if (config == null) {
            config = FlowDesignerConfig.defaults();
        }
        if (config.getVersion() == null) {
            config.setVersion(1);
        }
        return config;
    }
}
