package org.nkk.flow.web.extension;

import org.nkk.flow.web.model.FlowDesignerCategoryNode;

import java.util.Collections;
import java.util.List;

/**
 * 默认设计器分类数据提供者。
 *
 * <p>默认返回空数据，避免 starter 引入后强制依赖使用方分类表。实际项目应注册自己的
 * {@link FlowDesignerCategoryProvider} Bean。</p>
 */
public class DefaultFlowDesignerCategoryProvider implements FlowDesignerCategoryProvider {

    @Override
    public List<FlowDesignerCategoryNode> listCategoryTree() {
        return Collections.emptyList();
    }
}
