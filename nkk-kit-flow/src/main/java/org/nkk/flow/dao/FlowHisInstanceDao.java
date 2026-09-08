package org.nkk.flow.dao;

import org.nkk.flow.entity.FlowHisInstance;

import java.util.List;

/**
 * 历史实例 DAO。
 */
public interface FlowHisInstanceDao {

    boolean insert(FlowHisInstance instance);

    boolean updateById(FlowHisInstance instance);

    boolean deleteById(Long id);

    boolean deleteByIds(List<Long> ids);

    FlowHisInstance selectById(Long id);

    List<FlowHisInstance> selectListByCreateId(String createId);

    List<FlowHisInstance> selectListByProcessId(Long processId);

    List<FlowHisInstance> selectListByParentInstanceId(Long parentInstanceId);

    List<FlowHisInstance> selectListByBusinessKey(String businessKey);
}

