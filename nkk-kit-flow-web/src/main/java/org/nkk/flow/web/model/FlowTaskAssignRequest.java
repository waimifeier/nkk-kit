package org.nkk.flow.web.model;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务指派请求。
 */
@Data
public class FlowTaskAssignRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 转交方式：1 转办，2 委派，3 代理。
     */
    private Integer transferType = 1;

    /**
     * 转交人员列表。
     */
    private List<FlowTaskActorParam> actors = new ArrayList<>();

    /**
     * 转交原因。
     */
    private String opinion;

    /**
     * 操作变量。
     */
    private Map<String, Object> variables = new LinkedHashMap<>();
}
