package org.nkk.flow.web.extension;

import org.nkk.flow.web.model.FlowDesignerFormOption;
import org.nkk.flow.web.model.FlowFieldMeta;

import java.util.Collections;
import java.util.List;

/**
 * 默认表单数据提供者。
 *
 * <p>默认返回空列表，避免 starter 强依赖使用方的表单系统。
 * 使用方可以通过实现 {@link FlowDesignerFormProvider} 覆盖 Bean，
 * 提供自定义表单或业务表单的下拉选项和字段元数据。</p>
 */
public class DefaultFlowDesignerFormProvider implements FlowDesignerFormProvider {

    @Override
    public List<FlowDesignerFormOption> listForms(String sourceType) {
        return Collections.emptyList();
    }

    @Override
    public List<FlowFieldMeta> listFormFields(String formKey, Integer formVersion, String sourceType) {
        return Collections.emptyList();
    }
}
