package org.nkk.flow.web.service;

import org.nkk.flow.web.extension.FlowDesignerOrgProvider;
import org.nkk.flow.web.model.FlowDesignerOption;
import org.nkk.flow.web.model.FlowDesignerTreeNode;

import java.util.List;

/**
 * 流程设计器组织数据服务。
 */
public class FlowDesignerOrgService {

    private final FlowDesignerOrgProvider orgProvider;

    public FlowDesignerOrgService(FlowDesignerOrgProvider orgProvider) {
        this.orgProvider = orgProvider;
    }

    public List<FlowDesignerTreeNode> listDepartmentTree() {
        return orgProvider.listDepartmentTree();
    }

    public List<FlowDesignerTreeNode> listEmployeeTree() {
        return orgProvider.listEmployeeTree();
    }

    public List<FlowDesignerOption> listRoles() {
        return orgProvider.listRoles();
    }
}
