package org.nkk.flow.dao.mybatis;

import org.nkk.flow.dao.FlowExtInstanceDao;
import org.nkk.flow.entity.FlowExtInstance;
import org.nkk.flow.mapper.FlowExtInstanceMapper;

/**
 * 实例扩展 MyBatis-Plus DAO。
 */
public class FlowExtInstanceDaoMybatisPlusImpl implements FlowExtInstanceDao {

    private final FlowExtInstanceMapper mapper;

    public FlowExtInstanceDaoMybatisPlusImpl(FlowExtInstanceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean insert(FlowExtInstance instance) {
        return mapper.insert(instance) > 0;
    }

    @Override
    public boolean updateById(FlowExtInstance instance) {
        return mapper.updateById(instance) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return mapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteByIds(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        mapper.deleteBatchIds(ids);
        return true;
    }

    @Override
    public FlowExtInstance selectById(Long id) {
        return mapper.selectById(id);
    }
}

