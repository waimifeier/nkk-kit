package org.nkk.core.enums.common;

import one.util.streamex.StreamEx;
import org.nkk.core.beans.common.EnumResp;
import org.nkk.core.beans.exception.EnumIllegalArgumentException;
import org.nkk.core.enums.fail.EnumErrorCodeEnum;

import java.util.List;
import java.util.Objects;

/**
 * 参考文档：https://www.cnblogs.com/kelelipeng/p/13972138.html
 *
 */
/**
 * <p>描述: 枚举抽象类
 * <p>开发者: dlj
 * <p>时间 2022/6/14 3:29 下午
 */

public interface IEnum<T> {

    /**
     * 获取枚举值
     *
     * @return 枚举值
     * @since 1.1.0.211021
     */
    T value();

    /**
     * 获取枚举信息
     *
     * @return 枚举信息
     * @since 1.1.0.211021
     */
    String label();

    /**
     * <p>描述: 查找枚举key 是否存在
     * <p>开发者: dlj
     * <p>时间: 2022/6/14 6:01 下午
     *
     * @param value: 枚举的value
     * @return java.lang.Boolean
     **/
    static <T, E extends IEnum<T>> Boolean existsKey(Class<E> enumClass, T value) {
        return Objects.nonNull(resolveKeyOfNullable(enumClass, value));
    }


    /**
     * 根据key 获取指定的枚举类型
     *
     * @param enumClass 枚举类型
     * @param value     枚举value
     * @param <E>       枚举泛型
     * @return 返回枚举
     * @throws NullPointerException 不存在抛出空指针
     */
    static <T, E extends IEnum<T>> E resolveKey(Class<E> enumClass, T value) throws EnumIllegalArgumentException {
        E tIEnum = resolveKeyOfNullable(enumClass, value);

        if(Objects.isNull(tIEnum)) {
           throw new EnumIllegalArgumentException(EnumErrorCodeEnum.ENUM_CAN_NOT_MATCH, String.format("枚举值[%s]不存在", value));
        }
        return tIEnum;
    }


    /**
     * <p>描述: 根据枚举key 解析出枚举
     * <p>开发者: dlj
     * <p>时间: 2022/6/20 11:55 上午
     *
     * @param value: 枚举值
     * @return com.nk.enums.BaseEnum<Key>
     **/
    static <T, E extends IEnum<T>> E resolveKeyOfNullable(Class<E> enumClass, T value) {
        if (!enumClass.isEnum() || !IEnum.class.isAssignableFrom(enumClass)) {
            return null;
        }

        E[] enumConstants = enumClass.getEnumConstants();
        return StreamEx.of(enumConstants)
                .findFirst(it ->Objects.equals(it.value(), value))
                .orElse(null);
    }

    /**
     * 获取所有的枚举key
     *
     * @param enumClass 枚举类
     * @return 返回枚举key的集合
     */
    static <T, E extends IEnum<T>> List<T> getKeys(Class<E> enumClass) {
        E[] enumConstants = enumClass.getEnumConstants();
        return StreamEx.of(enumConstants).map(IEnum::value).toList();
    }

    /**
    *   获取返回值
    * @author nkk
    * @return {@link EnumResp}
    */
    default EnumResp getEnumResp(){
        return EnumResp.builder()
                .value(String.valueOf(value()))
                .label(label())
                .build();
    }

}
