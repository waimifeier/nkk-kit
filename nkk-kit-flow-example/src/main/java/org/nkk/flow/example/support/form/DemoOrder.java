package org.nkk.flow.example.support.form;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 示例业务订单。
 *
 * <p>模拟业务系统中的业务单据，实际项目中可能是 JPA/MyBatis-Plus 实体。
 * 通过 {@code @FlowApproval} 注解可以让业务方法在保存后自动发起审批，
 * 业务代码本身不需要感知审批引擎。</p>
 */
@Data
@NoArgsConstructor
public class DemoOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单编号，作为审批 businessKey */
    private String orderNo;

    /** 订单类型 */
    private String orderType;

    /** 金额 */
    private BigDecimal amount;

    /** 申请日期 */
    private LocalDate applyDate;

    /** 申请人 ID */
    private String applicantId;

    /** 申请人名称 */
    private String applicantName;

    /** 备注 */
    private String remark;

    /**
     * 订单状态。业务系统自己维护的状态，审批回调里会根据审批结果更新。
     * PENDING_SUBMIT → PENDING_APPROVAL → APPROVED / REJECTED
     */
    private String status = "PENDING_SUBMIT";

    public DemoOrder(String orderNo, String orderType, BigDecimal amount, LocalDate applyDate,
                     String applicantId, String applicantName, String remark) {
        this.orderNo = orderNo;
        this.orderType = orderType;
        this.amount = amount;
        this.applyDate = applyDate;
        this.applicantId = applicantId;
        this.applicantName = applicantName;
        this.remark = remark;
    }
}
