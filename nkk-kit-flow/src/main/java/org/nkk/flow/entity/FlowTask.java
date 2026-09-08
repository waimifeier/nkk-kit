package org.nkk.flow.entity;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;
import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.enums.core.FlowTaskEnum.PerformType;
import org.nkk.flow.enums.core.FlowTaskEnum.TaskType;

import java.util.HashMap;
import java.util.Map;

/**
 * 活动任务。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flow_task")
public class FlowTask extends FlowEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 流程实例 ID。
     */
    private Long instanceId;

    /**
     * 父任务 ID，顺序会签、子流程等场景会使用该字段关联来源任务。
     */
    private Long parentTaskId;

    /**
     * 被调用的子流程定义 ID。
     */
    private Long callProcessId;

    /**
     * 被调用的子流程实例 ID。
     */
    private Long callInstanceId;

    /**
     * 任务名称。
     */
    private String taskName;

    /**
     * 任务节点编码。
     */
    private String taskKey;

    /**
     * 任务类型，取值见 {@link TaskType}。
     */
    private Integer taskType;

    /**
     * 审批参与方式，取值见 {@link PerformType}。
     */
    private Integer performType;

    /**
     * 任务办理页地址。
     */
    private String actionUrl;

    /**
     * 任务变量 JSON 内容。
     */
    private String variable;

    /**
     * 审批意见。
     */
    private String opinion;

    /**
     * 委派人或转办发起人 ID。
     */
    private String assignorId;

    /**
     * 委派人或转办发起人名称。
     */
    private String assignor;

    /**
     * 任务过期时间。
     */
    private java.util.Date expireTime;

    /**
     * 任务提醒时间。
     */
    private java.util.Date remindTime;

    /**
     * 是否重复提醒，0 表示不重复。
     */
    private Integer remindRepeat = 0;

    /**
     * 任务到期处理模式，1 表示超时自动拒绝，其他值按自动通过处理。
     */
    private Integer termMode;

    /**
     * 任务是否已查看，0 表示未查看，1 表示已查看。
     */
    private Integer viewed = 0;

    /**
     * 将任务变量 JSON 转换为 Map。
     *
     * @return 任务变量 Map
     */
    public Map<String, Object> variableToMap() {
        if (StrUtil.isBlank(variable)) {
            return new HashMap<>();
        }
        Map<String, Object> map = FlowContext.fromJson(variable, Map.class);
        return map == null ? new HashMap<>() : map;
    }

    /**
     * 合并任务变量并重新写回 JSON。
     *
     * @param args 待合并变量
     * @return 当前任务对象
     */
    public FlowTask putAllVariable(Map<String, Object> args) {
        if (CollUtil.isNotEmpty(args)) {
            Map<String, Object> map = variableToMap();
            map.putAll(args);
            variable = FlowContext.toJson(map);
        }
        return this;
    }
}


