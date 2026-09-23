package org.nkk.flow.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.nkk.flow.entity.FlowHisTask;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 历史任务 DAO。
 */
public interface FlowHisTaskDao {

    boolean insert(FlowHisTask task);

    boolean updateById(FlowHisTask task);

    boolean deleteById(Long id);

    boolean deleteByInstanceIds(List<Long> instanceIds);

    FlowHisTask selectById(Long id);

    List<FlowHisTask> selectListByIds(List<Long> ids);

    List<FlowHisTask> selectListByCreateId(String createId);

    List<FlowHisTask> selectListByInstanceId(Long instanceId);

    List<FlowHisTask> selectListByTaskKey(Long instanceId, String taskKey);

    /**
     * 按审批人 ID 分页查询历史任务（审批记录）。
     *
     * <p>本表（flow_his_task）不携带实例状态，需排除"实例已撤销/作废"等场景时，
     * 通过 {@code excludeInstanceStates} 触发对 flow_his_instance 的子查询过滤。
     *
     * @param page                  分页参数
     * @param createId              审批人 ID（create_id），必填
     * @param excludeTaskTypes       需要排除的任务类型列表（如 START=0、COPY=2），为空不排除
     * @param startTime             审批时间下限（finish_time，含），为空不过滤
     * @param endTime               审批时间上限（finish_time，含），为空不过滤
     * @param instanceIds           实例 ID 集合（IN 过滤，前置过滤用），为 null 或空不过滤
     * @param excludeInstanceStates 需要排除的实例状态值（参考 {@code FlowInstanceEnum.InstanceState}），
     *                              为 null 或空不过滤；通过 NOT IN 子查询 flow_his_instance 实现
     * @return 分页结果
     */
    IPage<FlowHisTask> selectPageByCreateId(IPage<FlowHisTask> page, String createId,
                                            List<Integer> excludeTaskTypes, Date startTime, Date endTime,
                                            Collection<Long> instanceIds,
                                            Collection<Integer> excludeInstanceStates);
}

