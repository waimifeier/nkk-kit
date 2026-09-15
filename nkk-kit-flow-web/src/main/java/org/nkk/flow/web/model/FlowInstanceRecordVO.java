package org.nkk.flow.web.model;

import lombok.Data;
import org.nkk.flow.entity.FlowHisInstance;

import java.io.Serializable;
import java.util.Date;

/**
 * 流程实例记录展示对象。
 *
 * <p>用于按流程定义 ID 分页查询已发起的流程实例列表。</p>
 */
@Data
public class FlowInstanceRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 实例 ID（实例主键）。
     */
    private Long instanceId;

    /**
     * 发起人名字（实例创建人名称，createBy）。
     */
    private String starterName;

    /**
     * 流程名称（由流程定义 ID 反查 flow_process.process_name 填充）。
     */
    private String processName;

    /**
     * 提交时间（实例创建时间）。
     */
    private Date submitTime;

    /**
     * 审批状态，取值见 {@link org.nkk.flow.enums.core.FlowInstanceEnum.InstanceState}：
     * {@code 0}=审批中、{@code 1}=审批通过、{@code 2}=审批驳回、{@code 3}=已撤销 等。
     */
    private Integer instanceState;

    /**
     * 摘要：预留字段，当前版本固定返回空值。
     */
    private String summary;

    /**
     * 根据历史实例构建展示对象（流程名称由调用方反查流程定义后填入）。
     *
     * @param instance 历史实例
     * @return 实例展示对象
     */
    public static FlowInstanceRecordVO of(FlowHisInstance instance) {
        if (instance == null) {
            return null;
        }
        FlowInstanceRecordVO vo = new FlowInstanceRecordVO();
        vo.setInstanceId(instance.getId());
        vo.setStarterName(instance.getCreateBy());
        vo.setSubmitTime(instance.getCreateTime());
        vo.setInstanceState(instance.getInstanceState());
        // summary 为预留字段，固定空值
        return vo;
    }
}
