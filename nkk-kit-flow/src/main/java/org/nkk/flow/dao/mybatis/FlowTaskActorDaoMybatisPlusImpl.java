package org.nkk.flow.dao.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.nkk.flow.dao.FlowTaskActorDao;
import org.nkk.flow.entity.FlowTaskActor;
import org.nkk.flow.mapper.FlowTaskActorMapper;

import java.util.Collections;
import java.util.List;

/**
 * 活动任务参与者 MyBatis-Plus DAO。
 */
public class FlowTaskActorDaoMybatisPlusImpl implements FlowTaskActorDao {

    private final FlowTaskActorMapper mapper;

    public FlowTaskActorDaoMybatisPlusImpl(FlowTaskActorMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean insert(FlowTaskActor actor) {
        return mapper.insert(actor) > 0;
    }

    @Override
    public boolean updateById(FlowTaskActor actor) {
        return mapper.updateById(actor) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return mapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteByTaskId(Long taskId) {
        mapper.delete(new QueryWrapper<FlowTaskActor>().eq("task_id", taskId));
        return true;
    }

    @Override
    public boolean deleteByTaskIds(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return true;
        }
        mapper.delete(new QueryWrapper<FlowTaskActor>().in("task_id", taskIds));
        return true;
    }

    @Override
    public boolean deleteByInstanceIds(List<Long> instanceIds) {
        if (instanceIds == null || instanceIds.isEmpty()) {
            return true;
        }
        mapper.delete(new QueryWrapper<FlowTaskActor>().in("instance_id", instanceIds));
        return true;
    }

    @Override
    public List<FlowTaskActor> selectListByTaskId(Long taskId) {
        return mapper.selectList(new QueryWrapper<FlowTaskActor>().eq("task_id", taskId));
    }

    @Override
    public List<FlowTaskActor> selectListByTaskIds(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return Collections.emptyList();
        }
        return mapper.selectList(new QueryWrapper<FlowTaskActor>().in("task_id", taskIds));
    }

    @Override
    public List<FlowTaskActor> selectList() {
        return mapper.selectList(new QueryWrapper<FlowTaskActor>());
    }

    @Override
    public List<FlowTaskActor> selectListByInstanceId(Long instanceId) {
        return mapper.selectList(new QueryWrapper<FlowTaskActor>().eq("instance_id", instanceId));
    }

    @Override
    public List<FlowTaskActor> selectListByTaskIdAndActorId(Long taskId, String actorId) {
        return mapper.selectList(new QueryWrapper<FlowTaskActor>().eq("task_id", taskId).eq("actor_id", actorId));
    }
}

