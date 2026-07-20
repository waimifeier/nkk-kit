package org.nkk.core.beans.common;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class BaseQuery {

    @NotNull(message = "页码不能为空！")
    @Min(value = 1, message = "页码最小值为1！")
    private Integer current;

    @NotNull(message = "条目不能为空！")
    @Min(value = 1, message = "条目最小值为1！")
    private Integer size;
}
