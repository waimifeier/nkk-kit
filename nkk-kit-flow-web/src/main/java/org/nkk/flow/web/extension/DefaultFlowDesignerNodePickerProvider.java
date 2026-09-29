package org.nkk.flow.web.extension;

import org.nkk.flow.web.model.FlowDesignerNodePickerItem;

import java.util.Collections;
import java.util.List;

/**
 * 默认设计器节点选择器数据提供者。
 *
 * <p>默认返回空数据，避免 starter 引入后强制依赖使用方建表或配置。
 * 实际项目应注册自己的 {@link FlowDesignerNodePickerProvider} Bean。</p>
 */
public class DefaultFlowDesignerNodePickerProvider implements FlowDesignerNodePickerProvider {

    @Override
    public List<FlowDesignerNodePickerItem> listNodePickerItems() {
        return Collections.emptyList();
    }
}
