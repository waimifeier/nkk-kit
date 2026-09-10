package org.nkk.flow.model.node.task;

import lombok.Getter;

import java.io.Serializable;
import java.util.Map;

/**
 * 多人审批收口策略。
 */
@Getter
public class FlowSignPolicy implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通过权重配置 key。
     */
    public static final String PASS_WEIGHT = "passWeight";

    /**
     * 拒绝权重配置 key。
     */
    public static final String REJECT_WEIGHT = "rejectWeight";

    /**
     * 一票否决权重配置 key。
     */
    public static final String VETO_WEIGHT = "vetoWeight";

    /**
     * 是否允许弃权配置 key。
     */
    public static final String ALLOW_ABSTAIN = "allowAbstain";

    /**
     * 弃权是否按通过计算配置 key。
     */
    public static final String ABSTAIN_AS_PASS = "abstainAsPass";

    /**
     * 通过权重，范围 1-100。
     */
    private int passWeight = 50;

    /**
     * 拒绝权重，达到该权重后多人审批结果为拒绝。
     */
    private Integer rejectWeight;

    /**
     * 一票否决权重，单个拒绝参与人的权重大于等于该值时直接拒绝。
     */
    private Integer vetoWeight;

    /**
     * 是否允许弃权。
     */
    private boolean allowAbstain;

    /**
     * 弃权是否按通过计算。
     */
    private boolean abstainAsPass;

    /**
     * 从节点扩展配置中解析多人审批收口策略。
     *
     * @param nodeModel 任务节点模型
     * @return 多人审批收口策略
     */
    public static FlowSignPolicy of(TaskNodeModel nodeModel) {
        FlowSignPolicy policy = new FlowSignPolicy();
        if (nodeModel == null) {
            return policy;
        }
        Map<String, Object> config = nodeModel.getExtendConfig();
        Integer passWeight = readInteger(config, PASS_WEIGHT);
        if (passWeight == null) {
            passWeight = nodeModel.getPassWeight();
        }
        policy.passWeight = normalizePercent(passWeight, 50);
        policy.rejectWeight = normalizeNullablePercent(readInteger(config, REJECT_WEIGHT));
        policy.vetoWeight = normalizeNullablePercent(readInteger(config, VETO_WEIGHT));
        policy.allowAbstain = readBoolean(config, ALLOW_ABSTAIN, false);
        policy.abstainAsPass = readBoolean(config, ABSTAIN_AS_PASS, false);
        return policy;
    }

    /**
     * 标准化百分比，非法值使用默认值。
     *
     * @param value 待标准化数值
     * @param defaultValue 默认值
     * @return 标准化后的百分比
     */
    private static int normalizePercent(Integer value, int defaultValue) {
        if (value == null || value <= 0 || value > 100) {
            return defaultValue;
        }
        return value;
    }

    /**
     * 标准化可空百分比，非法值返回 null。
     *
     * @param value 待标准化数值
     * @return 标准化后的百分比
     */
    private static Integer normalizeNullablePercent(Integer value) {
        if (value == null || value <= 0 || value > 100) {
            return null;
        }
        return value;
    }

    /**
     * 从扩展配置中读取整数。
     *
     * @param config 扩展配置
     * @param key 配置 key
     * @return 整数值，无法解析时返回 null
     */
    private static Integer readInteger(Map<String, Object> config, String key) {
        if (config == null || !config.containsKey(key)) {
            return null;
        }
        Object value = config.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.valueOf(((String) value).trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    /**
     * 从扩展配置中读取布尔值。
     *
     * @param config 扩展配置
     * @param key 配置 key
     * @param defaultValue 默认值
     * @return 布尔值
     */
    private static boolean readBoolean(Map<String, Object> config, String key, boolean defaultValue) {
        if (config == null || !config.containsKey(key)) {
            return defaultValue;
        }
        Object value = config.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean(((String) value).trim());
        }
        return defaultValue;
    }
}

