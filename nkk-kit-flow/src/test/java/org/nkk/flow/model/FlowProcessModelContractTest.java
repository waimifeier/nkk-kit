package org.nkk.flow.model;

import cn.hutool.core.util.ClassUtil;
import org.junit.Test;
import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.core.extension.condition.FlowExpression;
import org.nkk.flow.core.extension.condition.SimpleFlowExpression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.nkk.flow.model.node.FlowNodeModel;
import org.nkk.flow.model.node.FlowNodeType;
import org.nkk.flow.model.node.router.ConditionRouterNodeModel;
import org.nkk.flow.model.node.router.FlowCondition;
import org.nkk.flow.model.node.router.FlowConditionNode;
import org.nkk.flow.model.node.task.ApprovalNodeModel;
import org.nkk.flow.model.node.task.CopyNodeModel;
import org.nkk.flow.model.node.task.FlowNodeAssignee;
import org.nkk.flow.model.node.task.StartNodeModel;
import org.nkk.flow.model.node.task.TimerNodeModel;

/**
 * 前后端流程模型 JSON 契约测试。
 *
 * <p>fixture 模拟前端设计器发布时提交的真实 modelContent 结构（经过空 childNode 清理后的形态），
 * 反序列化为 {@link FlowProcessModel} 后再序列化回 JSON，保证往返不丢结构：
 * 条件组二维结构（组间 OR、组内 AND）、条件展示字段 label/type、节点扩展字段等。</p>
 *
 * <p>契约漂移时此测试应失败，例如：conditionList 被拍平、FlowCondition 丢失 label/type、
 * FlowProcessModel 顶层缺 metaFields 等。</p>
 */
public class FlowProcessModelContractTest {

    /**
     * 与前端发布 payload 对齐的模型 JSON（无空 childNode 哨兵）。
     */
    private static final String FRONTEND_MODEL_JSON = "{"
            + "\"key\":\"leave\","
            + "\"name\":\"请假审批\","
            + "\"metaFields\":["
            + "{\"fieldKey\":\"reason\",\"fieldName\":\"请假原因\",\"fieldType\":\"textarea\",\"required\":1,\"sort\":1},"
            + "{\"fieldKey\":\"days\",\"fieldName\":\"请假天数\",\"fieldType\":\"input_number\",\"required\":1,\"sort\":2}"
            + "],"
            + "\"nodeConfig\":{"
            + "\"nodeName\":\"发起人\",\"nodeKey\":\"start_1\",\"type\":0,"
            + "\"childNode\":{"
            + "\"nodeName\":\"主管审批\",\"nodeKey\":\"approve_1\",\"type\":1,"
            + "\"setType\":1,\"examineMode\":1,\"approveSelf\":1,\"examineLevel\":2,"
            + "\"rejectStrategy\":2,\"rejectStart\":1,"
            + "\"termAuto\":true,\"term\":24,\"termMode\":1,\"remind\":true,"
            + "\"allowTransfer\":true,\"allowAppendNode\":true,\"allowRollback\":true,\"allowCc\":true,"
            + "\"actionUrl\":\"/flow/task/detail\","
            + "\"nodeAssigneeList\":[{\"id\":\"emp-1\",\"name\":\"张三\",\"actorType\":2,\"weight\":1}],"
            + "\"extendConfig\":{\"passWeight\":60},"
            + "\"childNode\":{"
            + "\"nodeName\":\"条件分支\",\"nodeKey\":\"branch_1\",\"type\":4,"
            + "\"conditionNodes\":["
            + "{"
            + "\"nodeName\":\"年假分支\",\"nodeKey\":\"cond_1\",\"type\":3,\"priorityLevel\":1,"
            + "\"conditionList\":["
            + "[{\"label\":\"请假原因\",\"field\":\"reason\",\"operator\":\"contains\",\"value\":\"年假\",\"type\":\"form\"}],"
            + "[{\"label\":\"请假天数\",\"field\":\"days\",\"operator\":\"ge\",\"value\":3,\"type\":\"form\"},"
            + "{\"label\":\"请假天数\",\"field\":\"days\",\"operator\":\"lt\",\"value\":7,\"type\":\"form\"}]"
            + "],"
            + "\"childNode\":{"
            + "\"nodeName\":\"抄送人\",\"nodeKey\":\"copy_1\",\"type\":2,"
            + "\"nodeAssigneeList\":[{\"id\":\"emp-2\",\"name\":\"李四\",\"actorType\":2,\"weight\":1}],"
            + "\"childNode\":{"
            + "\"nodeName\":\"延迟等待\",\"nodeKey\":\"timer_1\",\"type\":6,"
            + "\"delayTime\":2,\"delayUnit\":\"hour\""
            + "}"
            + "}"
            + "},"
            + "{\"nodeName\":\"默认分支\",\"nodeKey\":\"cond_default\",\"type\":3,\"priorityLevel\":2}"
            + "]"
            + "}"
            + "}"
            + "}"
            + "}";

    @Test
    public void modelTopLevelStructureIsPreserved() {
        FlowProcessModel model = FlowContext.fromJson(FRONTEND_MODEL_JSON, FlowProcessModel.class);

        assertEquals("leave", model.getKey());
        assertEquals("请假审批", model.getName());
        assertNotNull(model.getNodeConfig());

        assertNotNull(model.getMetaFields());
        assertEquals(2, model.getMetaFields().size());
        FlowFieldMeta first = model.getMetaFields().get(0);
        assertEquals("reason", first.getFieldKey());
        assertEquals("请假原因", first.getFieldName());
        assertEquals("textarea", first.getFieldType());
        assertEquals(Integer.valueOf(1), first.getRequired());
    }

    @Test
    public void approvalNodeConfigIsPreserved() {
        FlowProcessModel model = FlowContext.fromJson(FRONTEND_MODEL_JSON, FlowProcessModel.class);
        FlowNodeModel start = model.getNodeConfig();
        FlowNodeModel approve = start.getChildNode();

        // 关键契约：type 判别字段必须驱动多态反序列化，实例化为对应子类
        assertTrue("发起节点应反序列化为StartNodeModel", start instanceof StartNodeModel);
        assertTrue("审批节点应反序列化为ApprovalNodeModel", approve instanceof ApprovalNodeModel);
        StartNodeModel startNode = (StartNodeModel) start;
        ApprovalNodeModel approvalNode = (ApprovalNodeModel) approve;

        assertEquals("start_1", startNode.getNodeKey());
        assertEquals("approve_1", approvalNode.getNodeKey());
        assertEquals(Integer.valueOf(1), approvalNode.getType());
        assertEquals(Integer.valueOf(1), approvalNode.getSetType());
        assertEquals(Integer.valueOf(1), approvalNode.getExamineMode());
        assertEquals(Integer.valueOf(1), approvalNode.getApproveSelf());
        assertEquals(Integer.valueOf(2), approvalNode.getExamineLevel());
        assertEquals(Integer.valueOf(2), approvalNode.getRejectStrategy());
        assertEquals(Integer.valueOf(1), approvalNode.getRejectStart());
        assertEquals(Boolean.TRUE, approvalNode.getTermAuto());
        assertEquals(Integer.valueOf(24), approvalNode.getTerm());
        assertEquals(Integer.valueOf(1), approvalNode.getTermMode());
        assertEquals(Boolean.TRUE, approvalNode.getAllowTransfer());
        assertEquals(Boolean.TRUE, approvalNode.getAllowRollback());
        assertEquals(Boolean.TRUE, approvalNode.getAllowCc());
        assertEquals("/flow/task/detail", approvalNode.getActionUrl());

        assertNotNull(approvalNode.getNodeAssigneeList());
        assertEquals(1, approvalNode.getNodeAssigneeList().size());
        FlowNodeAssignee assignee = approvalNode.getNodeAssigneeList().get(0);
        assertEquals("emp-1", assignee.getId());
        assertEquals("张三", assignee.getName());
        assertEquals(Integer.valueOf(2), assignee.getActorType());
        assertEquals(Integer.valueOf(1), assignee.getWeight());

        assertNotNull(approvalNode.getExtendConfig());
        assertEquals(60, approvalNode.getExtendConfig().get("passWeight"));
    }

    @Test
    public void conditionListRemainsTwoDimensionalWithDesignerFields() {
        FlowProcessModel model = FlowContext.fromJson(FRONTEND_MODEL_JSON, FlowProcessModel.class);
        FlowNodeModel branch = model.getNodeConfig().getChildNode().getChildNode();

        // 关键契约：type=4 必须反序列化为条件路由容器子类
        assertTrue("条件分支节点应反序列化为ConditionRouterNodeModel", branch instanceof ConditionRouterNodeModel);
        ConditionRouterNodeModel conditionRouter = (ConditionRouterNodeModel) branch;
        assertNotNull(conditionRouter.getConditionNodes());
        FlowConditionNode firstBranch = conditionRouter.getConditionNodes().get(0);
        assertTrue("分支项应反序列化为FlowConditionNode", firstBranch instanceof FlowConditionNode);
        assertEquals("cond_1", firstBranch.getNodeKey());
        assertEquals(Integer.valueOf(1), firstBranch.getPriorityLevel());

        List<List<FlowCondition>> conditionList = firstBranch.getConditionList();
        assertNotNull(conditionList);
        // 关键契约：conditionList 必须保持二维（外层条件组，内层组内条件），不允许被拍平
        assertEquals(2, conditionList.size());
        assertEquals(1, conditionList.get(0).size());
        assertEquals(2, conditionList.get(1).size());

        FlowCondition group1Condition = conditionList.get(0).get(0);
        assertEquals("reason", group1Condition.getField());
        assertEquals("contains", group1Condition.getOperator());
        assertEquals("年假", group1Condition.getValue());
        // 设计器展示字段必须保留（回显条件摘要依赖 label/type）
        assertEquals("请假原因", group1Condition.getLabel());
        assertEquals("form", group1Condition.getType());
    }

    @Test
    public void roundTripSerializationKeepsContract() {
        FlowProcessModel model = FlowContext.fromJson(FRONTEND_MODEL_JSON, FlowProcessModel.class);
        FlowProcessModel reparsed = FlowContext.fromJson(FlowContext.toJson(model), FlowProcessModel.class);

        // 关键契约：序列化必须携带 type 判别字段，再反序列化仍还原为正确子类
        String firstJson = FlowContext.toJson(model);
        assertTrue("序列化后条件分支 type 字段丢失", firstJson.contains("\"type\":4"));
        assertTrue("序列化后审批节点 type 字段丢失", firstJson.contains("\"type\":1"));

        FlowConditionNode firstBranch = ((ConditionRouterNodeModel) reparsed.getNodeConfig().getChildNode()
                .getChildNode()).getConditionNodes().get(0);
        List<List<FlowCondition>> conditionList = firstBranch.getConditionList();
        assertEquals(2, conditionList.size());
        assertEquals("请假原因", conditionList.get(0).get(0).getLabel());
        assertEquals("form", conditionList.get(0).get(0).getType());

        // 持久化后 label/type 必须仍在 JSON 中（不能被 @JsonIgnoreProperties 静默丢弃）
        String json = FlowContext.toJson(reparsed);
        assertTrue("往返序列化后 label 字段丢失", json.contains("\"label\":\"请假原因\""));
        assertTrue("往返序列化后条件来源 type 字段丢失", json.contains("\"type\":\"form\""));
        assertTrue("往返序列化后 approveSelf 字段丢失", json.contains("\"approveSelf\":1"));
        assertTrue("往返序列化后 examineLevel 字段丢失", json.contains("\"examineLevel\":2"));

        // 延迟等待节点字段往返，且多态类型保持
        FlowNodeModel copyNode = firstBranch.getChildNode();
        assertTrue("抄送节点应反序列化为CopyNodeModel", copyNode instanceof CopyNodeModel);
        FlowNodeModel timer = copyNode.getChildNode();
        assertTrue("延迟节点应反序列化为TimerNodeModel", timer instanceof TimerNodeModel);
        TimerNodeModel timerNode = (TimerNodeModel) timer;
        assertEquals("timer_1", timerNode.getNodeKey());
        assertEquals(Integer.valueOf(6), timerNode.getType());
        assertEquals(Integer.valueOf(2), timerNode.getDelayTime());
        assertEquals("hour", timerNode.getDelayUnit());

        assertEquals(2, reparsed.getMetaFields().size());
        assertEquals("请假天数", reparsed.getMetaFields().get(1).getFieldName());
    }

    @Test
    public void validatorAcceptsPolymorphicModel() {
        FlowProcessModel model = FlowContext.fromJson(FRONTEND_MODEL_JSON, FlowProcessModel.class);
        List<String> errors = FlowModelValidator.check(model);
        assertTrue("多态模型校验不应有错误：" + errors, errors.isEmpty());
    }

    @Test
    public void everyFlowNodeTypeAnnotationIsAutoRegistered() {
        Set<Class<?>> classes = ClassUtil.scanPackage("org.nkk.flow.model.node");
        int registered = 0;
        for (Class<?> clazz : classes) {
            FlowNodeType annotation = clazz.getAnnotation(FlowNodeType.class);
            if (annotation == null || !FlowNodeModel.class.isAssignableFrom(clazz) || clazz == FlowNodeModel.class) {
                continue;
            }
            registered++;
            String json = "{\"nodeKey\":\"k_" + clazz.getSimpleName() + "\",\"nodeName\":\"测试节点\",\"type\":"
                    + annotation.value().value() + "}";
            FlowNodeModel node = FlowContext.fromJson(json, FlowNodeModel.class);
            assertNotNull("type=" + annotation.value().value() + " 反序列化结果不能为空", node);
            assertEquals("type=" + annotation.value().value() + " 应自动注册并反序列化为 " + clazz.getSimpleName(),
                    clazz, node.getClass());
        }
        assertEquals("@FlowNodeType 标注的节点类型数量应为 14", 14, registered);
    }

    @Test
    public void expressionEvaluatesGroupsOrAndConditionsAnd() {
        FlowExpression expression = new SimpleFlowExpression();

        FlowProcessModel model = FlowContext.fromJson(FRONTEND_MODEL_JSON, FlowProcessModel.class);
        List<List<FlowCondition>> conditionList = ((ConditionRouterNodeModel) model.getNodeConfig().getChildNode()
                .getChildNode()).getConditionNodes().get(0).getConditionList();

        // 组 1 命中：reason contains 年假
        Map<String, Object> args1 = new HashMap<>();
        args1.put("reason", "请年假探亲");
        args1.put("days", 9);
        assertTrue(expression.eval(conditionList, args1));

        // 组 1 未命中，组 2 命中：days >= 3 且 days < 7（组内 AND）
        Map<String, Object> args2 = new HashMap<>();
        args2.put("reason", "事假");
        args2.put("days", 5);
        assertTrue(expression.eval(conditionList, args2));

        // 组 2 内一条不满足（days < 7 不成立），两组全不命中
        Map<String, Object> args3 = new HashMap<>();
        args3.put("reason", "事假");
        args3.put("days", 9);
        assertEquals(false, expression.eval(conditionList, args3));

        // 空条件组列表表示默认分支，直接命中
        assertTrue(expression.eval(null, args3));
        assertTrue(expression.eval(java.util.Collections.<List<FlowCondition>>emptyList(), args3));
    }
}
