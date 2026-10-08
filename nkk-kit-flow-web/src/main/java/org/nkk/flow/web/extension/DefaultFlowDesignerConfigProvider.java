package org.nkk.flow.web.extension;

import org.nkk.flow.web.model.FlowDesignerConfig;

/**
 * 默认设计器节点配置提供者。
 *
 * <p>返回与前端内置默认全集对齐的完整注册表（{@link FlowDesignerConfig#defaults()}），
 * 使接口返回的配置完整自解释，设计器表现与前端未指定配置时一致。
 * 需要替换时注册自己的 {@link FlowDesignerConfigProvider} Bean，返回使用方
 * 自己的完整配置（写什么节点就下发什么节点，整体替换、不与默认注册表合并）。</p>
 */
public class DefaultFlowDesignerConfigProvider implements FlowDesignerConfigProvider {

    @Override
    public FlowDesignerConfig getDesignerConfig() {
        return FlowDesignerConfig.defaults();
    }
}
