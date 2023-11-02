package org.nkk.core.enums.common;


/**
 * 系統错误码枚举
 *
 * @author Nkks
 * @class SysStatusEnum
 * @time 2022/1/26 11:12
 */
public enum SysStatusEnum {

    // --- 2xx Success ---
    /**
     * {@code 200 成功}.
     */
    OK(200,  "成功"),

    // --- 4xx Client Error ---
    /**
     * {@code 400 Bad 请求错误}.
     */
    BAD_REQUEST(400,  "请求错误"),

    /**
     * {@code 401 未授权}.
     */
    UNAUTHORIZED(401, "未授权"),

    /**
     * {@code 403 无权访问}.
     */
    FORBIDDEN(403, "无权访问"),
    /**
     * {@code 404 找不到地址}.
     */
    NOT_FOUND(404, "请求地址不存在"),


    /**
     * 请求方式不支持
     */
    NOT_SUPPORT_METHODS(405,  "请求方式不支持"),


    // --- 5xx Server Error ---
    /**
     * {@code 500 内部服务器错误}.
     */
    INTERNAL_SERVER_ERROR(500, "服务器错误"),

    /**
     * 违反sql唯一约束
     */
    SQLIntegrity(501, "违反sql唯一约束"),


    ;


    /**
     * 值
     */
    private final int value;


    /**
     * 原因短语/描述
     */
    private final String reason;

    /**
     * 构造方法
     *
     * @param value          value
     * @param failSeriesEnum series
     * @param reasonPhrase   reasonPhrase
     * @author Nkks
     * @time 2021/4/26 23:15
     */
    SysStatusEnum(int value, String reason) {
        this.value = value;
        this.reason = reason;
    }

    /**
     * 返回错误码的值
     *
     * @return {@link int}
     * @author Nkks
     * @time 2022/1/26 11:11
     */
    public int getIntValue() {
        return this.value;
    }

    /**
     * 返回错误码的值
     *
     * @return {@code int}
     * @author Nkks
     * @time 2021/4/26 23:02
     */
    public String value() {
        return String.valueOf(this.value);
    }

    /**
     * 返回错误码的描述
     *
     * @return {@link String}
     * @author Nkks
     * @time 2021/4/26 23:01
     */
    public String getReason() {
        return this.reason;
    }



}
