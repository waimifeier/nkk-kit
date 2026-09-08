package org.nkk.flow.example.support;

import cn.hutool.core.collection.CollUtil;
import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.core.extension.identity.DefaultFlowActorAccessStrategy;
import org.nkk.flow.core.extension.identity.FlowActorAccessStrategy;
import org.nkk.flow.core.extension.identity.FlowCreatorProvider;
import org.nkk.flow.core.extension.identity.FlowInstanceAccessStrategy;
import org.nkk.flow.entity.FlowHisInstance;
import org.nkk.flow.entity.FlowInstance;
import org.nkk.flow.enums.runtime.FlowInstanceOperateEnum;
import org.nkk.flow.web.extension.FlowDesignerOrgProvider;
import org.nkk.flow.web.model.FlowDesignerCategoryNode;
import org.nkk.flow.web.model.FlowDesignerOption;
import org.nkk.flow.web.model.FlowDesignerTreeNode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * flow-web 示例配置。
 */
@Configuration
public class FlowExampleWebConfiguration {

    /**
     * 实例级操作权限判断。
     *
     * <p>该策略只处理整个流程实例的管理类操作，不处理具体审批任务的办理权限。</p>
     *
     * <p>示例规则：</p>
     * <ul>
     *     <li>撤回流程：只允许流程发起人操作。</li>
     *     <li>挂起、激活、终止、作废等管理操作：只允许示例管理员操作。</li>
     * </ul>
     */
    @Bean
    public FlowInstanceAccessStrategy flowInstanceAccessStrategy() {
        return (creator, instance, hisInstance, operateType) -> {
            if (creator == null || creator.getCreateId() == null) {
                return false;
            }
            if (FlowInstanceOperateEnum.REVOKE == operateType) {
                return isCreator(creator, instance, hisInstance);
            }
            return isAdmin(creator);
        };
    }
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
     * 提供当前流程操作人，供流程发布接口写入创建人。
     */
    @Bean
    public FlowCreatorProvider flowCreatorProvider() {
        return () -> FlowCreator.of("1", "系统管理员");
    }

    private boolean isCreator(FlowCreator creator, FlowInstance instance, FlowHisInstance hisInstance) {
        String createId = instance == null ? null : instance.getCreateId();
        if (createId == null && hisInstance != null) {
            createId = hisInstance.getCreateId();
        }
        return Objects.equals(creator.getCreateId(), createId);
    }

    private boolean isAdmin(FlowCreator creator) {
        return "1".equals(creator.getCreateId());
    }

    private boolean isRoleAllowed(String userId, String roleId) {
        return "1".equals(userId) && "r1".equals(roleId);
    }

    private boolean isDepartmentAllowed(String userId, String departmentId) {
        return "1".equals(userId) && "d1".equals(departmentId);
    }

    /**
     * 提供设计器组织选择数据。
     */
    @Bean
    public FlowDesignerOrgProvider flowDesignerOrgProvider() {
        return new FlowDesignerOrgProvider() {
            @Override
            public List<FlowDesignerTreeNode> listDepartmentTree() {
                // 根节点
                FlowDesignerTreeNode root = FlowDesignerTreeNode.department("d1", null, "总部");

                // 一级部门（总部下属）
                FlowDesignerTreeNode dev = FlowDesignerTreeNode.department("d2", "d1", "研发部");
                FlowDesignerTreeNode finance = FlowDesignerTreeNode.department("d3", "d1", "财务部");
                FlowDesignerTreeNode hr = FlowDesignerTreeNode.department("d4", "d1", "人力资源部");
                FlowDesignerTreeNode market = FlowDesignerTreeNode.department("d5", "d1", "市场部");
                FlowDesignerTreeNode admin = FlowDesignerTreeNode.department("d6", "d1", "行政部");

                // 二级部门
                FlowDesignerTreeNode backend = FlowDesignerTreeNode.department("d7", "d2", "后端组");
                FlowDesignerTreeNode frontend = FlowDesignerTreeNode.department("d8", "d2", "前端组");
                FlowDesignerTreeNode sale = FlowDesignerTreeNode.department("d9", "d5", "销售组");
                FlowDesignerTreeNode brand = FlowDesignerTreeNode.department("d10", "d5", "品牌组");
                FlowDesignerTreeNode purchase = FlowDesignerTreeNode.department("d11", "d6", "采购组");

                // 组装树结构
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

                // 研发部
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

                // 财务部
                FlowDesignerTreeNode finance = FlowDesignerTreeNode.department("d3", "d1", "财务部");
                finance.setDisabled(true);
                finance.getChildren().add(FlowDesignerTreeNode.employee("u11", "d3", "王五"));
                finance.getChildren().add(FlowDesignerTreeNode.employee("u12", "d3", "赵婷"));
                finance.getChildren().add(FlowDesignerTreeNode.employee("u13", "d3", "陈丽"));

                // 人力资源部
                FlowDesignerTreeNode hr = FlowDesignerTreeNode.department("d4", "d1", "人力资源部");
                hr.setDisabled(true);
                hr.getChildren().add(FlowDesignerTreeNode.employee("u14", "d4", "周静"));
                hr.getChildren().add(FlowDesignerTreeNode.employee("u15", "d4", "吴涛"));

                // 市场部
                FlowDesignerTreeNode market = FlowDesignerTreeNode.department("d5", "d1", "市场部");
                market.setDisabled(true);
                market.getChildren().add(FlowDesignerTreeNode.employee("u16", "d5", "郑浩"));
                market.getChildren().add(FlowDesignerTreeNode.employee("u17", "d5", "何欣"));
                market.getChildren().add(FlowDesignerTreeNode.employee("u18", "d5", "马明"));

                // 行政部
                FlowDesignerTreeNode admin = FlowDesignerTreeNode.department("d6", "d1", "行政部");
                admin.setDisabled(true);
                admin.getChildren().add(FlowDesignerTreeNode.employee("u19", "d6", "宋佳"));
                admin.getChildren().add(FlowDesignerTreeNode.employee("u20", "d6", "黄强"));

                // 研发部二级：后端组
                FlowDesignerTreeNode backend = FlowDesignerTreeNode.department("d7", "d2", "后端组");
                backend.setDisabled(true);
                backend.getChildren().add(FlowDesignerTreeNode.employee("u21", "d7", "林峰"));
                backend.getChildren().add(FlowDesignerTreeNode.employee("u22", "d7", "徐航"));

                // 研发部二级：前端组
                FlowDesignerTreeNode frontend = FlowDesignerTreeNode.department("d8", "d2", "前端组");
                frontend.setDisabled(true);
                frontend.getChildren().add(FlowDesignerTreeNode.employee("u23", "d8", "苏瑶"));
                frontend.getChildren().add(FlowDesignerTreeNode.employee("u24", "d8", "江楠"));

                // 市场部二级：销售组
                FlowDesignerTreeNode sale = FlowDesignerTreeNode.department("d9", "d5", "销售组");
                sale.setDisabled(true);
                sale.getChildren().add(FlowDesignerTreeNode.employee("u25", "d9", "高磊"));
                sale.getChildren().add(FlowDesignerTreeNode.employee("u26", "d9", "方雯"));

                // 市场部二级：品牌组
                FlowDesignerTreeNode brand = FlowDesignerTreeNode.department("d10", "d5", "品牌组");
                brand.setDisabled(true);
                brand.getChildren().add(FlowDesignerTreeNode.employee("u27", "d10", "钟琪"));

                // 行政部二级：采购组
                FlowDesignerTreeNode purchase = FlowDesignerTreeNode.department("d11", "d6", "采购组");
                purchase.setDisabled(true);
                purchase.getChildren().add(FlowDesignerTreeNode.employee("u28", "d11", "崔鹏"));

                // 二级部门挂载到父部门
                dev.getChildren().add(backend);
                dev.getChildren().add(frontend);
                market.getChildren().add(sale);
                market.getChildren().add(brand);
                admin.getChildren().add(purchase);

                // 一级部门挂载到总部
                root.getChildren().add(dev);
                root.getChildren().add(finance);
                root.getChildren().add(hr);
                root.getChildren().add(market);
                root.getChildren().add(admin);

                return Collections.singletonList(root);
            }


            @Override
            public List<FlowDesignerCategoryNode> listCategoryTree() {
                return CollUtil.newArrayList(
                        FlowDesignerCategoryNode.leaf("c1-1-1", null, "人事审批"),
                        FlowDesignerCategoryNode.leaf("c1-1-2", null, "财务审批"),
                        FlowDesignerCategoryNode.leaf("c1-1-3", null, "合同审批")
                );
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
}
