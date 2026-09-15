package org.nkk.flow.core.extension.condition;

import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Collections;
import java.util.Map;

/**
 * 业务审批触发条件 SpEL 求值器。
 *
 * <p>对应 {@code FlowProcessModel.triggerExpression}：业务表单（form_source_type=business）
 * 发起审批前，以流程变量为上下文对表达式求值，结果为 true 才发起流程。</p>
 *
 * <h3>变量绑定</h3>
 * <ul>
 *   <li>流程变量同时注册为 SpEL 变量（{@code #fieldName}，设计器生成的标准写法）；</li>
 *   <li>变量 Map 同时作为根对象，兼容 {@code fieldName} 直接属性访问的写法。</li>
 * </ul>
 *
 * <p>支持 SpEL 文本操作符（{@code and}/{@code or}/{@code not}）与符号操作符
 * （{@code &&/||/!}）、比较运算（{@code == != &gt; &gt;= &lt; &lt;=}）
 * 以及字符串/集合方法（如 {@code #code.contains('SN')}）。</p>
 *
 * <p>表达式由流程设计器配置（受信管理员编写），使用 {@link StandardEvaluationContext}
 * 以获得完整 SpEL 能力。表达式非法或运行时求值出错时抛出
 * {@link IllegalStateException}，由调用方按「配置有误」快速暴露。</p>
 */
public class SpelTriggerExpression {

    /**
     * SpelExpressionParser 线程安全，可全局复用。
     */
    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    /**
     * 以流程变量为上下文求值触发表达式。
     *
     * @param expression SpEL 表达式，调用方需保证非空白
     * @param variables 发起流程时传入的变量 Map，为 null 时按空 Map 处理
     * @return true 表示满足触发条件，应发起审批流程
     * @throws IllegalStateException 表达式非法或运行时求值异常
     */
    public boolean match(String expression, Map<String, Object> variables) {
        Map<String, Object> args = variables == null ? Collections.emptyMap() : variables;
        try {
            Expression spel = PARSER.parseExpression(expression);
            StandardEvaluationContext context = new StandardEvaluationContext(args);
            // #变量名 引用（设计器生成的标准形式）
            context.setVariables(args);
            // 变量名直接属性访问（department_id 风格）：SpEL 默认不支持 Map.key 取值，注册 MapAccessor 兼容
            context.addPropertyAccessor(new MapAccessor());
            Boolean result = spel.getValue(context, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            throw new IllegalStateException("业务审批触发条件表达式求值失败：" + expression, e);
        }
    }
}
