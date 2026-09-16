package org.nkk.flow.web.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.nkk.core.beans.common.BaseQuery;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 待办中心分页查询请求。
 *
 * <p>分页参数 {@code current}（页码，从 1 开始）、{@code size}（每页条数）
 * 继承自 {@link BaseQuery}。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowTodoPageRequest extends BaseQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 查询类型，必填，取值见 {@link org.nkk.flow.enums.core.FlowTodoTypeEnum}：
     * 1-我申请的、2-待我审批、3-我已审批、4-抄送消息。
     */
    @NotNull(message = "查询类型不能为空！")
    private Integer type;

    /**
     * 用户 ID，可选。为空时使用 {@code FlowCreatorProvider} 获取当前用户。
     */
    private String userId;

    /**
     * 审批状态，仅 {@code type=1}（我申请的）时生效，可选；
     * 取值见 {@link org.nkk.flow.enums.core.FlowInstanceEnum.InstanceState}：
     * 0-审批中、1-审批通过、2-审批拒绝、3-已撤销；
     * 不传（为空）表示查询全部状态。其他 type 下该参数不生效。
     */
    private Integer instanceState;
}
