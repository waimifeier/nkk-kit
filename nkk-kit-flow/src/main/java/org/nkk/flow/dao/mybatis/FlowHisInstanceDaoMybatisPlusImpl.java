package org.nkk.flow.dao.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.nkk.flow.dao.FlowHisInstanceDao;
import org.nkk.flow.entity.FlowHisInstance;
import org.nkk.flow.mapper.FlowHisInstanceMapper;

import java.util.List;

/**
 * 历史实例 MyBatis-Plus DAO。
 */
public class FlowHisInstanceDaoMybatisPlusImpl implements FlowHisInstanceDao {

    private final FlowHisInstanceMapper mapper;

    public FlowHisInstanceDaoMybatisPlusImpl(FlowHisInstanceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean insert(FlowHisInstance instance) {
        return mapper.insert(instance) > 0;
    }

    @Override
    public boolean updateById(FlowHisInstance instance) {
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
    public FlowHisInstance selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<FlowHisInstance> selectListByCreateId(String createId) {
        return mapper.selectList(new QueryWrapper<FlowHisInstance>()
                .eq("create_id", createId)
                .orderByDesc("create_time")
                .orderByDesc("id"));
    }

    @Override
    public List<FlowHisInstance> selectListByProcessId(Long processId) {
        return mapper.selectList(new QueryWrapper<FlowHisInstance>().eq("process_id", processId));
    }

    @Override
    public List<FlowHisInstance> selectListByParentInstanceId(Long parentInstanceId) {
        return mapper.selectList(new QueryWrapper<FlowHisInstance>().eq("parent_instance_id", parentInstanceId));
    }

    @Override
    public List<FlowHisInstance> selectListByBusinessKey(String businessKey) {
        return mapper.selectList(new QueryWrapper<FlowHisInstance>().eq("business_key", businessKey));
    }
}

