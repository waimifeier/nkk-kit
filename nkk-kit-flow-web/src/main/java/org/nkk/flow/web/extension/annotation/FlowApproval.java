package org.nkk.flow.web.extension.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自动发起审批注解。
 *
 * <p>标注在业务 Service 方法上，方法执行成功（返回非 null 或 boolean=true）后，
 * 自动发起指定流程的审批实例。业务代码无需感知审批引擎，实现无侵入式接入。</p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 方式一：显式指定流程 key + businessKey 来自方法参数
 * @FlowApproval(processKey = "leave")
 * public LeaveOrder submitLeave(@FlowBusinessKey String orderNo, @FlowVariables Map<String, Object> vars) {
 *     LeaveOrder order = leaveRepository.save(new LeaveOrder(orderNo, vars));
 *     return order;
 * }
 *
 * // 方式二：businessKey 从返回值中提取
 * @FlowApproval(processKey = "purchase-order", businessKeyFrom = "id")
 * public PurchaseOrder submitPurchase(PurchaseOrder order) {
 *     return purchaseRepository.save(order);
 * }
 *
 * // 方式三：指定流程定义 ID + 事务提交后启动
 * @FlowApproval(processId = 1001L, afterCommit = true)
 * public Order submit(Order order) { ... }
 * }</pre>
 *
 * <h3>参数提取规则</h3>
 * <ul>
 *   <li>{@code businessKey}：优先取 {@code @FlowBusinessKey} 标注的方法参数；
 *       否则尝试从返回值中按 {@code businessKeyFrom} 属性名提取；
 *       都没有时返回值的 {@code toString()} 作为兜底。</li>
 *   <li>{@code variables}：优先取 {@code @FlowVariables} 标注的 {@code Map<String,Object>} 参数；
 *       否则把返回值的可序列化属性全部收集为变量。</li>
 * </ul>
 *
 * <h3>事务安全</h3>
 * <p>默认 {@code afterCommit=true}，即在业务方法所在事务提交后再发起流程。
 * 这是最安全的方式——业务数据已持久化，流程引擎才能通过 businessKey 查到数据。
 * 如果业务方法本身没有事务，则 afterCommit 仍然能正常工作（回调立即执行）。</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FlowApproval {

    /**
     * 流程 key。与 {@link #processId()} 二选一，优先级低于 processId。
     */
    String processKey() default "";

    /**
     * 流程定义 ID。优先级最高。
     */
    long processId() default 0L;

    /**
     * 流程版本。为空时取最新启用版本。
     */
    int processVersion() default -1;

    /**
     * 从返回值中提取 businessKey 的属性名。
     * 例如返回值是 Order 对象，属性名为 "id"，则 businessKey = order.getId()。
     * 留空时优先取 {@link #processKey()}，再兜底用返回值的 toString()。
     */
    String businessKeyFrom() default "";

    /**
     * 是否在事务提交后再发起流程。
     * 默认 true，避免流程引擎在业务数据未提交时就读不到 businessKey 对应的数据。
     */
    boolean afterCommit() default true;

    /**
     * 方法判定成功的返回值条件。
     * 默认 {@link ReturnCondition#NON_NULL}，即返回值非 null 时才触发流程。
     */
    ReturnCondition successOn() default ReturnCondition.NON_NULL;

    enum ReturnCondition {
        /** 返回值非 null 即成功。 */
        NON_NULL,
        /** 返回值 boolean=true 或 Boolean.TRUE 才成功。 */
        TRUE,
        /** 方法无返回值（void）或返回 null 即成功。 */
        VOID_OR_NULL,
        /** 忽略返回值，方法正常返回（无异常）即成功。 */
        ALWAYS
    }
}
