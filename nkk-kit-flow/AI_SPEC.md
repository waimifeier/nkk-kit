# nkk-kit-flow 后端规范约束（AI 开发指南）

> **本文档是 AI 工具在 nkk-kit-flow 核心模块进行开发、扩展、Bug 修复时的强制约束文件。**
> nkk-kit-flow 是审批流**引擎核心包**，独立于 Web 层，最终发布到 Maven 仓库。
> AI 在执行任何代码修改前，**必须先完整阅读本文件**。

---

## 一、模块定位

| 维度 | 决策 |
|------|------|
| Maven 坐标 | `org.nkk.kit:nkk-kit-flow` |
| 类型 | Spring Boot Starter（提供 autoconfigure） |
| 依赖 | nkk-kit-core / MyBatis-Plus / Jackson / Lombok |
| 职责 | 流程模型解析、流程引擎执行、实例/任务 CRUD、扩展点 SPI |
| 无依赖 | **不能** import Spring MVC / Controller 层 / Web 层 DTO |
| 发布目标 | Maven Central 或私有 Maven 仓库 |

### 依赖方向（严格单向）

```
nkk-kit-flow-web（Web 扩展层） → nkk-kit-flow（引擎核心） → nkk-kit-core（基础工具）
```

**nkk-kit-flow 绝对不能 import nkk-kit-flow-web 的任何类。** 扩展通过 extension 包下的 SPI 接口实现。

---

## 二、包结构与职责边界

```
org.nkk.flow
├── autoconfigure/           # Spring Boot 自动配置（Starter 入口）
│   ├── NkkFlowAutoConfiguration.java
│   ├── NkkFlowProperties.java           # 配置属性类
│   ├── NkkFlowMybatisPlusConfiguration.java
│   ├── SpringFlowInstanceListener.java  # 事件桥接（引擎事件 → Spring Event）
│   └── SpringFlowTaskListener.java
│
├── core/                              # 引擎核心运行时
│   ├── context/
│   │   ├── FlowContext.java           # 引擎上下文（扩展点接口聚合）
│   │   ├── FlowExecution.java         # 当前执行对象（跳转/驳回/终止 API）
│   │   ├── FlowCreator.java           # 实例创建上下文
│   │   └── FlowDataTransfer.java      # 流程变量传递
│   ├── engine/
│   │   └── FlowEngineBuilder.java     # 引擎 Builder（Spring 装配 Bean）
│   └── extension/                     # SPI 扩展点（业务侧实现）
│       ├── ai/FlowAiHandler.java
│       ├── cache/FlowModelCache.java
│       ├── condition/FlowConditionHandler.java + FlowExpression.java
│       ├── id/FlowIdGenerator.java
│       ├── identity/                  # 7 个策略接口（Start/Instance/Task 访问控制 + ActorProvider）
│       ├── json/FlowJsonHandler.java
│       ├── listener/                  # 监听器接口（Instance/Task/Node）
│       ├── schedule/                  # 调度（Trigger/Reminder/JobLock）
│       ├── subprocess/FlowSubProcessHandler.java
│       ├── task/FlowCreateTaskHandler.java + Interceptor
│       └── time/FlowCreateTimeHandler.java
│
├── dao/                               # DAO 接口 + MyBatis-Plus 实现
│   ├── FlowProcessDao.java            # 流程定义 DAO
│   ├── FlowInstanceDao.java           # 实例 DAO
│   ├── FlowTaskDao.java               # 任务 DAO
│   ├── FlowTaskActorDao.java          # 任务办理人 DAO
│   ├── FlowHisInstanceDao.java        # 历史实例 DAO
│   ├── FlowHisTaskDao.java            # 历史任务 DAO
│   ├── FlowHisTaskActorDao.java       # 历史办理人 DAO
│   ├── FlowExtInstanceDao.java        # 扩展实例 DAO
│   └── mybatis/                       # MyBatis-Plus 实现（默认实现，可替换）
│
├── entity/                            # MyBatis-Plus 实体（@TableName）
│   ├── FlowProcess.java               # 流程定义（model_content = FlowProcessModel JSON）
│   ├── FlowInstance.java              # 流程实例
│   ├── FlowTask.java                  # 运行时任务
│   ├── FlowTaskActor.java             # 任务参与者
│   ├── FlowHisInstance.java           # 历史实例
│   ├── FlowHisTask.java               # 历史任务
│   ├── FlowHisTaskActor.java          # 历史办理人
│   ├── FlowExtInstance.java           # 扩展实例（预留）
│   └── FlowEntity.java                # 公共基类
│
├── enums/                             # 枚举（业务契约，前端同步依赖）
│   ├── node/                          # ⚠️ 业务枚举，前后端共享
│   │   ├── FlowNodeTypeEnum.java      # 节点 type 值（前端 FlowProcessNodeType）
│   │   ├── FlowNodeSetTypeEnum.java   # setType 值（前端 setType）
│   │   └── FlowRejectStrategyEnum.java# rejectStrategy 值（前端 rejectStrategy）
│   ├── core/                          # 运行时枚举
│   │   ├── FlowProcessEnum.java       # ProcessState
│   │   ├── FlowInstanceEnum.java      # InstanceState
│   │   ├── FlowTaskEnum.java          # TaskType / PerformType / TaskState
│   │   └── FlowTaskActorEnum.java     # ActorType / AgentType
│   ├── runtime/                       # 运行时事件枚举
│   │   ├── FlowEventTypeEnum.java
│   │   ├── FlowExecuteTypeEnum.java
│   │   └── FlowInstanceOperateEnum.java
│   └── ai/                            # AI 扩展枚举
│       ├── FlowAiFallbackStrategyEnum.java
│       └── FlowAiStatusEnum.java
│
├── mapper/                            # MyBatis-Plus Mapper 接口
│   └── XxxMapper.java                 # 对应 Entity
│
├── model/                             # JSON 反序列化模型（业务数据契约）
│   ├── FlowProcessModel.java          # 流程定义顶层模型
│   ├── FlowNodeModel.java             # 节点模型（包含 execute 执行逻辑）
│   ├── FlowConditionNode.java         # 分支容器
│   ├── FlowCondition.java             # 条件表达式项
│   ├── FlowNodeAssignee.java          # 审批人/角色/部门
│   ├── FlowDynamicAssignee.java       # 动态解析后的办理人列表
│   ├── FlowAiConfig.java              # AI 节点配置（extendConfig.aiConfig）
│   ├── FlowAiResponse.java            # AI 执行结果
│   ├── FlowModelValidator.java        # 模型校验
│   └── FlowSignPolicy.java            # 票签策略
│
└── service/                           # Service 接口 + 实现
    ├── NkkFlowEngine.java             # ⭐ 引擎入口（start / approve / reject / rollback ...）
    ├── FlowProcessService.java        # 流程定义 CRUD
    ├── FlowRuntimeService.java        # 运行时操作（跳转/转交/委派/撤回）
    ├── FlowTaskService.java           # 任务操作（审批/拒绝/加签/减签）
    ├── FlowQueryService.java          # 查询（实例/任务/历史）
    └── impl/
        ├── NkkFlowEngineImpl.java
        ├── FlowProcessServiceImpl.java
        ├── FlowRuntimeServiceImpl.java
        ├── FlowTaskServiceImpl.java
        └── FlowQueryServiceImpl.java
```

---

## 三、不可触碰规则（硬约束）

### 🔴 规则 1：枚举值契约（前后端同步）

`enums/node/` 下的三个枚举是**前后端共享的业务契约**：

| 枚举 | 前端类型 | 已占用值 |
|------|---------|---------|
| `FlowNodeTypeEnum` | `FlowProcessNodeType` | -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 23, 30, 31 |
| `FlowNodeSetTypeEnum` | 前端 types 未完整覆盖 | 1, 2, 3, 4, 5, 7, 8 |
| `FlowRejectStrategyEnum` | `FlowProcessApprovalRejectStrategy` | 1, 2, 3, 4, 5（前端缺 5） |

**禁止：**
- ❌ 修改已有值的 `value`
- ❌ 删除已注册的枚举项
- ❌ 前端不经过后端同意就新增 type 值

**新增节点 type 的正确流程：**
1. 在后端 `FlowNodeTypeEnum` 新增枚举项，分配未占用数字
2. 在后端 `FlowNodeModel.executeInternal()` 添加对应的执行分支
3. 在前端 `types/process.ts` 扩展 `FlowProcessNodeType` 联合类型
4. 在 `JSON_SCHEMA.md` 第三节补充新 type 的完整字段定义
5. 在 `AI_SPEC.md` 第四节走完整 7 步注册

### 🔴 规则 2：DAO 与 Mapper 分层

```
Dao 接口（dao/FlowProcessDao.java）
  └── MyBatis-Plus 实现（dao/mybatis/FlowProcessDaoMybatisPlusImpl.java）
       └── 调用 Mapper（mapper/FlowProcessMapper.java）
```

**禁止直接在 Service 中注入 Mapper。** Service 层 → Dao 接口 → 实现 → Mapper。

### 🔴 规则 3：Entity 与 Model 职责分离

| 层 | 类 | 用途 | 注解 |
|----|---|------|------|
| 数据库层 | `entity/FlowProcess.java` | MyBatis-Plus 实体 | `@TableName("flow_process")` |
| JSON 层 | `model/FlowProcessModel.java` | 流程定义 JSON 反序列化 | `@JsonIgnoreProperties(ignoreUnknown = true)` |

**禁止在 Entity 中直接混入 JSON 模型字段**（如 `FlowNodeModel nodeConfig`）。`FlowProcessEntity.modelContent` 是 LongText 列，存 JSON 字符串，运行时通过 `FlowProcess.model()` 反序列化为 `FlowProcessModel`。

### 🔴 规则 4：不依赖 Spring MVC

nkk-kit-flow 是核心引擎包，**不能** import `org.springframework.web`、不能定义 Controller、不能依赖 Web Starter。Web 层能力由 nkk-kit-flow-web 提供。

### 🔴 规则 5：SPI 扩展点只定义接口

`core/extension/` 下的 SPI 接口**只做接口声明**，默认实现放在对应包下（如 `DefaultFlowConditionHandler`）。业务侧通过 Spring 容器注入自定义实现即可覆盖默认行为。

### 🔴 规则 6：@JsonIgnoreProperties(ignoreUnknown = true)

所有 `model/` 下的 JSON 模型类**必须**标注 `@JsonIgnoreProperties(ignoreUnknown = true)`，以兼容前端扩展字段和未来版本演进。

---

## 四、新增节点类型检查清单

当需要**新增一个节点 type**时，AI 必须按以下清单逐项完成：

### 4.1 后端枚举层

- [ ] 在 `FlowNodeTypeEnum` 新增枚举项，分配未占用数字 value
- [ ] 确定在 `FlowProcessNodeType` 联合类型中的对齐值
- [ ] 更新 `JSON_SCHEMA.md` 第三节 type 映射表

### 4.2 后端执行层

- [ ] 在 `FlowNodeModel.executeInternal()` 按 type 添加执行分支
- [ ] 如果是网关类节点（并行/包容/路由），在 `FlowNodeModel` 添加对应分支数组字段
- [ ] 如果需要条件判断，实现 `FlowConditionHandler` 接口的对应方法
- [ ] 如果需要子流程调用，使用 `FlowSubProcessHandler` 接口

### 4.3 前端同步

- [ ] 在 `FlowProcessNodeType` 扩展联合类型
- [ ] 在 `createNodeByType()` 添加 case
- [ ] 在 `getKind()` / `getAdvancedNodeKind()` 添加映射
- [ ] 在 `FLOW_NODE_SIZES` / `FLOW_NODE_ACCENTS` / `renderKindIcon()` 添加视觉
- [ ] 在 `defaultNodePickerOptions` 添加选项

### 4.4 文档同步

- [ ] 更新 `JSON_SCHEMA.md` 第三节 type 映射表
- [ ] 更新 `AI_SPEC.md` 第四节节点类型注册清单

---

## 五、命名规范

### 5.1 Java 类命名

| 对象 | 格式 | 示例 |
|------|------|------|
| Entity | `FlowXxx` | `FlowProcess`, `FlowTask` |
| Model（JSON） | `FlowXxxModel` | `FlowNodeModel`, `FlowConditionNode` |
| Dao 接口 | `FlowXxxDao` | `FlowProcessDao` |
| Dao 实现 | `FlowXxxDaoMybatisPlusImpl` | `FlowProcessDaoMybatisPlusImpl` |
| Mapper | `FlowXxxMapper` | `FlowProcessMapper` |
| Service 接口 | `FlowXxxService` / `NkkFlowEngine` | `FlowProcessService`, `NkkFlowEngine` |
| Service 实现 | `FlowXxxServiceImpl` / `NkkFlowEngineImpl` | 同上 |
| SPI 接口 | `FlowXxxHandler` / `FlowXxxStrategy` | `FlowConditionHandler`, `FlowActorAccessStrategy` |
| SPI 默认实现 | `DefaultFlowXxxHandler` | `DefaultFlowConditionHandler` |
| 枚举 | `FlowXxxEnum`（外层容器）+ `XxxType`/`XxxState`（内层枚举） | `FlowTaskEnum.TaskType` |

### 5.2 数据库表命名

| 表 | 说明 |
|----|------|
| `flow_process` | 流程定义 |
| `flow_instance` | 流程实例 |
| `flow_task` | 运行时任务 |
| `flow_task_actor` | 任务办理人 |
| `flow_his_instance` | 历史实例 |
| `flow_his_task` | 历史任务 |
| `flow_his_task_actor` | 历史办理人 |
| `flow_ext_instance` | 扩展实例（预留） |

### 5.3 IEnum 接口

所有枚举必须实现 `org.nkk.core.enums.common.IEnum<T>`：

```java
public enum FlowNodeTypeEnum implements IEnum<Integer> {
    START(0, "发起节点"),
    // ...
    ;
    private final Integer value;
    private final String label;

    @Override public Integer value() { return value; }
    @Override public String label() { return label; }

    public boolean eq(Integer v) { return this.value.equals(v); }
    public static FlowNodeTypeEnum of(Integer v) {
        return IEnum.resolveKeyOfNullable(FlowNodeTypeEnum.class, v);
    }
}
```

---

## 六、SPI 扩展点清单

| 接口 | 位置 | 作用 | 可被替换 |
|------|------|------|---------|
| `FlowConditionHandler` | core/extension/condition | 条件匹配逻辑（条件分支/包容路由/路由跳转） | ✅ |
| `FlowSubProcessHandler` | core/extension/subprocess | 子流程调用 | ✅ |
| `FlowAiHandler` | core/extension/ai | AI 节点处理 | ✅ |
| `FlowIdGenerator` | core/extension/id | 节点 ID 生成 | ✅ |
| `FlowJsonHandler` | core/extension/json | JSON 序列化/反序列化 | ✅ |
| `FlowModelCache` | core/extension/cache | 流程模型缓存 | ✅ |
| `FlowTaskCreateInterceptor` | core/extension/task | 任务创建拦截 | ✅ |
| `FlowCreateTaskHandler` | core/extension/task | 任务创建自定义 | ✅ |
| `FlowCreateTimeHandler` | core/extension/time | 创建时间来源 | ✅ |
| `FlowActorAccessStrategy` | core/extension/identity | 任务参与者访问控制 | ✅ |
| `FlowTaskActorProvider` | core/extension/identity | 运行时办理人解析 | ✅ |
| `FlowStartAccessStrategy` | core/extension/identity | 发起权限控制 | ✅ |
| `FlowInstanceAccessStrategy` | core/extension/identity | 实例访问权限 | ✅ |
| `FlowTaskAccessStrategy` | core/extension/identity | 任务访问权限 | ✅ |
| `FlowCreatorProvider` | core/extension/identity | 发起人信息获取 | ✅ |
| `FlowInstanceListener` | core/extension/listener | 实例生命周期监听 | ✅ |
| `FlowTaskListener` | core/extension/listener | 任务生命周期监听 | ✅ |
| `FlowNodeListener` | core/extension/listener | 节点执行监听 | ✅ |

---

## 七、发布前检查清单

- [ ] `mvn clean package -DskipTests` 编译无错误
- [ ] `mvn clean install` 成功安装到本地仓库
- [ ] `pom.xml` 中没有直接依赖 nkk-kit-flow-web 的 artifactId
- [ ] `enums/node/` 下的枚举值与前端 `FlowProcessNodeType` 等类型一致
- [ ] 新增枚举值时已同步更新 `JSON_SCHEMA.md`
- [ ] 新增 Entity 时已添加 `@TableName` 注解并确认 MySQL 表存在
- [ ] 新增 SPI 接口时已提供默认实现（`DefaultFlowXxx`）
- [ ] 所有 JSON 模型类都有 `@JsonIgnoreProperties(ignoreUnknown = true)`
- [ ] Service 层通过 Dao 接口访问数据库，没有直接注入 Mapper
- [ ] DAO 实现不引入任何 Web 层依赖

---

> **AI 工具注意**：收到后端功能需求时，先读本文件，再读 `JSON_SCHEMA.md` 的第三节（前后端对齐的 type 枚举和控制逻辑）。如果涉及新增节点 type，走完整的前后端同步流程。
