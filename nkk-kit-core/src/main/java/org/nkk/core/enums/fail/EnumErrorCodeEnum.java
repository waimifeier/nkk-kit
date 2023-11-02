package org.nkk.core.enums.fail;


import org.nkk.core.enums.common.BaseEnum;

/**
*   枚举错误码
* @author Nkks
* @class EnumErrorCodeEnum
* @time 2022/1/26 11:13
*/
public enum EnumErrorCodeEnum implements BaseEnum {

    ENUM_CAN_NOT_MATCH("E100","枚举类型无法匹配"),
    NOT_AN_ENUM("E200","不是枚举类型")
    ;

    private final String value;

    private final String reason;

    EnumErrorCodeEnum(String value, String reason) {
        this.value = value;
        this.reason = reason;
    }

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public String getText() {
        return this.reason;
    }
}
