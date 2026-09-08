package org.nkk.flow.dao.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.nkk.flow.dao.FlowHisTaskActorDao;
import org.nkk.flow.entity.FlowHisTaskActor;
import org.nkk.flow.mapper.FlowHisTaskActorMapper;

import java.util.Collections;
import java.util.List;

/**
 * 历史任务参与者 MyBatis-Plus DAO。
 */
public class FlowHisTaskActorDaoMybatisPlusImpl implements FlowHisTaskActorDao {

    private final FlowHisTaskActorMapper mapper;

    public FlowHisTaskActorDaoMybatisPlusImpl(FlowHisTaskActorMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean insert(FlowHisTaskActor actor) {
        return mapper.insert(actor) > 0;
    }

    @Override
    public boolean deleteByTaskId(Long taskId) {
        mapper.delete(new QueryWrapper<FlowHisTaskActor>().eq("task_id", taskId));
        return true;
    }

    @Override
    public boolean deleteByInstanceIds(List<Long> instanceIds) {
        if (instanceIds == null || instanceIds.isEmpty()) {
            return true;
        }
        mapper.delete(new QueryWrapper<FlowHisTaskActor>().in("instance_id", instanceIds));
        return true;
    }

    @Override
    public List<FlowHisTaskActor> selectList() {
        return mapper.selectList(new QueryWrapper<FlowHisTaskActor>());
    }

    @Override
    public List<FlowHisTaskActor> selectListByTaskId(Long taskId) {
        return mapper.selectList(new QueryWrapper<FlowHisTaskActor>().eq("task_id", taskId));
    }

    @Override
    public List<FlowHisTaskActor> selectListByInstanceId(Long instanceId) {
        return mapper.selectList(new QueryWrapper<FlowHisTaskActor>().eq("instance_id", instanceId));
    }

    @Override
    public List<FlowHisTaskActor> selectListByTaskIdAndActorId(Long taskId, String actorId) {
        return mapper.selectList(new QueryWrapper<FlowHisTaskActor>().eq("task_id", taskId).eq("actor_id", actorId));
    }
}

