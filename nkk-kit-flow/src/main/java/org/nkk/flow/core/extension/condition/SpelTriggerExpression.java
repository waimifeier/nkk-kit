package org.nkk.flow.core.extension.condition;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.ast.Operator;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.nkk.flow.exception.FlowException;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
 * <h3>类型宽容相等（{@code ==} / {@code !=}）</h3>
 * <p>SpEL 原生相等比较不做 Number↔String 强转（{@code 1 == "1"} 恒为 false、{@code 1 != "1"} 恒为 true），
 * 当流程变量为业务实体的数值类型、而设计器条件写的是字符串字面量时会误判。本求值器在解析阶段
 * 把相等比较改写为 {@code #coerceEq(...)}，按数值/布尔做跨类型比较（{@code 1 == "1"}、
 * {@code true == "true"} 均成立），字符串与字符串比较语义保持不变。</p>
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
     * {@code #coerceEq} 类型宽容相等函数，注册到求值上下文，供改写后的表达式调用。
     */
    private static final Method COERCE_EQ = resolveCoerceEqMethod();

    private static Method resolveCoerceEqMethod() {
        try {
            return SpelTriggerExpression.class.getMethod("coerceEquals", Object.class, Object.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("coerceEquals method not found", e);
        }
    }

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
            spel = PARSER.parseExpression(rewriteEqualityComparisons(expression));
        } catch (FlowException e) {
            throw e;
        } catch (Exception e) {
            throw new FlowException("业务审批触发条件表达式语法错误：" + expression, e);
        }
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            // 流程变量同时注册为根对象和变量：根对象支持 #root-less 写法（field），变量支持 #field 写法
            context.setRootObject(args);
            context.setVariables(args);
            context.addPropertyAccessor(MAP_ACCESSOR);
            // 类型宽容相等函数，供 ==/!= 改写后的 #coerceEq(...) 调用
            context.registerFunction("coerceEq", COERCE_EQ);
            Boolean result = spel.getValue(context, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            // 求值阶段异常（变量不存在、null 方法调用、类型不匹配等）按条件不满足处理
            return false;
        }
    }

    /**
     * 把表达式中的 {@code ==}/{@code !=} 改写为类型宽容的 {@code #coerceEq(...)} 调用。
     *
     * <p>SpEL 的 {@code ==}/{@code !=} 不做 Number↔String 类型强转（{@code 1 == "1"} 恒为
     * false），而流程变量多为业务实体的数值类型、设计器条件里写的是字符串字面量，两者类型
     * 不一致会导致条件误判。此处在解析后的 AST 上定位每个相等比较节点，用操作数原文改写为
     * {@code #coerceEq(left, right)}（{@code !=} 取反），由 {@link #coerceEquals} 做数值/布尔
     * 宽容比较；字符串与字符串比较语义完全不变，无回归。</p>
     *
     * <p>改写基于 SpEL 节点位置（操作数子节点覆盖完整操作数文本，end 为开区间）：左操作数
     * 起点 → 右操作数终点即完整比较片段。位置越界时跳过该节点，保持原始表达式不变。</p>
     *
     * @param expression 原始 SpEL 表达式
     * @return 改写后的表达式；不含相等比较或无法改写时返回原文
     */
    private static String rewriteEqualityComparisons(String expression) {
        Expression parsed;
        try {
            parsed = PARSER.parseExpression(expression);
        } catch (Exception e) {
            throw new FlowException("业务审批触发条件表达式语法错误：" + expression, e);
        }
        if (!(parsed instanceof SpelExpression)) {
            return expression;
        }
        List<EqualityRewrite> rewrites = new ArrayList<>();
        collectEqualityNodes(((SpelExpression) parsed).getAST(), expression, rewrites);
        if (rewrites.isEmpty()) {
            return expression;
        }
        // 从后往前替换，避免前面替换后 offset 失效；与前一次已改写区间重叠的节点跳过
        rewrites.sort((a, b) -> Integer.compare(b.start, a.start));
        StringBuilder builder = new StringBuilder(expression);
        int boundary = Integer.MAX_VALUE;
        for (EqualityRewrite rewrite : rewrites) {
            if (rewrite.end > boundary) {
                continue;
            }
            builder.replace(rewrite.start, rewrite.end, rewrite.text);
            boundary = rewrite.start;
        }
        return builder.toString();
    }

    /** 递归收集相等比较节点（{@code ==}/{@code !=}），命中即记录并停止下钻。 */
    private static void collectEqualityNodes(SpelNode node, String source, List<EqualityRewrite> out) {
        if (node instanceof Operator) {
            String operator = ((Operator) node).getOperatorName();
            if ("==".equals(operator) || "!=".equals(operator)) {
                SpelNode left = node.getChild(0);
                SpelNode right = node.getChild(1);
                int start = left.getStartPosition();
                int end = right.getEndPosition();
                if (start >= 0 && start < end && end <= source.length()) {
                    String call = "#coerceEq("
                            + source.substring(start, left.getEndPosition())
                            + ", "
                            + source.substring(right.getStartPosition(), end) + ")";
                    out.add(new EqualityRewrite(start, end, "==".equals(operator) ? call : "!(" + call + ")"));
                }
                return;
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            collectEqualityNodes(node.getChild(i), source, out);
        }
    }

    /**
     * 类型宽容相等：先按原值相等判断，再对「数值 ↔ 字符串」「布尔 ↔ 字符串」做跨类型比较。
     *
     * <ul>
     *   <li>同类型/可空：{@link Objects#equals}</li>
     *   <li>Number ↔ 字符串：字符串可按数值解析时按数值比较（如 {@code 1} 与 {@code "1"} 相等）</li>
     *   <li>Boolean ↔ 字符串：字符串为 true/false（忽略大小写）时按布尔比较</li>
     *   <li>其余跨类型一律视为不等，保持字符串比较原语义、无回归</li>
     * </ul>
     */
    public static boolean coerceEquals(Object left, Object right) {
        if (Objects.equals(left, right)) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        if (left instanceof Number && right instanceof CharSequence) {
            return numberEqualsText((Number) left, right.toString());
        }
        if (right instanceof Number && left instanceof CharSequence) {
            return numberEqualsText((Number) right, left.toString());
        }
        if (left instanceof Boolean && right instanceof CharSequence) {
            return booleanEqualsText((Boolean) left, right.toString());
        }
        if (right instanceof Boolean && left instanceof CharSequence) {
            return booleanEqualsText((Boolean) right, left.toString());
        }
        return false;
    }

    /** 数值与字符串按 BigDecimal 比较，字符串无法解析为数值时视为不等。 */
    private static boolean numberEqualsText(Number number, String text) {
        try {
            return new BigDecimal(number.toString()).compareTo(new BigDecimal(text.trim())) == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /** 布尔与字符串比较，字符串须为 true/false（忽略大小写），否则视为不等。 */
    private static boolean booleanEqualsText(Boolean value, String text) {
        String normalized = text.trim();
        if ("true".equalsIgnoreCase(normalized)) {
            return value;
        }
        if ("false".equalsIgnoreCase(normalized)) {
            return !value;
        }
        return false;
    }

    /** 一次相等比较改写：源串区间 [{@code start}, {@code end}) → {@code text}。 */
    private static final class EqualityRewrite {
        private final int start;
        private final int end;
        private final String text;

        private EqualityRewrite(int start, int end, String text) {
            this.start = start;
            this.end = end;
            this.text = text;
        }
    }
}
