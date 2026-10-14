package org.nkk.flow.web.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 流程设计器单类节点的抽屉属性配置。
 *
 * <p>与前端 FlowNodeRegistryProperties 对齐。各字段均为可选（按字段级缺省回退）：</p>
 * <ul>
 *   <li>枚举型字段（{@code setType} 等）为完整选项对象数组，未声明时前端回退内置全集；
 *       声明为空数组表示隐藏该属性区块</li>
 *   <li>{@code termAuto} / {@code remind} 为布尔区块显隐开关，{@code null} 按展示处理，
 *       {@code false} 显式隐藏</li>
 *   <li>{@code permissionOps} 为权限操作白名单（allowTransfer / allowAppendNode /
 *       allowRollback / allowCc / allowRevoke），未声明时全部展示，空数组隐藏整个权限区块</li>
 * </ul>
 *
 * <p>仅非 null 字段参与 JSON 序列化，避免前端把 Java null 当成"已声明值"走到校验逻辑。</p>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlowDesignerNodeProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 审批人 / 发起人 / 抄送人类型选项（节点字段 setType）。
     */
    private List<FlowDesignerChoiceOption> setType;

    /**
     * 审批方式选项（顺序签 / 会签 / 或签 / 票签，节点字段 examineMode）。
     */
    private List<FlowDesignerChoiceOption> examineMode;

    /**
     * 审批人为发起人本人时的处理方式选项（节点字段 approveSelf）。
     */
    private List<FlowDesignerChoiceOption> approveSelf;

    /**
     * 驳回策略选项（节点字段 rejectStrategy）。
     */
    private List<FlowDesignerChoiceOption> rejectStrategy;

    /**
     * 超时自动处理方式选项（1=自动拒绝，2=自动通过，节点字段 termMode）。
     */
    private List<FlowDesignerChoiceOption> termMode;

    /**
     * 是否展示「到期自动处理」配置区块。{@code null} 默认展示。
     */
    private Boolean termAuto;

    /**
     * 是否展示「超时提醒」配置区块。{@code null} 默认展示。
     */
    private Boolean remind;

    /**
     * 权限操作白名单，取值：allowTransfer（允许转交）、allowAppendNode（允许加减签）、
     * allowRollback（允许回退）、allowCc（允许抄送）、allowRevoke（允许撤回流程）。
     * 未声明时全部展示；空列表隐藏整个「操作权限」区块。
     */
    private List<String> permissionOps;
}
