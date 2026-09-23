package org.nkk.flow.dao.mybatis;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.nkk.flow.dao.FlowHisTaskDao;
import org.nkk.flow.entity.FlowHisTask;
import org.nkk.flow.mapper.FlowHisTaskMapper;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 历史任务 MyBatis-Plus DAO。
 */
public class FlowHisTaskDaoMybatisPlusImpl implements FlowHisTaskDao {

    private final FlowHisTaskMapper mapper;

    public FlowHisTaskDaoMybatisPlusImpl(FlowHisTaskMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean insert(FlowHisTask task) {
        return mapper.insert(task) > 0;
    }

    @Override
    public boolean updateById(FlowHisTask task) {
        return mapper.updateById(task) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return mapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteByInstanceIds(List<Long> instanceIds) {
        if (instanceIds == null || instanceIds.isEmpty()) {
            return true;
        }
        mapper.delete(new QueryWrapper<FlowHisTask>().in("instance_id", instanceIds));
        return true;
    }

    @Override
    public FlowHisTask selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<FlowHisTask> selectListByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return mapper.selectBatchIds(ids);
    }

    @Override
    public List<FlowHisTask> selectListByCreateId(String createId) {
        if (createId == null) {
            return Collections.emptyList();
        }
        return mapper.selectList(new QueryWrapper<FlowHisTask>()
                .eq("create_id", createId)
                .orderByDesc("finish_time")
                .orderByDesc("id"));
    }

    @Override
    public List<FlowHisTask> selectListByInstanceId(Long instanceId) {
        return mapper.selectList(new QueryWrapper<FlowHisTask>().eq("instance_id", instanceId));
    }

    @Override
    public List<FlowHisTask> selectListByTaskKey(Long instanceId, String taskKey) {
        return mapper.selectList(new QueryWrapper<FlowHisTask>().eq("instance_id", instanceId).eq("task_key", taskKey));
    }

    @Override
    public IPage<FlowHisTask> selectPageByCreateId(IPage<FlowHisTask> page, String createId,
                                                    List<Integer> excludeTaskTypes, Date startTime, Date endTime,
                                                    Collection<Long> instanceIds,
                                                    Collection<Integer> excludeInstanceStates) {
        QueryWrapper<FlowHisTask> wrapper = new QueryWrapper<>();
        wrapper.eq("create_id", createId);
        if (CollUtil.isNotEmpty(excludeTaskTypes)) {
            wrapper.notIn("task_type", excludeTaskTypes);
        }
        wrapper.ge(startTime != null, "finish_time", startTime);
        wrapper.le(endTime != null, "finish_time", endTime);
        if (CollUtil.isNotEmpty(instanceIds)) {
            wrapper.in("instance_id", instanceIds);
        }
        if (CollUtil.isNotEmpty(excludeInstanceStates)) {
            // flow_his_task 不携带 instance_state，通过子查询 flow_his_instance 过滤掉
            // 撤销/作废等实例的已审批任务（参数为 Integer 枚举值，无注入风险）
            String states = excludeInstanceStates.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            wrapper.notInSql("instance_id",
                    "SELECT id FROM flow_his_instance WHERE instance_state IN (" + states + ")");
        }
        wrapper.orderByDesc("finish_time");
        wrapper.orderByDesc("id");
        return mapper.selectPage(page, wrapper);
    }
}

