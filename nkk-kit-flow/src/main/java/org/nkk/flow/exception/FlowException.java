package org.nkk.flow.exception;

/**
 * 流程引擎统一业务异常。
 *
 * <p>审批流转、任务办理、流程定义管理、引擎配置缺失及触发条件求值等
 * 流程相关的业务错误均抛出本异常，替代原先使用的 {@link IllegalStateException}，
 * 便于宿主全局异常处理器统一捕获并向调用方透出具体错误信息。</p>
 *
 * @author Nkks
 */
public class FlowException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造流程业务异常。
     *
     * @param detailedMessage 错误信息
     */
    public FlowException(String detailedMessage) {
        super(detailedMessage);
    }

    /**
     * 构造带导火索的流程业务异常。
     *
     * @param detailedMessage 错误信息
     * @param cause           原始异常
     */
    public FlowException(String detailedMessage, Throwable cause) {
        super(detailedMessage, cause);
    }
}
