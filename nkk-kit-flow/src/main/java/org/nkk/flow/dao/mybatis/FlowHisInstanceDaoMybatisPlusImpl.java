package org.nkk.flow.dao.mybatis;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.nkk.flow.dao.FlowHisInstanceDao;
import org.nkk.flow.entity.FlowHisInstance;
import org.nkk.flow.mapper.FlowHisInstanceMapper;

import java.util.Collection;
import java.util.Collections;
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

    @Override
    public IPage<FlowHisInstance> selectPageByCreateId(IPage<FlowHisInstance> page, String createId,
                                                        Integer instanceState, Date startTime, Date endTime) {
        QueryWrapper<FlowHisInstance> wrapper = new QueryWrapper<>();
        wrapper.eq("create_id", createId);
        wrapper.eq(instanceState != null, "instance_state", instanceState);
        wrapper.ge(startTime != null, "create_time", startTime);
        wrapper.le(endTime != null, "create_time", endTime);
        wrapper.orderByDesc("create_time");
        wrapper.orderByDesc("id");
        return mapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<FlowHisInstance> selectPageByInstanceIds(IPage<FlowHisInstance> page,
                                                           Collection<Long> instanceIds, Long instanceId,
                                                           String starterName, Date startTime, Date endTime,
                                                           Collection<Long> processIds) {
        if (CollUtil.isEmpty(instanceIds)) {
            return page;
        }
        QueryWrapper<FlowHisInstance> wrapper = new QueryWrapper<>();
        wrapper.in("id", instanceIds);
        wrapper.eq(instanceId != null, "id", instanceId);
        wrapper.like(StrUtil.isNotBlank(starterName), "create_by", StrUtil.trim(starterName));
        wrapper.ge(startTime != null, "create_time", startTime);
        wrapper.le(endTime != null, "create_time", endTime);
        if (CollUtil.isNotEmpty(processIds)) {
            wrapper.in("process_id", processIds);
        }
        wrapper.orderByDesc("create_time");
        wrapper.orderByDesc("id");
        return mapper.selectPage(page, wrapper);
    }

    @Override
    public List<Long> selectIdsByCreateByLikeAndProcessIds(String createByLike, Collection<Long> processIds) {
        QueryWrapper<FlowHisInstance> wrapper = new QueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(createByLike), "create_by", StrUtil.trim(createByLike));
        if (CollUtil.isNotEmpty(processIds)) {
            wrapper.in("process_id", processIds);
        }
        wrapper.select("id");
        List<FlowHisInstance> list = mapper.selectList(wrapper);
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<Long> ids = new java.util.ArrayList<>(list.size());
        for (FlowHisInstance instance : list) {
            if (instance.getId() != null) {
                ids.add(instance.getId());
            }
        }
        return ids;
    }
}

