package org.nkk.flow.core.extension.identity;

import org.nkk.flow.core.context.FlowCreator;

/**
 * 当前流程操作人提供者。
 *
 * <p>使用方接入 Spring Security、Sa-Token、Shiro 或自有登录体系时，可以注册该 Bean，
 * 由核心流程在未显式传入 {@link FlowCreator} 的入口中获取当前操作人。</p>
 */
public interface FlowCreatorProvider {

    /**
     * 获取当前流程操作人。
     *
     * @return 当前操作人；无法获取时返回 {@code null}
     */
    FlowCreator getCurrentCreator();
}
