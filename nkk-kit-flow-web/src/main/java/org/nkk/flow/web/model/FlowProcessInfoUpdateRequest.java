package org.nkk.flow.web.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 流程基础信息修改请求。
 */
@Data
public class FlowProcessInfoUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 流程名称。
     */
    private String processName;

    /**
     * 流程图标标识。
     */
    private String processIcon;

    /**
     * 流程类型。
     */
    private String processType;

    /**
     * 流程描述。
     */
    private String remark;
}
