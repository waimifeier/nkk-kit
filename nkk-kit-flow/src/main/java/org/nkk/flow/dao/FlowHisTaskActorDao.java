package org.nkk.flow.dao;

import org.nkk.flow.entity.FlowHisTaskActor;

import java.util.List;

/**
 * 历史任务参与者 DAO。
 */
public interface FlowHisTaskActorDao {

    boolean insert(FlowHisTaskActor actor);

    boolean deleteByTaskId(Long taskId);

    boolean deleteByInstanceIds(List<Long> instanceIds);

    List<FlowHisTaskActor> selectList();

    List<FlowHisTaskActor> selectListByTaskId(Long taskId);

    List<FlowHisTaskActor> selectListByInstanceId(Long instanceId);

    List<FlowHisTaskActor> selectListByTaskIdAndActorId(Long taskId, String actorId);
}

