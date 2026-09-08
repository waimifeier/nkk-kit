package org.nkk.flow.service;

import org.nkk.flow.entity.FlowHisInstance;
import org.nkk.flow.entity.FlowExtInstance;
import org.nkk.flow.entity.FlowHisTask;
import org.nkk.flow.entity.FlowHisTaskActor;
import org.nkk.flow.entity.FlowInstance;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.entity.FlowTaskActor;

import java.util.Date;
import java.util.List;

/**
 * 流程查询服务。
 */
public interface FlowQueryService {

    FlowInstance getInstance(Long instanceId);

    FlowHisInstance getHisInstance(Long instanceId);

    FlowExtInstance getExtInstance(Long instanceId);

    boolean existActiveSubProcess(Long instanceId);

    boolean existActiveTask(Long instanceId);

    FlowTask getTask(Long taskId);

    FlowHisTask getHisTask(Long taskId);

    List<FlowTask> getTasksByInstanceId(Long instanceId);

    List<FlowTask> getTasksByInstanceIdAndTaskKey(Long instanceId, String taskKey);

    List<FlowTaskActor> getTaskActorsByTaskId(Long taskId);

    List<FlowTaskActor> getTaskActorsByTaskIdAndActorId(Long taskId, String actorId);

    List<FlowTaskActor> getActiveTaskActorsByInstanceId(Long instanceId);

    List<FlowHisTask> getHisTasksByInstanceId(Long instanceId);

    List<FlowHisTaskActor> getHisTaskActorsByTaskId(Long taskId);

    List<FlowHisTaskActor> getHisTaskActorsByInstanceId(Long instanceId);

    List<FlowInstance> getInstancesByBusinessKey(String businessKey);

    List<FlowHisInstance> getHisInstancesByBusinessKey(String businessKey);

    List<FlowInstance> getSubProcessByInstanceId(Long instanceId);

    List<FlowTask> getTimeoutOrRemindTasks(Date now);
}

