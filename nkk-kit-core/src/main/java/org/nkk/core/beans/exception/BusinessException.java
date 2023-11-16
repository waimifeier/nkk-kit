package org.nkk.core.beans.exception;


import org.nkk.core.enums.common.BaseEnum;
import org.nkk.core.enums.common.SysStatusEnum;

/**
*   应用全局异常
* @author Nkks
* @class AppException
* @time :2022/1/27 15:46
*/
public class BusinessException extends RuntimeException{

    private static final long serialVersionUID = -8626647865331982731L;

    /** 错误码 */
    protected final BaseEnum errorCode;

    public BaseEnum getErrorCode() {
        return errorCode;
    }

    public String getCode() {
        return errorCode.value();
    }

    /**
     *   无参默认构造UNSPECIFIED(未指明异常)，默认返回500
     * @author Nkks
     * @time 2021/11/18 10:04
     */
    public BusinessException(){
        super(SysStatusEnum.INTERNAL_SERVER_ERROR.getReason());
        this.errorCode = SysStatusEnum.INTERNAL_SERVER_ERROR;
    }

    /**
     * 指定错误码构造通用异常
     * @author Nkks
     * @time 2021/11/18 10:05
     * @param errorCode 错误码
     */
    public BusinessException(final BaseEnum errorCode) {
        super(errorCode.label());
        this.errorCode = errorCode;
    }

    /**
     * 指定详细描述构造通用异常
     * @author Nkks
     * @time 2021/11/18 10:07
     * @param detailedMessage 错误信息
     */
    public BusinessException(final String detailedMessage) {
        super(detailedMessage);
        this.errorCode = SysStatusEnum.INTERNAL_SERVER_ERROR;
    }

    /**
     *   指定导火索构造通用异常
     * @author Nkks
     * @time 2021/11/18 10:08
     * @param t 导火索
     */
    public BusinessException(final Throwable t) {
        super(t);
        this.errorCode = SysStatusEnum.INTERNAL_SERVER_ERROR;
    }

    /**
     *   构造指定错误码和错误消息的异常
     * @author Nkks
     * @time 2021/11/18 10:14
     * @param errorCode 错误码
     * @param detailedMessage 错误消息
     */
    public BusinessException(final BaseEnum errorCode, final String detailedMessage) {
        super(detailedMessage);
        this.errorCode = errorCode;
    }

    /**
     *   构造指定错误码和导火索的异常
     * @author Nkks
     * @time 2021/11/18 10:15
     * @param errorCode 错误码
     * @param t 导火索
     */
    public BusinessException(final BaseEnum errorCode, final Throwable t) {
        super(errorCode.label(), t);
        this.errorCode = errorCode;
    }

    /**
     *   构造指定错误消息和导火索的异常
     * @author Nkks
     * @time 2021/11/18 10:15
     * @param detailedMessage 错误消息
     * @param t 导火索
     */
    public BusinessException(final String detailedMessage, final Throwable t) {
        super(detailedMessage, t);
        this.errorCode = SysStatusEnum.INTERNAL_SERVER_ERROR;
    }

    /**
     *   构造指定错误消息和导火索和错误码的异常
     * @author Nkks
     * @time 2021/11/18 10:16
     * @param errorCode errorCode
     * @param detailedMessage detailedMessage
     * @param t t
     */
    public BusinessException(final BaseEnum errorCode, final String detailedMessage, final Throwable t) {
        super(detailedMessage, t);
        this.errorCode = errorCode;
    }

    /**
     *   返回一个通用的异常，默认返回500
     * @author Nkks
     * @time 2021/11/18 10:04
     * @return {@link BusinessException}
     */
    public static BusinessException fail() {
        return new BusinessException();
    }

    /**
     *  返回一个指定错误码构造通用异常
     * @author Nkks
     * @time 2021/11/18 10:06
     * @param errorCode 错误码
     * @return {@link BusinessException}
     */
    public static BusinessException fail(final BaseEnum errorCode) {
        return new BusinessException(errorCode);
    }

    /**
     *   返回一个指定指定导火索构造通用异常
     * @author Nkks
     * @time 2021/11/18 10:09
     * @param detailedMessage 详细描述
     * @return {@link BusinessException}
     */
    public static BusinessException fail(final String detailedMessage) {
        return new BusinessException(detailedMessage);
    }

    /**
     *  返回一个指定详细描述构造通用异常
     * @author Nkks
     * @time 2021/11/18 10:06
     * @param t 导火索
     * @return {@link BusinessException}
     */
    public static BusinessException fail(final Throwable t) {
        return new BusinessException(t);
    }

    /**
     *  返回一个指定错误码和详细描述构造通用异常
     * @author Nkks
     * @time 2021/11/18 10:18
     * @param errorCode 错误码
     * @param detailedMessage 详细描述
     * @return {@link BusinessException}
     */
    public static BusinessException fail(final BaseEnum errorCode, final String detailedMessage) {
        return new BusinessException(errorCode,detailedMessage);
    }
    

}
