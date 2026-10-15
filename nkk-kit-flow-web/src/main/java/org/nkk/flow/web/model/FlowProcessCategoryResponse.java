package org.nkk.flow.web.model;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 流程分类分组响应。
 */
@Data
public class FlowProcessCategoryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 流程分类。
     */
    private String processType;

    /**
     * 流程分类中文名称，由 FlowDesignerCategoryProvider 的分类树匹配得到；未匹配到时为空。
     */
    private String processTypeName;

    /**
     * 流程数量。
     */
    private Integer processCount = 0;

    /**
     * 当前分类下的流程定义列表。
     */
    private List<FlowProcessVO> processes = new ArrayList<>();

    public static FlowProcessCategoryResponse of(String processType, String processTypeName, List<FlowProcessVO> processes) {
        FlowProcessCategoryResponse response = new FlowProcessCategoryResponse();
        response.setProcessType(processType);
        response.setProcessTypeName(processTypeName);
        response.setProcesses(processes == null ? new ArrayList<>() : processes);
        response.setProcessCount(response.getProcesses().size());
        return response;
    }
}
