package org.nkk.enums.base;

import org.nkk.beans.common.EnumResp;
import org.nkk.beans.exception.EnumIllegalArgumentException;
import org.nkk.enums.fail.EnumErrorCodeEnum;

import java.util.Objects;


/**
*   公共枚举
*
* @author nkk
* @class BaseEnum
* @time 2022/1/27 9:50
*/

public interface BaseEnum {

    /**
     * 获取枚举值
     *
     * @return 枚举值
     * @since 1.1.0.211021
     */

    String value();

    /**
     * 获取枚举信息
     *
     * @return 枚举信息
     * @since 1.1.0.211021
     */
    String getReasonPhrase();

    /**
     * 解析枚举参数值
     *
     * @param value     参数值
     * @param enumClass 需要解析的枚举类
     * @param <E>       继承 {@link BaseEnum} 的枚举类型
     * @return 解析后的枚举对象
     * @throws EnumIllegalArgumentException 解析异常
     * @since 1.1.0.211021
     */
    static <E extends BaseEnum> E resolve(String value, Class<E> enumClass) throws EnumIllegalArgumentException {
        E e = resolveNullAble(value, enumClass);
        if (e == null) {
            throw EnumIllegalArgumentException.resolveEnumFail(EnumErrorCodeEnum.ENUM_CAN_NOT_MATCH,"[" + value + "]无法匹配为枚举类型");
        }
        return e;
    }

    /**
     * 解析枚举参数值
     *
     * @param value     参数值
     * @param enumClass 需要解析的枚举类
     * @param <E>       继承 {@link BaseEnum} 的枚举类型
     * @return 解析后的枚举对象
     * @throws EnumIllegalArgumentException 解析异常
     * @since 1.1.0.211021
     */
    static <E extends BaseEnum> E resolveNullAble(String value, Class<E> enumClass) throws EnumIllegalArgumentException {
        if (!enumClass.isEnum()) {
            throw EnumIllegalArgumentException.notAnEnumError(EnumErrorCodeEnum.NOT_AN_ENUM);
        }
        E enumValue = null;
        E[] enumConstants = enumClass.getEnumConstants();
        for (E enumConstant : enumConstants) {
            if (Objects.equals(enumConstant.value(), value)) {
                enumValue = enumConstant;
                break;
            }
        }
        return enumValue;
    }

    /**
    *   获取返回值
    * @author nkk
    * @time 2022/3/7 10:26
    * @return {@link EnumResp}
    */
    default EnumResp getEnumResp(){
        return EnumResp.builder()
                .value(value())
                .text(getReasonPhrase())
                .build();
    }
}
