package org.nkk.flow.model.node;

import org.nkk.flow.enums.node.FlowNodeTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.nkk.flow.model.node.router.RouterNodeModel;
import org.nkk.flow.model.node.task.TaskNodeModel;

/**
 * 节点模型类型声明注解。
 *
 * <p>标注在 {@link FlowNodeModel} 的具体子类上，声明该子类对应的节点类型。
 * JSON 处理器启动时扫描 {@code org.nkk.flow.model} 包下带本注解的类，
 * 自动向 Jackson 注册多态子类型（{@code type} 字段为判别字段）。</p>
 *
 * <p>新增节点类型只需：新建继承自 {@link FlowNodeModel}/{@link TaskNodeModel}/{@link RouterNodeModel}
 * 的子类并贴上本注解，无需修改基类上的子类型清单。</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FlowNodeType {

    /**
     * 节点类型枚举，取其 {@link FlowNodeTypeEnum#value()} 作为 JSON 中 {@code type} 字段的判别值。
     *
     * @return 节点类型枚举
     */
    FlowNodeTypeEnum value();
}
