package org.nkk.flow.entity;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;
import org.nkk.flow.core.context.FlowContext;

import java.util.HashMap;
import java.util.Map;

/**
 * 活动流程实例。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flow_instance")
public class FlowInstance extends FlowEntity implements Cloneable {

    private static final long serialVersionUID = 1L;

    /**
     * 流程定义 ID。
     */
    private Long processId;

    /**
     * 流程定义 key，与 processId 对应的业务可读标识。
     * 回调 Handler 可以通过此字段快速判断是哪个流程，无需反查 flow_process 表。
     */
    private String processKey;

    /**
     * 父流程实例 ID，子流程实例会记录发起它的父实例。
     */
    private Long parentInstanceId;

    /**
     * 实例优先级，值越大优先级越高。
     */
    private Integer priority;

    /**
     * 流程实例编号。
     */
    private String instanceNo;

    /**
     * 业务主键，用于关联业务单据或业务数据。
     */
    private String businessKey;

    /**
     * 流程变量 JSON 内容。
     */
    private String variable;

    /**
     * 当前节点名称。
     */
    private String currentNodeName;

    /**
     * 当前节点编码。
     */
    private String currentNodeKey;

    /**
     * 实例过期时间。
     */
    private java.util.Date expireTime;

    /**
     * 最后更新人名称。
     */
    private String lastUpdateBy;

    /**
     * 最后更新时间。
     */
    private java.util.Date lastUpdateTime;

    /**
     * 根据业务主键创建流程实例对象。
     *
     * @param businessKey 业务主键
     * @return 流程实例对象
     */
    public static FlowInstance of(String businessKey) {
        FlowInstance instance = new FlowInstance();
        instance.setBusinessKey(businessKey);
        return instance;
    }

    /**
     * 将流程变量 JSON 转换为 Map。
     *
     * @return 流程变量 Map
     */
    public Map<String, Object> variableToMap() {
        if (StrUtil.isBlank(variable)) {
            return new HashMap<>();
        }
        Map<String, Object> map = FlowContext.fromJson(variable, Map.class);
        return map == null ? new HashMap<>() : map;
    }

    /**
     * 合并流程变量并重新写回 JSON。
     *
     * @param args 待合并变量
     */
    public void putAllVariable(Map<String, Object> args) {
        if (CollUtil.isEmpty(args)) {
            return;
        }
        Map<String, Object> map = variableToMap();
        map.putAll(args);
        this.variable = FlowContext.toJson(map);
    }
}


