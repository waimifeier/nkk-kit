package org.nkk.flow.core.extension.listener;

/**
 * 节点生命周期监听器。
 *
 * <p>该监听器用于扩展节点级业务动作，例如记录节点轨迹、发送节点消息、在创建任务前后补充业务数据。
 * 与 {@link FlowTaskListener}、{@link FlowInstanceListener} 不同，它关注“模型节点正在执行”这一层。</p>
 */
public interface FlowNodeListener {

    /**
     * 节点开始执行前触发。
     *
     * @param event 节点事件
     */
    default void beforeExecute(FlowNodeEvent event) {
    }

    /**
     * 节点执行完成后触发。
     *
     * @param event 节点事件
     */
    default void afterExecute(FlowNodeEvent event) {
    }

    /**
     * 节点即将创建任务前触发。
     *
     * @param event 节点事件
     */
    default void beforeCreateTask(FlowNodeEvent event) {
    }

    /**
     * 节点任务创建完成后触发。
     *
     * @param event 节点事件，包含本次创建出的任务列表
     */
    default void afterCreateTask(FlowNodeEvent event) {
    }
}
