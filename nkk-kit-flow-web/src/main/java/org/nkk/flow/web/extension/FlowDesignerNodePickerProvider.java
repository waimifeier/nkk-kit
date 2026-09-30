package org.nkk.flow.web.extension;

import org.nkk.flow.web.model.FlowDesignerNodePickerItem;

import java.util.List;

/**
 * 流程设计器节点选择器数据提供者。
 *
 * <p>用于向流程设计器暴露可添加的节点类型列表。
 * 使用方应从自己的配置系统中查询数据并转换为设计器模型。</p>
 *
 * <p>与 {@link FlowDesignerCategoryProvider} 的区别：</p>
 * <ul>
 *   <li>{@code FlowDesignerCategoryProvider} 提供流程分类、业务分组等自定义分类数据</li>
 *   <li>{@code FlowDesignerNodePickerProvider} 提供可添加节点类型的定义（type/label/description/icon/color）</li>
 * </ul>
 */
public interface FlowDesignerNodePickerProvider {

    /**
     * 查询节点选择器选项列表。
     *
     * <p>列表按前端展示顺序排列，需包含 type、label、description 三个字段，
     * 可选携带 icon（在线图标 http 地址）与 color（十六进制主题色），未配置时前端回退到内置样式。
     * 默认实现返回空列表，使用方应注册自定义 Bean 覆盖。</p>
     *
     * @return 节点选择器选项列表
     */
    List<FlowDesignerNodePickerItem> listNodePickerItems();
}
