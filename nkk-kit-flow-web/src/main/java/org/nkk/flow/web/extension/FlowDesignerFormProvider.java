package org.nkk.flow.web.extension;

import org.nkk.flow.web.model.FlowDesignerFormOption;

import java.util.List;

/**
 * 流程设计器表单下拉框提供者。
 *
 * <p>使用方通过实现该接口，把自定义表单、业务表单、表单版本等可选数据暴露给设计器。
 * starter 不内置表单主数据，只负责提供统一的返回模型和查询入口。</p>
 */
public interface FlowDesignerFormProvider {

    /**
     * 查询表单下拉数据。
     *
     * @param sourceType 表单来源类型，取值见 {@code FlowFormFieldEnum.SourceType}；为空时返回全部
     * @return 表单下拉选项列表
     */
    List<FlowDesignerFormOption> listForms(String sourceType);
}
