package org.nkk.core.beans.exception;

import lombok.Getter;
import org.nkk.core.enums.fail.EnumErrorCodeEnum;

/**
*  枚举参数异常
* @author Nkks
* @class EnumIllegalArgumentException
* @time 2022/1/26 11:20
*/
@Getter
public class EnumIllegalArgumentException extends IllegalArgumentException{

    private static final long serialVersionUID = -6738594844611626333L;

    private final String code;

    private final EnumErrorCodeEnum enumErrorCodeEnum;

    public EnumIllegalArgumentException(EnumErrorCodeEnum enumErrorCodeEnum, String reasonPhrase) {
        super(reasonPhrase);
        this.enumErrorCodeEnum = enumErrorCodeEnum;
        this.code = enumErrorCodeEnum.value();
    }

    public EnumIllegalArgumentException(EnumErrorCodeEnum enumErrorCodeEnum) {
        super(enumErrorCodeEnum.label());
        this.enumErrorCodeEnum = enumErrorCodeEnum;
        this.code =enumErrorCodeEnum.label();
    }



    /**
     *   处理枚举类型失败异常
     * @author :  Nkks
     * @time :2022/1/26 11:21
     * @return {@link EnumIllegalArgumentException}
     */
    public static EnumIllegalArgumentException notAnEnumError(EnumErrorCodeEnum enumErrorCodeEnum){
        return new EnumIllegalArgumentException(enumErrorCodeEnum);
    }

    /**
     *   处理枚举类型失败异常
     * @author Nkks
     * @time 2022/1/26 11:21
     * @return {@link EnumIllegalArgumentException}
     */
    public static EnumIllegalArgumentException notAnEnumError(EnumErrorCodeEnum enumErrorCodeEnum,String reasonPhrase){
        return new EnumIllegalArgumentException(enumErrorCodeEnum,reasonPhrase);
    }

    /**
    *   处理枚举类型失败异常
    * @author Nkks
    * @time 2022/1/26 11:21
    * @return {@link EnumIllegalArgumentException}
    */
    public static EnumIllegalArgumentException resolveEnumFail(EnumErrorCodeEnum enumErrorCodeEnum){
        return new EnumIllegalArgumentException(enumErrorCodeEnum);
    }

    /**
     *   处理枚举类型失败异常
     * @author Nkks
     * @time 2022/1/26 11:21
     * @return {@link EnumIllegalArgumentException}
     */
    public static EnumIllegalArgumentException resolveEnumFail(EnumErrorCodeEnum enumErrorCodeEnum,String reasonPhrase){
        return new EnumIllegalArgumentException(enumErrorCodeEnum,reasonPhrase);
    }
}
