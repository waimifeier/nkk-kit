package org.nkk.core.enums.common;

import com.alibaba.fastjson2.annotation.JSONCreator;
import one.util.streamex.StreamEx;
import org.nkk.core.beans.common.EnumResp;

import java.util.List;
import java.util.Objects;

/**
 * 参考文档：https://www.cnblogs.com/kelelipeng/p/13972138.html
 *
 * https://blog.csdn.net/XY1790026787/article/details/107768555
 */

/**
 * <p>描述: 枚举抽象类
 * <p>开发者: dlj
 * <p>时间 2022/6/14 3:29 下午
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
    String label();


    /**
     * <p>描述: 查找枚举key 是否存在
     * <p>开发者: dlj
     * <p>时间: 2022/6/14 6:01 下午
     *
     * @param value: 枚举的value
     * @return java.lang.Boolean
     **/
    static <E extends BaseEnum> Boolean existsKey(Class<E> enumClass, String value) {
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
    static <E extends BaseEnum> BaseEnum resolveKey(Class<E> enumClass, String value) throws NullPointerException {
        BaseEnum tBaseEnum = resolveKeyOfNullable(enumClass, value);
        Objects.requireNonNull(tBaseEnum, String.format("枚举值[%s]不存在", value));
        return tBaseEnum;
    }


    /**
     * <p>描述: 根据枚举key 解析出枚举
     * <p>开发者: dlj
     * <p>时间: 2022/6/20 11:55 上午
     *
     * @param value: 枚举值
     * @return com.nk.enums.BaseEnum<Key>
     **/
    static <E extends BaseEnum> BaseEnum resolveKeyOfNullable(Class<E> enumClass, String value) {
        if (!enumClass.isEnum() || !BaseEnum.class.isAssignableFrom(enumClass)) {
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
    static <E extends BaseEnum> List<String> getKeys(Class<E> enumClass) {
        E[] enumConstants = enumClass.getEnumConstants();
        return StreamEx.of(enumConstants).map(BaseEnum::value).toList();
    }

    /**
    *   获取返回值
    * @author nkk
    * @return {@link EnumResp}
    */
    default EnumResp getEnumResp(){
        return EnumResp.builder()
                .value(value())
                .label(label())
                .build();
    }

}
