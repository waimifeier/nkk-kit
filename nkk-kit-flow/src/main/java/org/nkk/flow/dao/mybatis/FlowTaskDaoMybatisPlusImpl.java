package org.nkk.flow.dao.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.nkk.flow.dao.FlowTaskDao;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.mapper.FlowTaskMapper;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 活动任务 MyBatis-Plus DAO。
 */
public class FlowTaskDaoMybatisPlusImpl implements FlowTaskDao {

    private final FlowTaskMapper mapper;

    public FlowTaskDaoMybatisPlusImpl(FlowTaskMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean insert(FlowTask task) {
        return mapper.insert(task) > 0;
    }

    @Override
    public boolean updateById(FlowTask task) {
        return mapper.updateById(task) > 0;
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
    public boolean deleteByInstanceIds(List<Long> instanceIds) {
        if (instanceIds == null || instanceIds.isEmpty()) {
            return true;
        }
        mapper.delete(new QueryWrapper<FlowTask>().in("instance_id", instanceIds));
        return true;
    }

    @Override
    public FlowTask selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<FlowTask> selectListByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return mapper.selectBatchIds(ids);
    }

    @Override
    public List<FlowTask> selectListByInstanceId(Long instanceId) {
        return mapper.selectList(new QueryWrapper<FlowTask>().eq("instance_id", instanceId));
    }

    @Override
    public List<FlowTask> selectListByInstanceIdAndTaskKey(Long instanceId, String taskKey) {
        return mapper.selectList(new QueryWrapper<FlowTask>().eq("instance_id", instanceId).eq("task_key", taskKey));
    }

    @Override
    public List<FlowTask> selectListByParentTaskId(Long parentTaskId) {
        return mapper.selectList(new QueryWrapper<FlowTask>().eq("parent_task_id", parentTaskId));
    }

    @Override
    public List<FlowTask> selectListByCallProcessIdAndCallInstanceId(Long callProcessId, Long callInstanceId) {
        return mapper.selectList(new QueryWrapper<FlowTask>()
                .eq("call_process_id", callProcessId)
                .eq("call_instance_id", callInstanceId));
    }

    @Override
    public List<FlowTask> selectTimeoutOrRemindTasks(Date now) {
        if (now == null) {
            return Collections.emptyList();
        }
        QueryWrapper<FlowTask> wrapper = new QueryWrapper<>();
        wrapper.le("expire_time", now).or().le("remind_time", now);
        return mapper.selectList(wrapper);
    }
}

