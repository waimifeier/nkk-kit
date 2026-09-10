package org.nkk.flow.web.model;

import lombok.Data;
import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.entity.FlowProcess;
import org.nkk.flow.enums.core.FlowProcessEnum.ProcessState;
import org.nkk.flow.model.FlowProcessModel;

import java.io.Serializable;
import java.util.Date;

/**
 * 流程定义展示对象。
 *
 * <p>用于流程列表、分类列表和版本列表，不返回流程模型 JSON 内容。</p>
 */
@Data
public class FlowProcessVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 流程定义 ID。
     */
    private Long id;

    /**
     * 租户 ID。
     */
    private String tenantId;

    /**
     * 创建人 ID。
     */
    private String createId;

    /**
     * 创建人名称。
     */
    private String createBy;

    /**
     * 创建时间。
     */
    private Date createTime;

    /**
     * 流程定义唯一编码，同一流程多版本共享该编码。
     */
    private String processKey;

    /**
     * 流程定义名称。
     */
    private String processName;

    /**
     * 流程图标标识。
     */
    private String processIcon;

    /**
     * 流程分类。
     */
    private String processType;

    /**
     * 流程版本号。
     */
    private Integer processVersion;

    /**
     * 流程实例详情页地址。
     */
    private String instanceUrl;

    /**
     * 流程说明。
     */
    private String remark;

    /**
     * 使用范围，预留给业务侧做流程可见性控制。
     */
    private Integer useScope;

    /**
     * 流程定义状态，取值见 {@link ProcessState}。
     */
    private Integer processState;

    /**
     * 排序值，值越小越靠前。
     */
    private Integer sort;

    /**
     * 流程元表单信息。
     */
    private FlowMetaFormVO metaForm;

    public static FlowProcessVO of(FlowProcess process) {
        if (process == null) {
            return null;
        }
        FlowProcessVO vo = new FlowProcessVO();
        vo.setId(process.getId());
        vo.setTenantId(process.getTenantId());
        vo.setCreateId(process.getCreateId());
        vo.setCreateBy(process.getCreateBy());
        vo.setCreateTime(process.getCreateTime());
        vo.setProcessKey(process.getProcessKey());
        vo.setProcessName(process.getProcessName());
        vo.setProcessIcon(process.getProcessIcon());
        vo.setProcessType(process.getProcessType());
        vo.setProcessVersion(process.getProcessVersion());
        vo.setInstanceUrl(process.getInstanceUrl());
        vo.setRemark(process.getRemark());
        vo.setUseScope(process.getUseScope());
        vo.setProcessState(process.getProcessState());
        vo.setSort(process.getSort());
        vo.setMetaForm(resolveMetaForm(process));
        return vo;
    }

    /**
     * 从流程定义实体解析元表单信息。
     *
     * <p>表单元数据从 {@code flow_process} 独立列读取，字段定义从模型 JSON 的
     * {@code metaFields} 顶层字段读取。</p>
     */
    private static FlowMetaFormVO resolveMetaForm(FlowProcess process) {
        if (process == null) {
            return null;
        }
        FlowMetaFormVO vo = new FlowMetaFormVO();
        vo.setSourceType(process.getFormSourceType());
        vo.setFormId(process.getFormId());
        vo.setFormKey(process.getFormKey());
        vo.setFormVersion(process.getFormVersion());
        vo.setFormName(process.getFormName());

        if (process.getModelContent() != null) {
            try {
                FlowProcessModel model = FlowContext.fromJson(process.getModelContent(), FlowProcessModel.class);
                if (model != null && model.getMetaFields() != null) {
                    vo.setFields(model.getMetaFields());
                }
            } catch (Exception ignored) {
            }
        }
        return vo;
    }
}
