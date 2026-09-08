package org.nkk.flow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;
import org.nkk.flow.enums.node.FlowNodeTypeEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 分支条件节点。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlowConditionNode implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分支节点编码，同一个分支容器内不可重复。
     */
    private String nodeKey;

    /**
     * 分支节点名称。
     */
    private String nodeName;

    /**
     * 分支节点类型，取值见 {@link FlowNodeTypeEnum}，参考设计器通常为 3-条件审批。
     */
    private Integer type;

    /**
     * 分支优先级，值越小越先匹配。
     */
    private Integer priorityLevel = 0;

    /**
     * 分支条件列表，空列表表示默认分支。
     */
    private List<FlowCondition> conditionList = new ArrayList<>();

    /**
     * 当前分支命中后进入的子节点。
     */
    private FlowNodeModel childNode;

    /**
     * 设置分支条件列表。
     *
     * <p>兼容两种设计器数据格式：
     * 一维条件列表 {@code [{field, operator, value}]} 和二维条件分组
     * {@code [[{field, operator, value}]]}。当前默认条件表达式会按 AND 方式处理
     * 归一化后的条件列表。</p>
     *
     * @param conditionList 分支条件列表
     */
    @JsonSetter("conditionList")
    public void setConditionList(Object conditionList) {
        this.conditionList = normalizeConditionList(conditionList);
    }

    /**
     * 将设计器传入的条件列表归一化为一维条件列表。
     *
     * @param source 原始条件列表
     * @return 一维条件列表
     */
    private List<FlowCondition> normalizeConditionList(Object source) {
        List<FlowCondition> result = new ArrayList<>();
        addCondition(result, source);
        return result;
    }

    /**
     * 递归添加条件项。
     *
     * @param result 目标条件列表
     * @param source 原始条件项
     */
    @SuppressWarnings("unchecked")
    private void addCondition(List<FlowCondition> result, Object source) {
        if (source == null) {
            return;
        }
        if (source instanceof FlowCondition) {
            result.add((FlowCondition) source);
            return;
        }
        if (source instanceof List) {
            for (Object item : (List<?>) source) {
                addCondition(result, item);
            }
            return;
        }
        if (source instanceof Map) {
            result.add(toCondition((Map<String, Object>) source));
        }
    }

    /**
     * 将 Map 条件转换为条件模型。
     *
     * @param source 原始条件 Map
     * @return 条件模型
     */
    private FlowCondition toCondition(Map<String, Object> source) {
        FlowCondition condition = new FlowCondition();
        condition.setField(stringValue(source.get("field")));
        condition.setOperator(stringValue(source.get("operator")));
        condition.setValue(source.get("value"));
        return condition;
    }

    /**
     * 将对象转换为字符串。
     *
     * @param value 原始值
     * @return 字符串值
     */
    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}

