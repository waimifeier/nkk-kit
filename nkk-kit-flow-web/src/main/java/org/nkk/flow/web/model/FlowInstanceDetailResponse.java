package org.nkk.flow.web.model;

import lombok.Data;
import org.nkk.flow.entity.FlowExtInstance;
import org.nkk.flow.entity.FlowHisInstance;
import org.nkk.flow.entity.FlowHisTask;
import org.nkk.flow.entity.FlowInstance;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.entity.FlowTaskActor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程实例详情响应。
 */
@Data
public class FlowInstanceDetailResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 活动流程实例，流程结束后为空。
     */
    private FlowInstance instance;

    /**
     * 历史流程实例，流程发起后即存在。
     */
    private FlowHisInstance hisInstance;

    /**
     * 流程实例模型快照。
     */
    private FlowExtInstance extInstance;

    /**
     * 当前活动任务。
     */
    private List<FlowTask> tasks = new ArrayList<>();

    /**
     * 当前活动任务参与者，key 为任务 ID。
     */
    private Map<Long, List<FlowTaskActor>> taskActors = new LinkedHashMap<>();

    /**
     * 历史任务。
     */
    private List<FlowHisTask> hisTasks = new ArrayList<>();
}
