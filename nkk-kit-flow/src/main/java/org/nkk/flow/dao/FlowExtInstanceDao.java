package org.nkk.flow.dao;

import org.nkk.flow.entity.FlowExtInstance;

/**
 * 实例模型扩展 DAO。
 */
public interface FlowExtInstanceDao {

    boolean insert(FlowExtInstance instance);

    boolean updateById(FlowExtInstance instance);

    boolean deleteById(Long id);

    boolean deleteByIds(java.util.List<Long> ids);

    FlowExtInstance selectById(Long id);
}

