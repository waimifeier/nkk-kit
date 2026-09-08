package org.nkk.flow.web.service;

import cn.hutool.core.util.StrUtil;
import org.nkk.flow.web.extension.FlowDesignerFormProvider;
import org.nkk.flow.web.model.FlowDesignerFormOption;
import org.nkk.flow.web.model.FlowFieldMeta;

import java.util.Collections;
import java.util.List;

/**
 * 流程设计器表单字段查询服务。
 *
 * <p>字段元数据不再落独立表，统一走 {@link FlowDesignerFormProvider} SPI 获取。
 * Provider 未实现时返回空列表——流程发布时前端会把字段快照直接带入
 * {@code extendConfig.metaForm.fields}，后续条件分支和表单渲染从流程模型 JSON 读取即可。</p>
 */
public class FlowDesignerFormService {

    private final FlowDesignerFormProvider formProvider;

    public FlowDesignerFormService(FlowDesignerFormProvider formProvider) {
        this.formProvider = formProvider;
    }

    /**
     * 查询指定表单的字段元数据。
     *
     * <p>优先走 {@link FlowDesignerFormProvider#listFormFields}，Provider 未实现时返回空列表。
     * 使用方如果需要持久化字段快照，可以在流程发布时由前端直接写入 metaForm.fields。</p>
     *
     * @param formKey 表单编码
     * @param formVersion 表单版本，空时取 Provider 返回的最新版本
     * @param sourceType 表单来源类型，form/business
     * @return 字段元数据列表
     */
    public List<FlowFieldMeta> listFormFields(String formKey, Integer formVersion, String sourceType) {
        if (formProvider == null || StrUtil.isBlank(formKey)) {
            return Collections.emptyList();
        }
        return formProvider.listFormFields(StrUtil.trim(formKey), formVersion, sourceType);
    }

    /**
     * 查询设计器可选表单下拉数据。
     *
     * @param sourceType 表单来源类型，form/business
     * @return 表单下拉选项
     */
    public List<FlowDesignerFormOption> listForms(String sourceType) {
        if (formProvider == null) {
            return Collections.emptyList();
        }
        return formProvider.listForms(sourceType);
    }
}
