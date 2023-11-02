package org.nkk.core.beans.common;



import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nkk.core.enums.common.SysStatusEnum;

import java.io.Serializable;

/**
*   通用返回结果包装类
* @author Nkks
* @class Result
* @time :2022/1/27 9:41
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 	错误码
     */
    private String code;

    /**
     * 	描述
     */
    private String msg;

    /**
     * 	数据
     */
    private T data;

    /**
     *	返回成功
     * @author Nkks
     * @time :2021/4/26 23:22
     * @param msg 错误描述,如果为空则返回{@link SysStatusEnum#getReason()}
     * @return {@link Result<T>}
     */
    public static <T> Result<T> ok(String msg) {
        return Result.<T>builder()
                .code(SysStatusEnum.OK.value())
                .data(null)
                .msg(StrUtil.isBlank(msg) ? SysStatusEnum.OK.getReason() : msg)
                .build();
    }

    /**
     *	返回服务器错误
     * @author Nkks
     * @time :2021/4/26 23:22
     * @param msg 错误描述,如果为空则返回{@link SysStatusEnum#getReason()}
     * @return {@link Result<T>}
     */
    public static <T> Result<T> serverError(String msg) {
        return Result.<T>builder()
                .code(SysStatusEnum.INTERNAL_SERVER_ERROR.value())
                .data(null)
                .msg(StrUtil.isBlank(msg) ? SysStatusEnum.INTERNAL_SERVER_ERROR.getReason() : msg)
                .build();
    }

    /**
     *	返回失败
     * @author Nkks
     * @time :2021/4/26 23:22
     * @param baseEnum 错误码 {@link SysStatusEnum}枚举常量
     * @param msg 错误描述,如果为空则返回{@link SysStatusEnum#getReason()} ()}
     * @return {@link Result<T>}
     */
    public static <T> Result<T> fail(SysStatusEnum baseEnum, String msg) {
        return Result.<T>builder()
                .code(baseEnum.value())
                .data(null)
                .msg(StrUtil.isBlank(msg) ? baseEnum.getReason() : msg)
                .build();
    }

    /**
     *	返回失败
     * @author Nkks
     * @time :2021/4/26 23:22
     * @param baseEnum 错误码 {@link SysStatusEnum}枚举常量
     * @return {@link Result<T>}
     */
    public static <T> Result<T> fail(SysStatusEnum baseEnum) {
        return fail(baseEnum,null);
    }

    /**
     *	返回失败
     * @author Nkks
     * @time 2021/4/26 23:22
     * @return {@link Result<T>}
     */
    public static <T> Result<T> fail(String message) {
        return fail(SysStatusEnum.INTERNAL_SERVER_ERROR,message);
    }


    /**
     *	返回指定data的Result
     * @author Nkks
     * @time 2021/4/26 23:28
     * @param data {@code <T>}类型的data
     * @return {@link Result<T>}
     */
    public static <T> Result<T> ok(T data) {
        return ok(data,null);
    }

    /**
     *	返回成功
     * @author Nkks
     * @time :2021/4/26 23:22
     * @param msg 错误描述,如果为空则返回{@link SysStatusEnum#getReason()} ()}
     * @param data msg
     * @return {@link Result<T>}
     */
    public static <T> Result<T> ok(T data,String msg) {
        return Result.<T>builder()
                .code(SysStatusEnum.OK.value())
                .data(data)
                .msg(StrUtil.isBlank(msg) ? SysStatusEnum.OK.getReason() : msg)
                .build();
    }

    /**
     *	返回指定错误码Result
     * @author Nkks
     * @time :2021/4/26 23:24
     * @param baseEnum 错误码 {@link SysStatusEnum}枚举常量
     * @return {@link Result<T>}
     */
    public static <T> Result<T> result(SysStatusEnum baseEnum) {
        return result(baseEnum,null,null);
    }

    /**
     *	返回自定义的Result
     * @author Nkks
     * @time :2021/4/26 23:34
     * @param baseEnum 错误码 {@link SysStatusEnum}枚举常量
     * @param msg 错误描述,如果为空则返回{@link SysStatusEnum#getReason()} ()}
     * @param data {@code <T>}类型的data
     * @return {@link Result<T>}
     */
    public static <T> Result<T> result(SysStatusEnum baseEnum, String msg, T data) {
        return Result.<T>builder()
                .code(baseEnum.value())
                .data(data)
                .msg(StrUtil.isBlank(msg) ? baseEnum.getReason() : msg)
                .build();
    }

    /**
     *	返回自定义的Result
     * @author Nkks
     * @time :2021/4/26 23:34
     * @param baseEnum 错误码 {@link SysStatusEnum}枚举常量
     * @param data {@code <T>}类型的data
     * @return {@link Result<T>}
     */
    public static <T> Result<T> result(SysStatusEnum baseEnum, T data) {
        return result(baseEnum,null,data);
    }

    /**
     *	返回自定义的Result
     * @author Nkks
     * @time :2021/4/26 23:34
     * @param baseEnum 错误码 {@link SysStatusEnum}枚举常量
     * @return {@link Result<T>}
     */
    public static <T> Result<T> result(SysStatusEnum baseEnum, String msg) {
        return result(baseEnum,msg,null);
    }


}
