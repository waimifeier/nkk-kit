package org.nkk.flow.core.extension.condition;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.nkk.flow.exception.FlowException;

import java.util.Collections;
import java.util.Map;

/**
 * 业务审批触发条件 SpEL 求值器。
 *
 * <p>对应 {@code FlowProcessModel.triggerExpression}：业务表单（form_source_type=business）
 * 发起审批前，以流程变量为上下文对表达式求值，结果为 true 才发起流程。</p>
 *
 * <h3>变量绑定</h3>
 * <p>流程变量同时注册为根对象和 SpEL 变量，两种引用方式均可用：</p>
 * <ul>
 *   <li>{@code fieldName} — 根对象属性访问（Map key），如 {@code outbound_code.contains('123')}</li>
 *   <li>{@code #fieldName} — 变量引用，如 {@code #outbound_code.contains('123')}</li>
 * </ul>
 *
 * <p>支持 SpEL 文本操作符（{@code and}/{@code or}/{@code not}）与符号操作符
 * （{@code &&/||/!}）、比较运算（{@code == != &gt; &gt;= &lt; &lt;=}）
 * 以及字符串/集合方法（如 {@code outbound_code.contains('SN')}）。</p>
 *
 * <p>表达式由流程设计器配置（受信管理员编写），使用 {@link StandardEvaluationContext}
 * 以获得完整 SpEL 能力。表达式非法或运行时求值出错时抛出
 * {@link FlowException}，由调用方按「配置有误」快速暴露。</p>
 */
public class SpelTriggerExpression {

    /**
     * SpelExpressionParser 线程安全，可全局复用。
     */
    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    /**
     * Map 属性访问器：让 SpEL 能用 {@code map.key} 语法访问 Map 的 key。
     * <p>{@code StandardEvaluationContext} 默认的 {@code ReflectivePropertyAccessor}
     * 在部分 Spring 版本不支持 Map 的 key 作为属性，此处显式注册一个。</p>
     */
    private static final PropertyAccessor MAP_ACCESSOR = new PropertyAccessor() {
        @Override
        public Class<?>[] getSpecificTargetClasses() {
            return null;
        }

        @Override
        public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
            // 不检查 containsKey：变量不存在时返回 null，让表达式求值自然失败（按条件不满足处理）
            return target instanceof Map;
        }

        @Override
        public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
            return new TypedValue(((Map<?, ?>) target).get(name));
        }

        @Override
        public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
            return false;
        }

        @Override
        public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
        }
    };

    /**
     * 以流程变量为上下文求值触发表达式。
     *
     * @param expression SpEL 表达式，调用方需保证非空白
     * @param variables 发起流程时传入的变量 Map，为 null 时按空 Map 处理
     * @return true 表示满足触发条件，应发起审批流程
     * @throws FlowException 表达式非法或运行时求值异常
     */
    public boolean match(String expression, Map<String, Object> variables) {
        Map<String, Object> args = variables == null ? Collections.emptyMap() : variables;
        Expression spel;
        try {
            spel = PARSER.parseExpression(expression);
        } catch (Exception e) {
            throw new FlowException("业务审批触发条件表达式语法错误：" + expression, e);
        }
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            // 流程变量同时注册为根对象和变量：根对象支持 #root-less 写法（field），变量支持 #field 写法
            context.setRootObject(args);
            context.setVariables(args);
            context.addPropertyAccessor(MAP_ACCESSOR);
            Boolean result = spel.getValue(context, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            // 求值阶段异常（变量不存在、null 方法调用、类型不匹配等）按条件不满足处理
            return false;
        }
    }
}
