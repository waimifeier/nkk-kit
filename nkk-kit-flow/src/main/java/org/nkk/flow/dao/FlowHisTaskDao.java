package org.nkk.flow.dao;

import org.nkk.flow.entity.FlowHisTask;

import java.util.List;

/**
 * 历史任务 DAO。
 */
public interface FlowHisTaskDao {

    boolean insert(FlowHisTask task);

    boolean updateById(FlowHisTask task);

    boolean deleteById(Long id);

    boolean deleteByInstanceIds(List<Long> instanceIds);

    FlowHisTask selectById(Long id);

    List<FlowHisTask> selectListByIds(List<Long> ids);

    List<FlowHisTask> selectListByCreateId(String createId);

    List<FlowHisTask> selectListByInstanceId(Long instanceId);

    List<FlowHisTask> selectListByTaskKey(Long instanceId, String taskKey);
}

