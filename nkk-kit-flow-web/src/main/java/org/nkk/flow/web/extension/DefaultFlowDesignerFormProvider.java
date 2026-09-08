package org.nkk.flow.web.extension;

import org.nkk.flow.web.model.FlowDesignerFormOption;

import java.util.Collections;
import java.util.List;

/**
 * 默认表单下拉框提供者。
 *
 * <p>默认返回空列表，避免 starter 强依赖使用方的表单系统。</p>
 */
public class DefaultFlowDesignerFormProvider implements FlowDesignerFormProvider {

    @Override
    public List<FlowDesignerFormOption> listForms(String sourceType) {
        return Collections.emptyList();
    }
}
