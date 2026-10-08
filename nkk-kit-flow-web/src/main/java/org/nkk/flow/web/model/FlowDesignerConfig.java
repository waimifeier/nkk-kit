package org.nkk.flow.web.model;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 流程设计器节点配置（节点注册表）。
 *
 * <p>与前端 FlowDesignerConfig 对齐，是 GET {@code /designer/config} 的响应体：
 * 设计器添加节点弹窗的可选项与节点配置抽屉的可配置项均由本注册表驱动。
 * nodes 声明列表即可添加节点白名单（0 发起 / -1 结束 / 3 条件配置为结构节点，固定不进弹窗）。</p>
 *
 * <p>配置一经下发即完全生效，因此 properties 应返回完整默认配置而非省略；
 * 可通过 {@link #defaults()} 获取与前端内置默认全集对齐的注册表，再按需裁剪或定制。</p>
 */
@Data
public class FlowDesignerConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置结构版本号，当前固定为 1，用于后续结构演进时的兼容判断。
     */
    private Integer version = 1;

    /**
     * 节点注册表条目列表，按添加节点弹窗的展示顺序排列。
     */
    private List<FlowDesignerNodeRegistryEntry> nodes;

    /**
     * 构建设计器默认节点注册表（与前端内置默认全集对齐）。
     *
     * <p>作为 defaultProvider 的兜底配置：0/1/2 任务节点携带完整 properties
     * （选项子集、区块显隐、权限白名单均为前端默认展示口径），其余节点仅声明文案。
     * 添加节点弹窗实际展示审批 / 抄送 / 条件分支 / 延迟等待 / 触发器 / 并行分支 /
     * 包容分支 / 自动通过 / 自动拒绝（0 发起 / -1 结束为结构节点固定不进弹窗；
     * 3 条件配置是 4/7/8 路由的分支子项，由前端自动处理，不下发），
     * 与前端未指定配置时的兜底表现一致。</p>
     *
     * <p>各条目显式携带标准图标与主题色（与前端内置默认主题一致：icon 为 lucide-static
     * 在线 SVG 地址、color 为十六进制色值），保证默认配置开箱即有视觉样式。
     * 使用方可按条目覆盖 icon/color 实现定制；不需要图标时置 null 即不展示。</p>
     *
     * <p>本默认注册表是使用方未提供 {@code FlowDesignerConfigProvider}（或返回 null）
     * 时的内置兜底；Provider 的返回值即最终下发内容（写什么节点就下发什么节点，
     * 整体替换、不与本默认值合并），需要替换时请写出完整节点列表与属性。</p>
     *
     * @return 默认节点注册表（可变列表，可直接修改后返回）
     */
    public static FlowDesignerConfig defaults() {
        FlowDesignerConfig config = new FlowDesignerConfig();

        FlowDesignerNodeRegistryEntry initiator = FlowDesignerNodeRegistryEntry.of(0, "发起人", "配置发起人",
                lucide("users"), "#64748b");
        initiator.setProperties(initiatorProperties());

        FlowDesignerNodeRegistryEntry approval = FlowDesignerNodeRegistryEntry.of(1, "审批节点", "人工审批",
                lucide("stamp"), "#f59e0b");
        approval.setProperties(approvalProperties());

        FlowDesignerNodeRegistryEntry cc = FlowDesignerNodeRegistryEntry.of(2, "抄送节点", "通知抄送",
                lucide("send"), "#2563eb");

        config.setNodes(new ArrayList<>(Arrays.asList(
                FlowDesignerNodeRegistryEntry.of(-1, "结束节点", "流程结束", lucide("flag-triangle-right"), "#9ca3af"),
                initiator,
                approval,
                cc,
                FlowDesignerNodeRegistryEntry.of(4, "条件分支", "按条件分流", lucide("git-branch"), "#16a34a"),
                FlowDesignerNodeRegistryEntry.of(5, "延迟等待", "延时后继续流转", lucide("clock"), "#0f766e"),
                FlowDesignerNodeRegistryEntry.of(6, "触发器", "触发外部动作", lucide("zap"), "#db2777"),
                FlowDesignerNodeRegistryEntry.of(7, "并行分支", "多路并行审批", lucide("git-fork"), "#84cc16"),
                FlowDesignerNodeRegistryEntry.of(8, "包容分支", "按条件并行分流", lucide("list-checks"), "#84cc16"),
                FlowDesignerNodeRegistryEntry.of(9, "自动通过", "自动审批通过", lucide("shield-check"), "#16a34a"),
                FlowDesignerNodeRegistryEntry.of(10, "自动拒绝", "自动审批拒绝", lucide("shield-x"), "#dc2626")
        )));
        return config;
    }

    /**
     * lucide-static 在线 SVG 图标地址（经 cdn.jsdmirror.com 镜像加速），
     * 与前端 iconUrl 同源；bolt→zap、list-check→list-checks 的 lucide 名称映射已在此落定。
     */
    private static String lucide(String icon) {
        return "https://cdn.jsdmirror.com/npm/lucide-static/icons/" + icon + ".svg";
    }

    /** 发起人节点抽屉属性默认全集：setType 白名单（value=null 表示所有人，不限制发起人） */
    private static FlowDesignerNodeProperties initiatorProperties() {
        FlowDesignerNodeProperties properties = new FlowDesignerNodeProperties();
        properties.setSetType(Arrays.asList(
                FlowDesignerChoiceOption.of(null, "所有人", "不限制发起人"),
                FlowDesignerChoiceOption.of(1, "指定成员", "选择具体发起人"),
                FlowDesignerChoiceOption.of(3, "指定角色", "按角色匹配发起人"),
                FlowDesignerChoiceOption.of(7, "指定部门", "按部门匹配发起人")
        ));
        return properties;
    }

    /**
     * 审批节点抽屉属性默认全集。
     *
     * <p>setType 仅含前端已点亮能力（5 发起人本人 / 7 指定部门、examineMode=4 票签
     * 的选人 / 权重 UI 尚未实现，下发也会被前端静默过滤）；setType=2 部门领导的
     * 「连续多级审批」由前端选择器内部勾选承载，无需单独下发 6。</p>
     */
    private static FlowDesignerNodeProperties approvalProperties() {
        FlowDesignerNodeProperties properties = new FlowDesignerNodeProperties();
        properties.setSetType(Arrays.asList(
                FlowDesignerChoiceOption.of(1, "指定成员", "选择具体审批人"),
                FlowDesignerChoiceOption.of(2, "部门领导", "按部门层级自动选人"),
                FlowDesignerChoiceOption.of(3, "角色", "按角色匹配审批人"),
                FlowDesignerChoiceOption.of(4, "发起人自选", "由发起人自己选择")
        ));
        properties.setExamineMode(Arrays.asList(
                FlowDesignerChoiceOption.of(1, "顺序签", "按序依次审批"),
                FlowDesignerChoiceOption.of(2, "会签", "所有人审批通过"),
                FlowDesignerChoiceOption.of(3, "或签", "一人通过即可")
        ));
        properties.setApproveSelf(Arrays.asList(
                FlowDesignerChoiceOption.of(0, "自己审批", "由提交人处理"),
                FlowDesignerChoiceOption.of(1, "自动跳过", "自动略过"),
                FlowDesignerChoiceOption.of(2, "转交给上级", "交由上级处理")
        ));
        properties.setRejectStrategy(Arrays.asList(
                FlowDesignerChoiceOption.of(1, "发起人", ""),
                FlowDesignerChoiceOption.of(2, "上一节点", ""),
                FlowDesignerChoiceOption.of(3, "指定节点", ""),
                FlowDesignerChoiceOption.of(4, "终止流程", "")
        ));
        properties.setTermMode(Arrays.asList(
                FlowDesignerChoiceOption.of(2, "自动通过", "超时后自动审批通过"),
                FlowDesignerChoiceOption.of(1, "自动拒绝", "超时后自动审批拒绝")
        ));
        properties.setTermAuto(Boolean.TRUE);
        properties.setRemind(Boolean.TRUE);
        properties.setPermissionOps(Arrays.asList(
                "allowTransfer", "allowAppendNode", "allowRollback", "allowCc", "allowRevoke"));
        return properties;
    }

    /** 抄送节点抽屉属性默认全集：抄送人类型白名单 */
    private static FlowDesignerNodeProperties ccProperties() {
        FlowDesignerNodeProperties properties = new FlowDesignerNodeProperties();
        properties.setSetType(Arrays.asList(
                FlowDesignerChoiceOption.of(1, "指定成员", "选择具体抄送人"),
                FlowDesignerChoiceOption.of(2, "部门领导", "按部门层级自动选人"),
                FlowDesignerChoiceOption.of(3, "角色", "按角色匹配抄送人"),
                FlowDesignerChoiceOption.of(4, "发起人自选", "由发起人自己选择")
        ));
        return properties;
    }
}
