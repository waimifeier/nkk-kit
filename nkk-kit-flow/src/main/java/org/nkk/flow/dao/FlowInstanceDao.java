package org.nkk.flow.dao;

import org.nkk.flow.entity.FlowInstance;

import java.util.List;

/**
 * 活动实例 DAO。
 */
public interface FlowInstanceDao {

    boolean insert(FlowInstance instance);

    boolean updateById(FlowInstance instance);

    boolean deleteById(Long id);

    boolean deleteByIds(List<Long> ids);

    FlowInstance selectById(Long id);

    List<FlowInstance> selectListByParentInstanceId(Long parentInstanceId);

    List<FlowInstance> selectListByBusinessKey(String businessKey);
}

