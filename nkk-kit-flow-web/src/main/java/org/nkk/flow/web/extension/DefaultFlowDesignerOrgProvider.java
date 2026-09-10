package org.nkk.flow.web.extension;

import org.nkk.flow.web.model.FlowDesignerOption;
import org.nkk.flow.web.model.FlowDesignerTreeNode;

import java.util.Collections;
import java.util.List;

/**
 * 默认设计器组织数据提供者。
 *
 * <p>默认返回空数据，避免 starter 引入后强制依赖使用方组织表。实际项目应注册自己的
 * {@link FlowDesignerOrgProvider} Bean。</p>
 */
public class DefaultFlowDesignerOrgProvider implements FlowDesignerOrgProvider {

    @Override
    public List<FlowDesignerTreeNode> listDepartmentTree() {
        return Collections.emptyList();
    }

    @Override
    public List<FlowDesignerTreeNode> listEmployeeTree() {
        return Collections.emptyList();
    }

    @Override
    public List<FlowDesignerOption> listRoles() {
        return Collections.emptyList();
    }
}
