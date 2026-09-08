package org.nkk.flow.dao;

import org.nkk.flow.entity.FlowTask;

import java.util.Date;
import java.util.List;

/**
 * 活动任务 DAO。
 */
public interface FlowTaskDao {

    boolean insert(FlowTask task);

    boolean updateById(FlowTask task);

    boolean deleteById(Long id);

    boolean deleteByIds(List<Long> ids);

    boolean deleteByInstanceIds(List<Long> instanceIds);

    FlowTask selectById(Long id);

    List<FlowTask> selectListByIds(List<Long> ids);

    List<FlowTask> selectListByInstanceId(Long instanceId);

    List<FlowTask> selectListByInstanceIdAndTaskKey(Long instanceId, String taskKey);

    List<FlowTask> selectListByParentTaskId(Long parentTaskId);

    List<FlowTask> selectListByCallProcessIdAndCallInstanceId(Long callProcessId, Long callInstanceId);

    List<FlowTask> selectTimeoutOrRemindTasks(Date now);
}

