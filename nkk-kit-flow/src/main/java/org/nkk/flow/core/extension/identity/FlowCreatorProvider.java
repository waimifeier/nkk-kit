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

    /**
     * 获取系统操作人（用于自动超时、触发器、子流程等无人操作场景）。
     *
     * <p>使用方注册 {@link FlowCreatorProvider} 时必须覆盖本方法，
     * 返回一个代表"系统"身份的 {@link FlowCreator}（如 {@code FlowCreator.of("0", "系统操作人")}）。
     * 未覆盖时默认返回 {@code null}，调用方会抛异常。</p>
     *
     * @return 系统操作人；未配置时返回 {@code null}
     */
    default FlowCreator getSystemCreator() {
        return null;
    }
}
