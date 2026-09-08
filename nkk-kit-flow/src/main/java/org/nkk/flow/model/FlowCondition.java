package org.nkk.flow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

/**
 * 条件表达式项。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlowCondition implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 条件字段名，对应流程变量 Map 中的 key。
     */
    private String field;

    /**
     * 条件操作符，默认表达式支持 eq、ne、gt、ge、lt、le 以及 =、!=、>、>=、<、<=。
     */
    private String operator;

    /**
     * 条件期望值。
     */
    private Object value;
}

