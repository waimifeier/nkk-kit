package org.nkk.flow.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.model.FlowProcessModel;

import java.io.Serializable;

/**
 * 流程实例扩展模型。
 */
@Data
@TableName("flow_ext_instance")
public class FlowExtInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID，与流程实例 ID 保持一致。
     */
    @TableId(type = IdType.INPUT)
    private Long id;

    /**
     * 租户 ID，用于多租户场景下的数据隔离。
     */
    private String tenantId;

    /**
     * 流程定义 ID。
     */
    private Long processId;

    /**
     * 流程定义名称。
     */
    private String processName;

    /**
     * 流程分类。
     */
    private String processType;

    /**
     * 流程模型 JSON 内容，对应 {@link FlowProcessModel}。
     */
    private String modelContent;

    /**
     * 将流程模型 JSON 反序列化为流程模型对象，并补齐父节点引用。
     *
     * @return 流程模型对象
     */
    public FlowProcessModel model() {
        FlowProcessModel model = FlowContext.fromJson(modelContent, FlowProcessModel.class);
        if (model != null) {
            model.buildParentNode();
        }
        return model;
    }

    /**
     * 根据流程实例和流程定义创建流程实例扩展模型。
     *
     * @param instance 流程实例
     * @param process 流程定义
     * @return 流程实例扩展模型
     */
    public static FlowExtInstance of(FlowInstance instance, FlowProcess process) {
        FlowExtInstance ext = new FlowExtInstance();
        ext.setId(instance.getId());
        ext.setTenantId(instance.getTenantId());
        ext.setProcessId(process.getId());
        ext.setProcessName(process.getProcessName());
        ext.setProcessType(process.getProcessType());
        ext.setModelContent(process.getModelContent());
        return ext;
    }
}


