package org.nkk.flow.dao.mybatis;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.nkk.flow.dao.FlowHisInstanceDao;
import org.nkk.flow.entity.FlowHisInstance;
import org.nkk.flow.mapper.FlowHisInstanceMapper;

import java.util.Date;
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

    @Override
    public IPage<FlowHisInstance> selectPage(IPage<FlowHisInstance> page, Long processId, String starterName,
                                              Date startTime, Date endTime, Integer instanceState, Long instanceId) {
        QueryWrapper<FlowHisInstance> wrapper = new QueryWrapper<>();
        wrapper.eq("process_id", processId);
        wrapper.like(StrUtil.isNotBlank(starterName), "create_by", StrUtil.trim(starterName));
        wrapper.ge(startTime != null, "create_time", startTime);
        wrapper.le(endTime != null, "create_time", endTime);
        wrapper.eq(instanceState != null, "instance_state", instanceState);
        wrapper.eq(instanceId != null, "id", instanceId);
        // 默认按提交时间倒序，最新实例在前
        wrapper.orderByDesc("create_time");
        wrapper.orderByDesc("id");
        return mapper.selectPage(page, wrapper);
    }
}

