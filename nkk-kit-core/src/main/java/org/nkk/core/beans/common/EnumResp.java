package org.nkk.core.beans.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
*   枚举返回实体
* @author Nkks
* @class EnumResp
* @time 2022/3/4 12:14
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnumResp {

    /**
     *  值
     */
    private String value;

    /**
     *  描述
     */
    private String text;

}
