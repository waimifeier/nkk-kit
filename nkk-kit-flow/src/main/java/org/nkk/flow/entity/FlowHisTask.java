package org.nkk.flow.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;
import org.nkk.flow.enums.core.FlowTaskEnum.TaskState;

import java.util.Date;

/**
 * 历史任务。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flow_his_task")
public class FlowHisTask extends FlowTask {

    private static final long serialVersionUID = 1L;

    /**
     * 任务完成时间。
     */
    private Date finishTime;

    /**
     * 任务最终状态，取值见 {@link TaskState}。
     */
    private Integer taskState;

    /**
     * 任务耗时，单位毫秒。
     */
    private Long duration;

    /**
     * 根据活动任务创建历史任务。
     *
     * @param task 活动任务
     * @param state 历史任务状态
     * @return 历史任务
     */
    public static FlowHisTask of(FlowTask task, TaskState state) {
        FlowHisTask his = new FlowHisTask();
        his.setId(task.getId());
        his.setTenantId(task.getTenantId());
        his.setCreateId(task.getCreateId());
        his.setCreateBy(task.getCreateBy());
        his.setCreateTime(task.getCreateTime());
        his.setInstanceId(task.getInstanceId());
        his.setParentTaskId(task.getParentTaskId());
        his.setCallProcessId(task.getCallProcessId());
        his.setCallInstanceId(task.getCallInstanceId());
        his.setTaskName(task.getTaskName());
        his.setTaskKey(task.getTaskKey());
        his.setTaskType(task.getTaskType());
        his.setPerformType(task.getPerformType());
        his.setActionUrl(task.getActionUrl());
        his.setVariable(task.getVariable());
        his.setOpinion(task.getOpinion());
        his.setAssignorId(task.getAssignorId());
        his.setAssignor(task.getAssignor());
        his.setExpireTime(task.getExpireTime());
        his.setRemindTime(task.getRemindTime());
        his.setRemindRepeat(task.getRemindRepeat());
        his.setTermMode(task.getTermMode());
        his.setViewed(task.getViewed());
        his.setTaskState(state.value());
        his.setFinishTime(new Date());
        if (task.getCreateTime() != null) {
            his.setDuration(his.getFinishTime().getTime() - task.getCreateTime().getTime());
        }
        return his;
    }
}


