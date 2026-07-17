package org.nkk.core.enums.verify;

/**
 * 条件匹配类型
 */
public enum ConditionType {

    /**
     * 等于
     */
    EQUAL,

    /**
     * 不等于
     */
    NOT_EQUAL,

    /**
     * 大于
     */
    GREATER_THAN,

    /**
     * 大于等于
     */
    GREATER_THAN_OR_EQUAL,

    /**
     * 小于
     */
    LESS_THAN,

    /**
     * 小于等于
     */
    LESS_THAN_OR_EQUAL,

    /**
     * 包含
     */
    CONTAINS,

    /**
     * 以指定值开头
     */
    STARTS_WITH,

    /**
     * 以指定值结尾
     */
    ENDS_WITH,

    /**
     * 在指定集合中
     */
    IN,

    /**
     * 不在指定集合中
     */
    NOT_IN,

    /**
     * 为空
     */
    IS_NULL,

    /**
     * 不为空
     */
    NOT_NULL,

    /**
     * 空白字符串
     */
    BLANK,

    /**
     * 非空白字符串
     */
    NOT_BLANK
}
