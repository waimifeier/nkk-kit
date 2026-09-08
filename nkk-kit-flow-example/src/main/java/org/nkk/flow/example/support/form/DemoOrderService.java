package org.nkk.flow.example.support.form;

import cn.hutool.core.util.IdUtil;
import org.nkk.flow.entity.FlowHisInstance;
import org.nkk.flow.entity.FlowHisTask;
import org.nkk.flow.entity.FlowHisTaskActor;
import org.nkk.flow.web.extension.FlowApprovalCallbackHandler;
import org.nkk.flow.web.extension.annotation.FlowApproval;
import org.nkk.flow.web.extension.annotation.FlowApproval.ReturnCondition;
import org.nkk.flow.web.extension.annotation.FlowBusinessKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 示例业务订单服务。
 *
 * <p>一个 Service 同时承担两个角色：</p>
 * <ol>
 *   <li><b>发起审批</b>：业务方法加 {@code @FlowApproval}，方法成功返回后自动发起流程。</li>
 *   <li><b>接收回调</b>：实现 {@link FlowApprovalCallbackHandler}，审批流转时自动收到通知。</li>
 * </ol>
 *
 * <h3>为什么不单独建 Handler 类？</h3>
 * <ul>
 *   <li>Service 本身就是 Spring Bean，已经注入了自己的 Repository。</li>
 *   <li>业务逻辑（更新状态）和发起逻辑在同一个类里，代码更内聚。</li>
 *   <li>不需要额外的 Handler 类和 Repository 注入。</li>
 * </ul>
 *
 * <h3>会不会循环依赖？</h3>
 * <p>不会。</p>
 * <ul>
 *   <li>{@code @FlowApproval} 是 AOP 切面，方法执行后在事务提交时触发 runtimeService.start()。</li>
 *   <li>{@code FlowApprovalCallbackHandler} 回调是后续审批流转时通过 Spring Event 触发的。</li>
 *   <li>两者时序上完全分离，不存在循环引用。</li>
 * </ul>
 *
 * <h3>supports 路由策略</h3>
 * <p>本 Service 管理多种业务单据（采购订单、请假单、报销单），对应不同的 processKey。
 * {@code supports} 返回 true 接收所有回调，然后在回调方法内通过
 * {@code instance.getProcessKey()} 或 {@code instance.getBusinessKey()} 前缀做分发。</p>
 */
@Service
public class DemoOrderService implements FlowApprovalCallbackHandler {

    private static final Logger log = LoggerFactory.getLogger(DemoOrderService.class);

    /** 模拟业务表：key = businessKey（订单号），value = 订单数据 + 审批状态 */
    private static final Map<String, DemoOrder> ORDERS = new ConcurrentHashMap<>();

    // ==================== 1. 发起审批：业务方法 + @FlowApproval ====================

    /**
     * 提交采购订单 → 自动发起采购审批流程。
     */
    @Transactional(rollbackFor = Exception.class)
    @FlowApproval(processKey = "purchase-order", businessKeyFrom = "orderNo")
    public DemoOrder submitPurchase(DemoOrder order) {
        if (order == null || order.getAmount() == null || order.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("订单金额必须大于 0");
        }
        if (order.getOrderNo() == null) {
            order.setOrderNo("PO-" + IdUtil.simpleUUID().substring(0, 10));
        }
        order.setOrderType("purchase");
        order.setStatus("PENDING_APPROVAL");
        ORDERS.put(order.getOrderNo(), order);
        log.info("📦 [业务] 采购订单已保存，orderNo={}, amount={}", order.getOrderNo(), order.getAmount());
        // 返回值非 null → 切面在事务提交后自动发起流程
        return order;
    }

    /**
     * 提交请假单 → 自动发起请假审批流程。
     */
    @Transactional(rollbackFor = Exception.class)
    @FlowApproval(processKey = "leave")
    public DemoOrder submitLeave(@FlowBusinessKey String orderNo, BigDecimal days, String reason) {
        DemoOrder order = new DemoOrder();
        order.setOrderNo(orderNo);
        order.setOrderType("leave");
        order.setAmount(days);
        order.setApplyDate(LocalDate.now());
        order.setRemark(reason);
        order.setStatus("PENDING_APPROVAL");
        ORDERS.put(orderNo, order);
        log.info("📋 [业务] 请假单已保存，orderNo={}, days={}, reason={}", orderNo, days, reason);
        return order;
    }

    /**
     * 提交报销单 → 自动发起报销审批流程。
     */
    @FlowApproval(processKey = "expense", afterCommit = false, successOn = ReturnCondition.ALWAYS)
    public void submitExpense(DemoOrder expense) {
        expense.setOrderNo("EX-" + IdUtil.simpleUUID().substring(0, 10));
        expense.setOrderType("expense");
        expense.setStatus("PENDING_APPROVAL");
        ORDERS.put(expense.getOrderNo(), expense);
        log.info("💰 [业务] 报销单已保存，orderNo={}", expense.getOrderNo());
    }

    // ==================== 2. 接收回调：实现 FlowApprovalCallbackHandler ====================

    /**
     * 接收所有流程的回调，然后在内部按 processKey 分发。
     *
     * <p>如果只想处理特定流程，可以覆盖为：
     * {@code return "purchase-order".equals(processKey);}</p>
     */
    @Override
    public boolean supports(String processKey) {
        // 本 Service 管理 purchase-order / leave / expense 三种流程
        return "purchase-order".equals(processKey)
                || "leave".equals(processKey)
                || "expense".equals(processKey);
    }

    @Override
    public void onInstanceStart(FlowHisInstance instance, Map<String, Object> variables) {
        updateFlowInfo(instance.getBusinessKey(), instance.getId(), "PENDING");
        log.info("🟢 [审批回调] 流程发起: processKey={}, instanceId={}, businessKey={}",
                instance.getProcessKey(), instance.getId(), instance.getBusinessKey());
    }

    @Override
    public void onInstanceComplete(FlowHisInstance instance) {
        updateFlowInfo(instance.getBusinessKey(), null, "APPROVED");
        log.info("✅ [审批回调] 审批通过: processKey={}, businessKey={}",
                instance.getProcessKey(), instance.getBusinessKey());
    }

    @Override
    public void onInstanceReject(FlowHisInstance instance) {
        updateFlowInfo(instance.getBusinessKey(), null, "REJECTED");
        log.info("❌ [审批回调] 审批拒绝: processKey={}, businessKey={}",
                instance.getProcessKey(), instance.getBusinessKey());
    }

    @Override
    public void onInstanceRevoke(FlowHisInstance instance) {
        updateFlowInfo(instance.getBusinessKey(), null, "REVOKED");
        log.info("📤 [审批回调] 发起人撤回: processKey={}, businessKey={}",
                instance.getProcessKey(), instance.getBusinessKey());
    }

    @Override
    public void onInstanceTerminate(FlowHisInstance instance) {
        updateFlowInfo(instance.getBusinessKey(), null, "TERMINATED");
        log.info("🛑 [审批回调] 终止/超时: processKey={}, businessKey={}, state={}",
                instance.getProcessKey(), instance.getBusinessKey(), instance.getInstanceState());
    }

    @Override
    public void onTaskComplete(FlowHisTask task, List<FlowHisTaskActor> actors) {
        log.info("📝 [审批回调] 任务完成: taskName={}, opinion={}", task.getTaskName(), task.getOpinion());
    }

    /**
     * 更新业务表的审批关联字段。
     *
     * @param businessKey 业务主键（订单号）
     * @param instanceId  审批实例 ID（null 表示只更新状态）
     * @param flowStatus  审批状态
     */
    private void updateFlowInfo(String businessKey, Long instanceId, String flowStatus) {
        DemoOrder order = ORDERS.get(businessKey);
        if (order == null) {
            log.warn("[审批回调] 找不到业务单据，businessKey={}", businessKey);
            return;
        }
        if (instanceId != null) {
            order.setFlowInstanceId(instanceId);
        }
        order.setFlowStatus(flowStatus);
        // 实际项目中：orderRepository.updateFlowStatus(businessKey, instanceId, flowStatus);
    }

    // ==================== 3. 业务表模型 ====================

    /**
     * 模拟业务订单对象。
     *
     * <h3>实际数据库表结构（建议）</h3>
     * <pre>{@code
     * CREATE TABLE t_order (
     *     id             BIGINT PRIMARY KEY AUTO_INCREMENT,
     *     order_no       VARCHAR(50) NOT NULL COMMENT '订单号（= businessKey）',
     *     order_type     VARCHAR(20) COMMENT 'purchase/leave/expense',
     *     amount         DECIMAL(12,2),
     *     apply_date     DATE,
     *     remark         VARCHAR(500),
     *     status         VARCHAR(32) COMMENT '审批前状态',
     *     flow_instance_id BIGINT COMMENT '当前审批实例ID',
     *     flow_status    VARCHAR(32) DEFAULT 'NONE'
     *         COMMENT '审批状态：NONE/PENDING/APPROVED/REJECTED/REVOKED/TERMINATED',
     *     create_time    DATETIME DEFAULT CURRENT_TIMESTAMP,
     *     INDEX idx_order_no (order_no),
     *     INDEX idx_flow_instance_id (flow_instance_id)
     * );
     * }</pre>
     */
    @lombok.Data
    public static class DemoOrder {
        private String orderNo;
        private String orderType;
        private BigDecimal amount;
        private LocalDate applyDate;
        private String remark;
        private String status;

        // 审批关联字段
        private Long flowInstanceId;
        private String flowStatus = "NONE";
    }

    // ==================== 4. 示例查询方法 ====================

    /** 查询业务表状态（演示用）。 */
    public DemoOrder getOrder(String orderNo) {
        return ORDERS.get(orderNo);
    }
}
