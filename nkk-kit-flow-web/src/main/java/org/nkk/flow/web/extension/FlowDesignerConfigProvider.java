package org.nkk.flow.web.extension;

import org.nkk.flow.web.model.FlowDesignerConfig;

/**
 * 流程设计器节点配置提供者。
 *
 * <p>用于向流程设计器暴露节点注册表（GET {@code /designer/config}）。
 * 注册表统一承载两类配置：</p>
 * <ul>
 *   <li>添加节点弹窗：nodes 中声明的节点类型（type/label/description/icon/color），
 *       0 发起 / -1 结束 / 3 条件配置为结构节点，声明仅用于抽屉属性配置，固定不进弹窗</li>
 *   <li>节点配置抽屉：每个节点的属性选项子集、区块显隐与权限操作白名单（properties）</li>
 * </ul>
 *
 * <p>Provider 的返回值即最终下发的完整配置：写什么节点就下发什么节点，
 * 与内置默认注册表是整体替换关系、不做合并；未定义 Provider 或返回 {@code null}
 * 时才回退 {@link FlowDesignerConfig#defaults()}。因此替换配置时必须返回
 * 完整节点列表与属性（节点、文案、图标、选项子集一次写全），而非只写差异部分。</p>
 */
public interface FlowDesignerConfigProvider {

    /**
     * 获取设计器节点配置。
     *
     * @return 节点注册表；返回 {@code null} 时回退默认注册表
     *         （{@link FlowDesignerConfig#defaults()}）
     */
    FlowDesignerConfig getDesignerConfig();
}
