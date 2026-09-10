package org.nkk.flow.example.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.core.extension.identity.DefaultFlowActorAccessStrategy;
import org.nkk.flow.core.extension.identity.FlowActorAccessStrategy;
import org.nkk.flow.core.extension.identity.FlowCreatorProvider;
import org.nkk.flow.enums.core.FlowFormFieldEnum.SourceType;
import org.nkk.flow.web.extension.FlowDesignerCategoryProvider;
import org.nkk.flow.web.extension.FlowDesignerFormProvider;
import org.nkk.flow.web.extension.FlowDesignerOrgProvider;
import org.nkk.flow.web.model.FlowDesignerCategoryNode;
import org.nkk.flow.web.model.FlowDesignerFormOption;
import org.nkk.flow.web.model.FlowDesignerOption;
import org.nkk.flow.web.model.FlowDesignerTreeNode;
import org.nkk.flow.model.FlowFieldMeta;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * flow-web 示例配置。
 */
@Configuration
public class FlowExampleWebConfiguration {

    /**
     * 统一参与人权限判断。
     *
     * <p>发起节点和审批节点都会使用该策略。示例中约定：
     * 用户 1 拥有角色 r1，并属于部门 d1。</p>
     */
    @Bean
    public FlowActorAccessStrategy flowActorAccessStrategy() {
        return new DefaultFlowActorAccessStrategy() {
            @Override
            protected boolean hasRole(String userId, String roleId) {
                return isRoleAllowed(userId, roleId);
            }

            @Override
            protected boolean inDepartment(String userId, String departmentId) {
                return isDepartmentAllowed(userId, departmentId);
            }
        };
    }

    /**
     * 提供当前流程操作人和系统操作人。
     *
     * <p>当前操作人用于流程发布接口写入创建人；系统操作人用于自动超时、触发器等无人操作场景。</p>
     */
    @Bean
    public FlowCreatorProvider flowCreatorProvider() {
        return new FlowCreatorProvider() {
            @Override
            public FlowCreator getCurrentCreator() {
                return FlowCreator.of("1", "系统管理员");
            }

            @Override
            public FlowCreator getSystemCreator() {
                return FlowCreator.of("0", "系统操作人");
            }
        };
    }

    private boolean isRoleAllowed(String userId, String roleId) {
        return "1".equals(userId) && "r1".equals(roleId);
    }

    private boolean isDepartmentAllowed(String userId, String departmentId) {
        return "1".equals(userId) && "d1".equals(departmentId);
    }

    /**
     * 提供设计器表单下拉和字段元数据。
     *
     * <p>覆盖 starter 默认的空实现，让设计器可以看到示例的自定义表单和业务表单。</p>
     */
    @Bean
    public FlowDesignerFormProvider flowDesignerFormProvider() {
        // ====== 表单元数据 ======
        Map<String, long[]> formMap = new LinkedHashMap<>(); // formKey → {formId, version}
        formMap.put("leave-form", new long[]{10001L, 1});
        formMap.put("expense-form", new long[]{10002L, 1});
        formMap.put("travel-form", new long[]{10003L, 1});
        formMap.put("ot-form", new long[]{10004L, 1});
        formMap.put("purchase-order", new long[]{20001L, 1});
        formMap.put("contract-approval", new long[]{20002L, 1});

        Map<String, String> formNameMap = new LinkedHashMap<>();
        formNameMap.put("leave-form", "请假表单");
        formNameMap.put("expense-form", "报销表单");
        formNameMap.put("travel-form", "出差表单");
        formNameMap.put("ot-form", "加班表单");
        formNameMap.put("purchase-order", "采购订单");
        formNameMap.put("contract-approval", "合同审批");

        Map<String, String> formSourceMap = new LinkedHashMap<>();
        formSourceMap.put("leave-form", SourceType.FORM.value());
        formSourceMap.put("expense-form", SourceType.FORM.value());
        formSourceMap.put("travel-form", SourceType.FORM.value());
        formSourceMap.put("ot-form", SourceType.FORM.value());
        formSourceMap.put("purchase-order", SourceType.BUSINESS.value());
        formSourceMap.put("contract-approval", SourceType.BUSINESS.value());

        Map<String, List<FlowFieldMeta>> fieldMap = new LinkedHashMap<>();
        fieldMap.put("leave-form", CollUtil.newArrayList(
                field("days", "请假天数", "number", 1, null, 1),
                field("reason", "请假原因", "string", 1, null, 2),
                field("type", "请假类型", "select", 1, "[{\"value\":\"1\",\"label\":\"年假\"},{\"value\":\"2\",\"label\":\"事假\"},{\"value\":\"3\",\"label\":\"病假\"},{\"value\":\"4\",\"label\":\"调休\"}]", 3),
                field("startDate", "开始日期", "date", 1, null, 4),
                field("endDate", "结束日期", "date", 1, null, 5),
                field("attachment", "附件", "string", 0, null, 6)
        ));
        fieldMap.put("expense-form", CollUtil.newArrayList(
                field("totalAmount", "报销金额", "number", 1, null, 1),
                field("category", "报销类别", "select", 1, "[{\"value\":\"1\",\"label\":\"交通费\"},{\"value\":\"2\",\"label\":\"餐饮费\"},{\"value\":\"3\",\"label\":\"住宿费\"},{\"value\":\"4\",\"label\":\"办公用品\"},{\"value\":\"5\",\"label\":\"其他\"}]", 2),
                field("reason", "报销事由", "string", 1, null, 3),
                field("expenseDate", "消费日期", "date", 1, null, 4),
                field("invoiceNo", "发票号", "string", 0, null, 5)
        ));
        fieldMap.put("travel-form", CollUtil.newArrayList(
                field("destination", "出差目的地", "string", 1, null, 1),
                field("startDate", "开始日期", "date", 1, null, 2),
                field("endDate", "结束日期", "date", 1, null, 3),
                field("budget", "预算金额", "number", 1, null, 4),
                field("purpose", "出差事由", "string", 1, null, 5),
                field("companions", "同行人", "multi_select", 0, null, 6)
        ));
        fieldMap.put("ot-form", CollUtil.newArrayList(
                field("otDate", "加班日期", "date", 1, null, 1),
                field("hours", "加班时长(小时)", "number", 1, null, 2),
                field("reason", "加班原因", "string", 1, null, 3),
                field("project", "项目名称", "string", 0, null, 4)
        ));
        fieldMap.put("purchase-order", CollUtil.newArrayList(
                field("orderNo", "订单编号", "string", 1, null, 1),
                field("supplier", "供应商", "string", 1, null, 2),
                field("totalAmount", "订单金额", "number", 1, null, 3),
                field("category", "采购类别", "select", 1, "[{\"value\":\"1\",\"label\":\"原材料\"},{\"value\":\"2\",\"label\":\"办公设备\"},{\"value\":\"3\",\"label\":\"IT设备\"},{\"value\":\"4\",\"label\":\"服务采购\"}]", 4),
                field("expectedDate", "预计到货日期", "date", 0, null, 5),
                field("urgent", "是否紧急", "boolean", 0, null, 6)
        ));
        fieldMap.put("contract-approval", CollUtil.newArrayList(
                field("contractNo", "合同编号", "string", 1, null, 1),
                field("contractName", "合同名称", "string", 1, null, 2),
                field("partyA", "甲方", "string", 1, null, 3),
                field("partyB", "乙方", "string", 1, null, 4),
                field("amount", "合同金额", "number", 1, null, 5),
                field("signDate", "签订日期", "date", 0, null, 6),
                field("expireDate", "到期日期", "date", 0, null, 7),
                field("contractType", "合同类型", "select", 1, "[{\"value\":\"1\",\"label\":\"采购合同\"},{\"value\":\"2\",\"label\":\"销售合同\"},{\"value\":\"3\",\"label\":\"服务合同\"},{\"value\":\"4\",\"label\":\"劳动合同\"}]", 8)
        ));

        return new FlowDesignerFormProvider() {
            @Override
            public List<FlowDesignerFormOption> listForms(String sourceType) {
                List<FlowDesignerFormOption> result = new ArrayList<>();
                for (Map.Entry<String, long[]> e : formMap.entrySet()) {
                    String formKey = e.getKey();
                    String actualSource = formSourceMap.get(formKey);
                    if (sourceType != null && !sourceType.equals(actualSource)) {
                        continue;
                    }
                    result.add(FlowDesignerFormOption.of(e.getValue()[0], formKey, formNameMap.get(formKey),
                            (int) e.getValue()[1], actualSource));
                }
                return result;
            }

            @Override
            public List<FlowFieldMeta> listFormFields(String formKey, Integer formVersion, String sourceType) {
                if (StrUtil.isBlank(formKey)) {
                    return Collections.emptyList();
                }
                List<FlowFieldMeta> fields = fieldMap.get(formKey);
                return fields == null ? Collections.emptyList() : new ArrayList<>(fields);
            }
        };
    }

    private static FlowFieldMeta field(String fieldKey, String fieldName, String fieldType,
                                       int required, String optionsJson, int sort) {
        FlowFieldMeta meta = new FlowFieldMeta();
        meta.setFieldKey(fieldKey);
        meta.setFieldName(fieldName);
        meta.setFieldType(fieldType);
        meta.setFieldPath(fieldKey);
        meta.setRequired(required);
        meta.setOptionsJson(optionsJson);
        meta.setSort(sort);
        return meta;
    }

    /**
     * 提供设计器组织选择数据。
     */
    @Bean
    public FlowDesignerOrgProvider flowDesignerOrgProvider() {
        return new FlowDesignerOrgProvider() {
            @Override
            public List<FlowDesignerTreeNode> listDepartmentTree() {
                FlowDesignerTreeNode root = FlowDesignerTreeNode.department("d1", null, "总部");

                FlowDesignerTreeNode dev = FlowDesignerTreeNode.department("d2", "d1", "研发部");
                FlowDesignerTreeNode finance = FlowDesignerTreeNode.department("d3", "d1", "财务部");
                FlowDesignerTreeNode hr = FlowDesignerTreeNode.department("d4", "d1", "人力资源部");
                FlowDesignerTreeNode market = FlowDesignerTreeNode.department("d5", "d1", "市场部");
                FlowDesignerTreeNode admin = FlowDesignerTreeNode.department("d6", "d1", "行政部");

                FlowDesignerTreeNode backend = FlowDesignerTreeNode.department("d7", "d2", "后端组");
                FlowDesignerTreeNode frontend = FlowDesignerTreeNode.department("d8", "d2", "前端组");
                FlowDesignerTreeNode sale = FlowDesignerTreeNode.department("d9", "d5", "销售组");
                FlowDesignerTreeNode brand = FlowDesignerTreeNode.department("d10", "d5", "品牌组");
                FlowDesignerTreeNode purchase = FlowDesignerTreeNode.department("d11", "d6", "采购组");

                dev.getChildren().add(backend);
                dev.getChildren().add(frontend);
                market.getChildren().add(sale);
                market.getChildren().add(brand);
                admin.getChildren().add(purchase);

                root.getChildren().add(dev);
                root.getChildren().add(finance);
                root.getChildren().add(hr);
                root.getChildren().add(market);
                root.getChildren().add(admin);

                return Collections.singletonList(root);
            }

            @Override
            public List<FlowDesignerTreeNode> listEmployeeTree() {
                FlowDesignerTreeNode root = FlowDesignerTreeNode.department("d1", null, "总部");
                root.setDisabled(true);

                FlowDesignerTreeNode dev = FlowDesignerTreeNode.department("d2", "d1", "研发部");
                dev.setDisabled(true);
                dev.getChildren().add(FlowDesignerTreeNode.employee("u1", "d2", "张三"));
                dev.getChildren().add(FlowDesignerTreeNode.employee("u2", "d2", "李四"));
                dev.getChildren().add(FlowDesignerTreeNode.employee("u3", "d2", "刘德华"));
                dev.getChildren().add(FlowDesignerTreeNode.employee("u4", "d2", "李天王"));
                dev.getChildren().add(FlowDesignerTreeNode.employee("u5", "d2", "孙悟空"));
                dev.getChildren().add(FlowDesignerTreeNode.employee("u6", "d2", "猪八戒"));
                dev.getChildren().add(FlowDesignerTreeNode.employee("u7", "d2", "沙和尚"));
                dev.getChildren().add(FlowDesignerTreeNode.employee("u8", "d2", "李靖"));
                dev.getChildren().add(FlowDesignerTreeNode.employee("u9", "d2", "牛魔王"));
                dev.getChildren().add(FlowDesignerTreeNode.employee("u10", "d2", "红孩儿"));

                FlowDesignerTreeNode finance = FlowDesignerTreeNode.department("d3", "d1", "财务部");
                finance.setDisabled(true);
                finance.getChildren().add(FlowDesignerTreeNode.employee("u11", "d3", "王五"));
                finance.getChildren().add(FlowDesignerTreeNode.employee("u12", "d3", "赵婷"));
                finance.getChildren().add(FlowDesignerTreeNode.employee("u13", "d3", "陈丽"));

                FlowDesignerTreeNode hr = FlowDesignerTreeNode.department("d4", "d1", "人力资源部");
                hr.setDisabled(true);
                hr.getChildren().add(FlowDesignerTreeNode.employee("u14", "d4", "周静"));
                hr.getChildren().add(FlowDesignerTreeNode.employee("u15", "d4", "吴涛"));

                FlowDesignerTreeNode market = FlowDesignerTreeNode.department("d5", "d1", "市场部");
                market.setDisabled(true);
                market.getChildren().add(FlowDesignerTreeNode.employee("u16", "d5", "郑浩"));
                market.getChildren().add(FlowDesignerTreeNode.employee("u17", "d5", "何欣"));
                market.getChildren().add(FlowDesignerTreeNode.employee("u18", "d5", "马明"));

                FlowDesignerTreeNode admin = FlowDesignerTreeNode.department("d6", "d1", "行政部");
                admin.setDisabled(true);
                admin.getChildren().add(FlowDesignerTreeNode.employee("u19", "d6", "宋佳"));
                admin.getChildren().add(FlowDesignerTreeNode.employee("u20", "d6", "黄强"));

                FlowDesignerTreeNode backend = FlowDesignerTreeNode.department("d7", "d2", "后端组");
                backend.setDisabled(true);
                backend.getChildren().add(FlowDesignerTreeNode.employee("u21", "d7", "林峰"));
                backend.getChildren().add(FlowDesignerTreeNode.employee("u22", "d7", "徐航"));

                FlowDesignerTreeNode frontend = FlowDesignerTreeNode.department("d8", "d2", "前端组");
                frontend.setDisabled(true);
                frontend.getChildren().add(FlowDesignerTreeNode.employee("u23", "d8", "苏瑶"));
                frontend.getChildren().add(FlowDesignerTreeNode.employee("u24", "d8", "江楠"));

                FlowDesignerTreeNode sale = FlowDesignerTreeNode.department("d9", "d5", "销售组");
                sale.setDisabled(true);
                sale.getChildren().add(FlowDesignerTreeNode.employee("u25", "d9", "高磊"));
                sale.getChildren().add(FlowDesignerTreeNode.employee("u26", "d9", "方雯"));

                FlowDesignerTreeNode brand = FlowDesignerTreeNode.department("d10", "d5", "品牌组");
                brand.setDisabled(true);
                brand.getChildren().add(FlowDesignerTreeNode.employee("u27", "d10", "钟琪"));

                FlowDesignerTreeNode purchase = FlowDesignerTreeNode.department("d11", "d6", "采购组");
                purchase.setDisabled(true);
                purchase.getChildren().add(FlowDesignerTreeNode.employee("u28", "d11", "崔鹏"));

                dev.getChildren().add(backend);
                dev.getChildren().add(frontend);
                market.getChildren().add(sale);
                market.getChildren().add(brand);
                admin.getChildren().add(purchase);

                root.getChildren().add(dev);
                root.getChildren().add(finance);
                root.getChildren().add(hr);
                root.getChildren().add(market);
                root.getChildren().add(admin);

                return Collections.singletonList(root);
            }

            @Override
            public List<FlowDesignerOption> listRoles() {
                return Arrays.asList(
                        FlowDesignerOption.role("r1", "部门负责人"),
                        FlowDesignerOption.role("r2", "财务审批"),
                        FlowDesignerOption.role("r3", "人事审批"),
                        FlowDesignerOption.role("r4", "采购审批"),
                        FlowDesignerOption.role("r5", "大数据审批"),
                        FlowDesignerOption.role("r6", "商务审批"),
                        FlowDesignerOption.role("r7", "技术审批"),
                        FlowDesignerOption.role("r8", "测试"),
                        FlowDesignerOption.role("r9", "管理员")
                );
            }
        };
    }

    /**
     * 提供设计器分类树。
     *
     * <p>覆盖 starter 默认的空实现，让设计器可以看到示例的流程分类。</p>
     */
    @Bean
    public FlowDesignerCategoryProvider flowDesignerCategoryProvider() {
        return new FlowDesignerCategoryProvider() {
            @Override
            public List<FlowDesignerCategoryNode> listCategoryTree() {
                return CollUtil.newArrayList(
                        FlowDesignerCategoryNode.leaf("c1-1-1", null, "人事审批"),
                        FlowDesignerCategoryNode.leaf("c1-1-2", null, "财务审批"),
                        FlowDesignerCategoryNode.leaf("c1-1-3", null, "合同审批"),
                        FlowDesignerCategoryNode.leaf("c1-1-4", null, "采购审批"),
                        FlowDesignerCategoryNode.leaf("c1-1-5", null, "行政审批")
                );
            }
        };
    }
}
