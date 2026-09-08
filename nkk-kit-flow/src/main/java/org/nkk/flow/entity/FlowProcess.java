package org.nkk.flow.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;
import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.enums.core.FlowProcessEnum.ProcessState;
import org.nkk.flow.model.FlowProcessModel;

/**
 * 流程定义。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flow_process")
public class FlowProcess extends FlowEntity {

    private static final long serialVersionUID = 1L;

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
     * 流程模型 JSON 内容，对应 {@link FlowProcessModel}。
     */
    private String modelContent;

    /**
     * 排序值，值越小越靠前。
     */
    private Integer sort;

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
     * 校验流程定义是否处于启用状态。
     *
     * @return 当前流程定义
     */
    public FlowProcess checkState() {
        if (!ProcessState.ENABLED.value().equals(processState)) {
            throw new IllegalStateException("流程定义未启用，processKey=" + processKey);
        }
        return this;
    }
}


