package org.nkk.flow.web.extension;

import org.nkk.flow.web.model.FlowDesignerFormOption;
import org.nkk.flow.web.model.FlowFieldMeta;

import java.util.Collections;
import java.util.List;

/**
 * 流程设计器表单数据提供者。
 *
 * <p>使用方通过实现该接口，把自定义表单、业务表单暴露给设计器和运行时。
 * starter 不内置表单主数据，只负责提供统一的返回模型和查询入口。</p>
 *
 * <h3>两种使用模式</h3>
 * <ul>
 *   <li><b>自定义表单</b>：用户系统内置了表单设计器，可以实现 {@link #listForms} 返回可选表单，
 *       再实现 {@link #listFormFields} 返回字段元数据。流程发布时前端会把字段快照带到 metaForm.fields 里，
 *       后续条件分支和表单渲染直接读流程模型 JSON，不再依赖 Provider。</li>
 *   <li><b>业务表单</b>：用户系统已有业务单据（请假单、采购单等），Provider 可以把业务表结构映射成
 *       {@link FlowFieldMeta} 返回，供设计器条件分支选择字段。业务表单的数据存储在用户业务系统，
 *       审批引擎只通过 businessKey 关联，发起/完成/拒绝/终止等结果通过回调 SPI 通知业务系统。</li>
 * </ul>
 *
 * <h3>未实现 Provider 时</h3>
 * <p>默认实现返回空列表。设计器没有表单可选，但流程模型 JSON 里已经自带 metaForm.fields 快照，
 * 条件分支和运行时表单渲染仍然可用——只要发布流程时前端把字段元数据带上。</p>
 */
public interface FlowDesignerFormProvider {

    /**
     * 查询表单下拉数据。
     *
     * @param sourceType 表单来源类型，取值见 {@code FlowFormFieldEnum.SourceType}；为空时返回全部
     * @return 表单下拉选项列表
     */
    List<FlowDesignerFormOption> listForms(String sourceType);

    /**
     * 查询表单字段元数据。
     *
     * <p>设计器在条件分支节点需要显示可选字段时调用。业务表单可以从数据库元数据或注解映射生成字段描述，
     * 自定义表单可以返回 Provider 自己的字段定义。</p>
     *
     * <p>默认返回空列表。如果用户系统没有实现自定义表单/业务表单，可以不覆盖此方法，
     * 流程发布时前端会直接把字段快照写入 metaForm.fields，后续条件分支和表单渲染都从流程模型 JSON 读取。</p>
     *
     * @param formKey 表单编码
     * @param formVersion 表单版本，为空时取最新版本
     * @param sourceType 表单来源类型，form/business
     * @return 字段元数据列表
     */
    default List<FlowFieldMeta> listFormFields(String formKey, Integer formVersion, String sourceType) {
        return Collections.emptyList();
    }
}
