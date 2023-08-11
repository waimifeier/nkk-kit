package org.nkk.beans.exception;

import lombok.Getter;
import org.nkk.enums.fail.EnumErrorCodeEnum;

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

    private EnumIllegalArgumentException(EnumErrorCodeEnum enumErrorCodeEnum, String reasonPhrase) {
        super(reasonPhrase);
        this.enumErrorCodeEnum = enumErrorCodeEnum;
        this.code =enumErrorCodeEnum.value();
    }

    private EnumIllegalArgumentException(EnumErrorCodeEnum enumErrorCodeEnum) {
        super(enumErrorCodeEnum.getReasonPhrase());
        this.enumErrorCodeEnum = enumErrorCodeEnum;
        this.code =enumErrorCodeEnum.value();
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
