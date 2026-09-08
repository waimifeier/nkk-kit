package org.nkk.flow.dao.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.nkk.flow.dao.FlowInstanceDao;
import org.nkk.flow.entity.FlowInstance;
import org.nkk.flow.mapper.FlowInstanceMapper;

import java.util.List;

/**
 * 活动实例 MyBatis-Plus DAO。
 */
public class FlowInstanceDaoMybatisPlusImpl implements FlowInstanceDao {

    private final FlowInstanceMapper mapper;

    public FlowInstanceDaoMybatisPlusImpl(FlowInstanceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean insert(FlowInstance instance) {
        return mapper.insert(instance) > 0;
    }

    @Override
    public boolean updateById(FlowInstance instance) {
        return mapper.updateById(instance) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return mapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        mapper.deleteBatchIds(ids);
        return true;
    }

    @Override
    public FlowInstance selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<FlowInstance> selectListByParentInstanceId(Long parentInstanceId) {
        return mapper.selectList(new QueryWrapper<FlowInstance>().eq("parent_instance_id", parentInstanceId));
    }

    @Override
    public List<FlowInstance> selectListByBusinessKey(String businessKey) {
        return mapper.selectList(new QueryWrapper<FlowInstance>().eq("business_key", businessKey));
    }
}

