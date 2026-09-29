package org.nkk.flow.web.service;

import org.nkk.flow.web.extension.FlowDesignerNodePickerProvider;
import org.nkk.flow.web.model.FlowDesignerNodePickerItem;

import java.util.List;

/**
 * 流程设计器节点选择器数据服务。
 */
public class FlowDesignerNodePickerService {

    private final FlowDesignerNodePickerProvider pickerProvider;

    public FlowDesignerNodePickerService(FlowDesignerNodePickerProvider pickerProvider) {
        this.pickerProvider = pickerProvider;
    }

    /**
     * 查询节点选择器选项列表。
     *
     * @return 节点选择器选项列表
     */
    public List<FlowDesignerNodePickerItem> listNodePickerItems() {
        return pickerProvider.listNodePickerItems();
    }
}
