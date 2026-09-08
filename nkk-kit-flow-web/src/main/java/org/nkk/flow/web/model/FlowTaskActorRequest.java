package org.nkk.flow.web.model;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务参与人请求基类。
 */
@Data
public class FlowTaskActorRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务参与人列表。
     */
    private List<FlowTaskActorParam> actors = new ArrayList<>();

    /**
     * 操作变量。
     */
    private Map<String, Object> variables = new LinkedHashMap<>();
}
