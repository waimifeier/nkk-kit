package org.nkk.flow.dao;

import org.nkk.flow.entity.FlowTaskActor;

import java.util.List;

/**
 * 活动任务参与者 DAO。
 */
public interface FlowTaskActorDao {

    boolean insert(FlowTaskActor actor);

    boolean updateById(FlowTaskActor actor);

    boolean deleteById(Long id);

    boolean deleteByTaskId(Long taskId);

    boolean deleteByTaskIds(List<Long> taskIds);

    boolean deleteByInstanceIds(List<Long> instanceIds);

    List<FlowTaskActor> selectListByTaskId(Long taskId);

    List<FlowTaskActor> selectListByTaskIds(List<Long> taskIds);

    List<FlowTaskActor> selectList();

    List<FlowTaskActor> selectListByInstanceId(Long instanceId);

    List<FlowTaskActor> selectListByTaskIdAndActorId(Long taskId, String actorId);
}

