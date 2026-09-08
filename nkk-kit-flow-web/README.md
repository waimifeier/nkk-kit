# nkk-kit-flow-web

`nkk-kit-flow-web` 是审批流的 Web 扩展模块，不承载核心引擎和默认持久化。

当前边界：

- 流程设计器页面。
- 流程定义配置接口。
- 管理端流程模型发布、复制、启停等接口。
- 业务系统可选的审批页面接口和页面资源。

核心引擎、Spring Boot starter 自动配置、MyBatis-Plus 默认持久化和 SQL 模板放在 `nkk-kit-flow`。

## 自动配置

引入依赖后，Web 环境会自动装配 `NkkFlowWebAutoConfiguration`。

```xml
<dependency>
    <groupId>io.github.waimifeier</groupId>
    <artifactId>nkk-kit-flow-web</artifactId>
    <version>${nkk-kit.version}</version>
</dependency>
```

可选配置：

```yaml
nkk:
  flow:
    web:
      enabled: true
      api-prefix: /nkk/flow
```

## 设计器组织数据

前端设计器选择员工、部门、角色时，需要业务系统提供组织数据。`nkk-kit-flow-web` 只定义接口和默认 HTTP 入口，不内置组织表。

当前提供 3 个接口：

| 接口 | 说明 |
| --- | --- |
| `GET /nkk/flow/designer/org/departments/tree` | 部门树，只包含部门节点。 |
| `GET /nkk/flow/designer/org/employees/tree` | 员工树，通常是部门节点 + 员工叶子节点。 |
| `GET /nkk/flow/designer/org/roles` | 角色普通列表。 |

使用方实现 `FlowDesignerOrgProvider` Bean 即可：

```java
@Bean
public FlowDesignerOrgProvider flowDesignerOrgProvider() {
    return new FlowDesignerOrgProvider() {
        @Override
        public List<FlowDesignerTreeNode> listDepartmentTree() {
            FlowDesignerTreeNode root = FlowDesignerTreeNode.department("d1", null, "总部");
            root.getChildren().add(FlowDesignerTreeNode.department("d2", "d1", "研发部"));
            return Collections.singletonList(root);
        }

        @Override
        public List<FlowDesignerTreeNode> listEmployeeTree() {
            FlowDesignerTreeNode dept = FlowDesignerTreeNode.department("d2", "d1", "研发部");
            dept.setDisabled(true);
            dept.getChildren().add(FlowDesignerTreeNode.employee("u1", "d2", "张三"));
            return Collections.singletonList(dept);
        }

        @Override
        public List<FlowDesignerOption> listRoles() {
            return Collections.singletonList(FlowDesignerOption.role("r1", "部门负责人"));
        }
    };
}
```

返回模型约定：

- 部门节点：`actorType = 2`，对应核心任务参与者的部门。
- 员工节点：`actorType = 0`，对应核心任务参与者的用户。
- 角色选项：`actorType = 1`，对应核心任务参与者的角色。
- `extra` 字段预留给前端设计器使用，例如头像、编码、排序、是否外部人员等。

这组接口只解决“设计器选择数据”。真正发起流程时，节点选中的数据会进入流程 JSON；运行时发起人范围和任务办理权限都会委托给统一的 `FlowActorAccessStrategy`，`FlowStartAccessStrategy` 和 `FlowTaskAccessStrategy` 只是内部适配层。

## 表单字段元数据

流程设计器的条件节点、字段选择器可以直接查询表单字段元数据。字段元数据不再落独立表，统一走 `FlowDesignerFormProvider` SPI 查询，设计器条件分支和运行时表单渲染从流程模型 JSON 的 `extendConfig.metaForm.fields` 读取快照。

当前接口：

| 接口 | 说明 |
| --- | --- |
| `GET /nkk/flow/designer/forms/options` | 查询设计器可选表单下拉数据。 |
| `GET /nkk/flow/designer/forms/{formKey}/fields` | 查询指定表单的字段元数据；`formVersion` 为空时返回最新版本。 |


字段元数据约定：

- `fieldKey`：字段编码，设计器里用于唯一定位字段。
- `fieldName`：字段展示名。
- `fieldPath`：字段路径，适合处理嵌套对象或数组路径。
- `fieldType`：字段类型，例如 `string`、`number`、`date`、`datetime`。
- `sourceType`：字段来源，例如 `form`、`business`。
- `optionsJson`：选择型字段的选项数据。

表单下拉数据由 `FlowDesignerFormProvider` Bean 提供。使用方只要实现这个扩展点，就能把自定义表单、业务表单、表单版本等主数据喂给前端设计器。

示例：

```bash
curl "http://localhost:18080/nkk/flow/designer/forms/leave-form/fields?formVersion=1"
```

保存示例：

```json
{
  "tenantId": "tenant-001",
  "formId": 10001,
  "formVersion": 1,
  "fields": [
    {
      "fieldKey": "days",
      "fieldName": "请假天数",
      "fieldType": "number",
      "fieldPath": "days",
      "sourceType": "form",
      "required": 1,
      "sort": 1
    }
  ]
}
```

## 流程定义接口

流程定义接口用于前端设计器发布流程、启用/禁用流程、修改基础信息和查看版本记录。

当前提供接口：

| 接口 | 说明 |
| --- | --- |
| `POST /nkk/flow/designer/processes/publish` | 发布流程定义，生成新版本。 |
| `GET /nkk/flow/designer/processes/categories` | 按流程分类查询当前流程定义，不分页。 |
| `GET /nkk/flow/designer/processes/{processId}` | 查询流程定义详情。 |
| `POST /nkk/flow/designer/processes/{processId}/enable` | 启用流程定义。 |
| `POST /nkk/flow/designer/processes/{processId}/disable` | 禁用流程定义。 |
| `PUT /nkk/flow/designer/processes/{processId}/info` | 修改流程基础信息。 |
| `GET /nkk/flow/designer/processes/key/{processKey}/versions` | 查询流程版本记录。 |

发布流程请求示例：

```json
{
  "processName": "请假审批",
  "processKey": "leave",
  "processIcon": "icon-leave",
  "processType": "hr",
  "instanceUrl": "/flow/instances/{instanceId}",
  "remark": "员工请假审批流程",
  "repeat": true,
  "modelContent": {
    "nodeKey": "start",
    "nodeName": "发起人",
    "type": 0
  }
}
```

说明：

- `processName`、`processKey`、`modelContent` 必填。
- `modelContent` 支持 JSON 对象或 JSON 字符串。
- 推荐 `modelContent` 直接传流程根节点 `nodeConfig`，不需要再包一层 `key/name/nodeConfig`。
- 为兼容旧调用，`modelContent` 仍支持完整流程模型 `{ "key": "...", "name": "...", "nodeConfig": {...} }`。
- 发布时会以前端传入的 `processName/processKey/instanceUrl` 覆盖模型中的 `name/key/instanceUrl`，避免基础信息和流程 JSON 不一致。
- `repeat = true` 时，同一个 `processKey` 再次发布会生成新版本，并把旧版本标记为历史版本。
- 发布后会补充写入 `processIcon/processType/remark`。
- 发布人从 `FlowCreatorProvider` 获取，所以使用方需要在业务系统中注册该 Bean。

按分类查询当前流程：

```bash
curl http://localhost:18080/nkk/flow/designer/processes/categories
```

基础信息修改请求示例：

```json
{
  "processName": "请假审批",
  "processIcon": "icon-leave",
  "processType": "hr",
  "remark": "员工请假审批流程"
}
```

基础信息修改只允许修改：

- `processName`
- `processIcon`
- `processType`
- `remark`

不会修改：

- `processKey`
- `processVersion`
- `processState`
- `modelContent`

## 流程运行时接口

运行时接口用于业务侧发起流程、查询流程实例，以及对当前任务进行办理。

当前提供接口：

| 接口 | 说明 |
| --- | --- |
| `POST /nkk/flow/designer/runtime/instances/start` | 发起流程实例。 |
| `GET /nkk/flow/designer/runtime/instances/{instanceId}` | 查询流程实例详情。 |
| `POST /nkk/flow/designer/runtime/instances/{instanceId}/submit` | 提交草稿并继续流转。 |
| `GET /nkk/flow/designer/runtime/instances/{instanceId}/tasks` | 查询实例当前活动任务。 |
| `GET /nkk/flow/designer/runtime/instances/{instanceId}/history/tasks` | 查询实例历史任务。 |
| `POST /nkk/flow/designer/runtime/instances/{instanceId}/revoke` | 撤回流程实例。 |
| `POST /nkk/flow/designer/runtime/instances/{instanceId}/suspend` | 挂起流程实例。 |
| `POST /nkk/flow/designer/runtime/instances/{instanceId}/active` | 激活流程实例。 |
| `POST /nkk/flow/designer/runtime/instances/{instanceId}/destroy` | 作废流程实例。 |
| `GET /nkk/flow/designer/runtime/tasks/{taskId}` | 查询任务详情。 |
| `GET /nkk/flow/designer/runtime/tasks/{taskId}/actors` | 查询任务参与人。 |
| `POST /nkk/flow/designer/runtime/tasks/{taskId}/complete` | 办理通过任务。 |
| `POST /nkk/flow/designer/runtime/tasks/{taskId}/reject` | 驳回任务。 |
| `POST /nkk/flow/designer/runtime/tasks/{taskId}/transfer` | 转办任务。 |
| `POST /nkk/flow/designer/runtime/tasks/{taskId}/delegate` | 委托任务。 |
| `POST /nkk/flow/designer/runtime/tasks/{taskId}/resolve` | 办结委托任务。 |
| `POST /nkk/flow/designer/runtime/tasks/{taskId}/view` | 阅读抄送任务。 |
| `POST /nkk/flow/designer/runtime/tasks/{taskId}/terminate` | 终止任务所在流程。 |

发起流程示例：

```json
{
  "processKey": "leave",
  "businessKey": "leave-001",
  "saveAsDraft": false,
  "variables": {
    "day": 3,
    "reason": "事假"
  }
}
```

暂存草稿示例：

```json
{
  "processKey": "leave",
  "businessKey": "leave-001",
  "saveAsDraft": true,
  "variables": {
    "day": 3,
    "reason": "事假"
  }
}
```

提交草稿示例：

```json
{
  "variables": {
    "day": 5,
    "reason": "事假，补充修改"
  }
}
```

说明：

- `saveAsDraft = true` 只创建流程实例和历史实例快照，不会创建后续审批任务。
- 提交草稿时调用 `POST /nkk/flow/designer/runtime/instances/{instanceId}/submit`。
- 提交草稿会把历史实例状态从“暂存”改为“审批中”，并从草稿保存的当前节点继续执行。
- 提交请求里的 `variables` 会合并到草稿保存的流程变量中，可用于补全表单数据。

发起后通常要处理的操作：

- 查询实例详情，拿到当前任务和任务参与人。
- 当前办理人调用 `complete` 办理通过，或调用 `reject` 驳回。
- 特殊场景可调用 `transfer` 转办、`delegate` 委托、`terminate` 终止。
- 抄送任务调用 `view` 标记已读。
- 流程级控制可调用 `revoke`、`suspend`、`active`、`destroy`。

发起权限说明：

- 发起流程时会读取开始节点的 `nodeAssigneeList`。
- `nodeAssigneeList` 为空时，默认不限制发起人。
- `nodeAssigneeList` 不为空时，默认只允许列表中的用户 ID 发起。
- 如果开始节点或审批节点配置的是角色、部门、岗位等范围，使用方需要注册自定义 `FlowActorAccessStrategy` Bean 来查询自己的组织关系并完成判断。

## 无侵入式自动发起审批（@FlowApproval）

业务系统中保存订单后需要自动发起审批，常见做法是在业务代码里手动注入 `FlowDesignerRuntimeService` 并调用 `start()`。`@FlowApproval` 注解 + AOP 切面提供了更简洁的方式——**业务代码零侵入**，只需在方法上加一个注解，审批自动触发。

### 依赖

使用 `@FlowApproval` 需要确保 classpath 有 `spring-boot-starter-aop`：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

starter 用 `@ConditionalOnClass` 保护，没有 AOP 依赖时切面 Bean 不会创建，不会导致启动失败。

### 快速上手

**改造前（手动调用）：**

```java
@Service
public class OrderService {

    private final FlowDesignerRuntimeService runtimeService; // 注入审批引擎

    public Order submit(Order order) {
        Order saved = orderRepository.save(order);
        // ❌ 业务代码侵入审批引擎
        FlowStartProcessRequest req = new FlowStartProcessRequest();
        req.setProcessKey("purchase-order");
        req.setBusinessKey(saved.getOrderNo());
        req.setVariables(BeanUtil.beanToMap(saved));
        runtimeService.start(req);
        return saved;
    }
}
```

**改造后（注解自动触发）：**

```java
@Service
public class OrderService {

    // ✅ 不再注入审批引擎

    @Transactional
    @FlowApproval(processKey = "purchase-order", businessKeyFrom = "orderNo")
    public Order submit(Order order) {
        Order saved = orderRepository.save(order);
        // ✅ 业务代码纯净，审批由 AOP 自动发起
        return saved;
    }
}
```

### 注解参数

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `processKey` | `""` | 流程 key，与 `processId` 二选一 |
| `processId` | `0` | 流程定义 ID，优先级最高 |
| `processVersion` | `-1` | 流程版本，-1 表示最新启用版本 |
| `businessKeyFrom` | `""` | 从返回值中提取 businessKey 的属性名，例如 `"orderNo"` → `order.getOrderNo()` |
| `afterCommit` | `true` | 事务提交后才启动流程（推荐），避免审批引擎读不到业务数据 |
| `successOn` | `NON_NULL` | 返回值成功判定条件 |

### businessKey 和 variables 提取规则

**businessKey 提取优先级：**

1. `@FlowBusinessKey` 标注的方法参数值
2. 返回值的 `businessKeyFrom` 属性（如果配置了）
3. 返回值的 `toString()`

**variables 提取优先级：**

1. `@FlowVariables` 标注的 `Map<String, Object>` 参数
2. 返回值的所有可序列化属性（JavaBean 属性）

### 参数注解

| 注解 | 位置 | 说明 |
| --- | --- | --- |
| `@FlowBusinessKey` | 方法参数 | 标记哪个参数是审批 engine 关联业务单据的 businessKey |
| `@FlowVariables` | 方法参数 | 标记哪个 `Map<String, Object>` 参数是流程变量 |

### 四种使用模式

```java
// 1. 返回值提取 businessKey + 自动收集 variables
@Transactional
@FlowApproval(processKey = "purchase-order", businessKeyFrom = "orderNo")
public Order submitPurchase(Order order) {
    return orderRepository.save(order);
}

// 2. 显式指定 businessKey 参数
@Transactional
@FlowApproval(processKey = "leave")
public Order submitLeave(@FlowBusinessKey String orderNo, LeaveForm form) {
    Order order = orderRepository.save(new Order(orderNo, form));
    return order;
}

// 3. 显式指定 businessKey + variables
@Transactional
@FlowApproval(processKey = "expense")
public Order submitExpense(@FlowBusinessKey String orderNo,
                           @FlowVariables Map<String, Object> vars) {
    Order order = orderRepository.save(new Order(orderNo, vars));
    return order;
}

// 4. 无事务场景 + 忽略返回值
@FlowApproval(processKey = "expense", afterCommit = false, successOn = ReturnCondition.ALWAYS)
public void submitExpenseNoTx(Order order) {
    orderRepository.save(order);
}
```

### 返回值成功判定

| `successOn` | 触发条件 | 典型场景 |
| --- | --- | --- |
| `NON_NULL`（默认） | 返回值非 null | 业务校验失败返回 null 时不触发 |
| `TRUE` | `boolean/Boolean` 返回 true | boolean 返回值控制是否触发 |
| `VOID_OR_NULL` | void 方法或返回 null | void 方法正常完成后触发 |
| `ALWAYS` | 方法正常返回（无异常） | 无事务场景，始终触发 |

### 为什么 afterCommit=true（默认）？

这是最关键的设计决策——**业务数据先持久化，审批再启动**：

```
afterCommit=false 的问题：
  OrderService.submit() → 事务还没提交
    → runtimeService.start() → 审批引擎查不到 businessKey=PO-001 的订单
    → 审批回调 updateStatus() 失败
    → 事务回滚，但流程已经启动了（孤儿流程实例）

afterCommit=true 的安全链路：
  OrderService.submit() → 事务提交 ✅
    → afterCommit 回调触发
    → runtimeService.start() → 审批引擎能查到 PO-001 ✅
```

实现方式：`TransactionSynchronizationManager.registerSynchronization().afterCommit()`，有事务就注册回调，没事务就直接执行。

### 切面失败不影响业务

审批启动失败不应该让业务方法回滚——订单已经保存成功了，只是审批没启动。切面 catch 异常后只打 ERROR 日志，业务照常返回。用户可以从日志里发现审批启动失败的情况再处理。

### 完整接入链路（业务表单场景）

```
┌──────────────────────────────────────────────────────┐
│ 1. 业务开发：加 @FlowApproval                         │
│    @Transactional                                    │
│    @FlowApproval(processKey = "purchase-order",      │
│                  businessKeyFrom = "orderNo")         │
│    public Order submit(Order order) { return repo.save(order); } │
└──────────────────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────────────────┐
│ 2. 业务执行                                          │
│    orderRepository.save(order) → 事务提交             │
└──────────────────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────────────────┐
│ 3. AOP 切面（afterCommit 回调）                       │
│    → runtimeService.start({                           │
│        processKey: "purchase-order",                 │
│        businessKey: "PO-001",         ← 从返回值提取 │
│        variables: {orderNo, amount, ...} ← 自动收集  │
│      })                                              │
└──────────────────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────────────────┐
│ 4. 审批回调（业务系统实现 FlowApprovalCallbackHandler）│
│    onInstanceComplete → orderRepository.updateStatus("APPROVED") │
│    onInstanceReject   → orderRepository.updateStatus("REJECTED") │
└──────────────────────────────────────────────────────┘
```

业务系统只做两件事：**加注解** + **实现回调 Handler**。审批引擎的启动、流转、通知全部自动处理。

### 审批回调 SPI

审批引擎在流程实例/任务的关键生命周期节点触发回调，业务系统通过实现 `FlowApprovalCallbackHandler` 同步自己的单据状态。

#### 业务表需要加的字段

```sql
ALTER TABLE t_order ADD COLUMN flow_instance_id BIGINT COMMENT '当前审批实例ID';
ALTER TABLE t_order ADD COLUMN flow_status VARCHAR(32) DEFAULT 'NONE'
    COMMENT '审批状态：NONE/PENDING/APPROVED/REJECTED/REVOKED/TERMINATED';
```

| 时机 | flow_instance_id | flow_status | 在哪个回调做 |
| --- | --- | --- | --- |
| `onInstanceStart` | 更新为新的 instanceId | → PENDING | ✅ 驳回重提时覆盖旧 instanceId |
| `onInstanceComplete` | 不变 | → APPROVED | ✅ |
| `onInstanceReject` | 不变 | → REJECTED | ✅ |
| `onInstanceRevoke` | 不变 | → REVOKED | ✅ |
| `onInstanceTerminate` | 不变 | → TERMINATED | ✅ |

#### 推荐模式：Service 直接实现

让业务 Service 同时承担两个角色——发起审批（`@FlowApproval`）+ 接收回调（`FlowApprovalCallbackHandler`）。Service 本身就是 Spring Bean，已经注入了自己的 Repository，代码最内聚：

```java
@Service
public class OrderService implements FlowApprovalCallbackHandler {

    private final OrderRepository orderRepository;

    // ---- 1. 发起审批 ----

    @Transactional
    @FlowApproval(processKey = "purchase-order", businessKeyFrom = "orderNo")
    public Order submit(Order order) {
        return orderRepository.save(order);  // 事务提交后自动发起
    }

    // ---- 2. 接收回调 ----

    /** 只处理订单审批流程。 */
    @Override
    public boolean supports(String processKey) {
        return "purchase-order".equals(processKey);
    }

    @Override
    public void onInstanceStart(FlowHisInstance instance, Map<String, Object> variables) {
        orderRepository.updateFlowInstance(instance.getBusinessKey(), instance.getId(), "PENDING");
    }

    @Override
    public void onInstanceComplete(FlowHisInstance instance) {
        orderRepository.updateFlowStatus(instance.getBusinessKey(), "APPROVED");
    }

    @Override
    public void onInstanceReject(FlowHisInstance instance) {
        orderRepository.updateFlowStatus(instance.getBusinessKey(), "REJECTED");
    }

    @Override
    public void onInstanceRevoke(FlowHisInstance instance) {
        orderRepository.updateFlowStatus(instance.getBusinessKey(), "REVOKED");
    }

    @Override
    public void onInstanceTerminate(FlowHisInstance instance) {
        orderRepository.updateFlowStatus(instance.getBusinessKey(), "TERMINATED");
    }
}
```

#### 备选模式：独立 Handler 类

如果不想让 Service 类实现太多接口，可以单独建 Handler：

```java
@Component
public class OrderApprovalCallback implements FlowApprovalCallbackHandler {

    private final OrderRepository orderRepository;

    @Override
    public boolean supports(String processKey) {
        return "purchase-order".equals(processKey);
    }

    @Override
    public void onInstanceComplete(FlowHisInstance instance) {
        orderRepository.updateFlowStatus(instance.getBusinessKey(), "APPROVED");
    }
    // ...
}
```

#### supports 路由机制

`supports(String processKey)` 是适配器的路由过滤器——只有返回 true 的 Handler 才会收到回调。

| supports 返回 | 效果 | 适用场景 |
| --- | --- | --- |
| `return "purchase-order".equals(processKey);` | 只处理订单审批 | 每个业务一个 Handler，职责单一 |
| `return "purchase-order".equals(processKey) \|\| "leave".equals(processKey);` | 处理多个流程 | 一个 Service 管理多种业务单据 |
| `return true;`（默认） | 接收所有回调 | 单 Handler 内部按 processKey 分发 |

回调方法列表：

| 方法 | 触发时机 |
| --- | --- |
| `onInstanceStart(instance, variables)` | 流程实例发起后 |
| `onInstanceComplete(instance)` | 审批通过 |
| `onInstanceReject(instance)` | 审批拒绝 |
| `onInstanceRevoke(instance)` | 发起人撤回 |
| `onInstanceTerminate(instance)` | 超时/强制终止 |
| `onTaskComplete(task, actors)` | 任务完成（通过/拒绝/转交等） |
| `onTaskCreate(task, actors)` | 任务创建 |

可以有多个 Handler Bean，适配器会按 order 值从小到大逐个触发。某个 Handler 抛异常只会打印 ERROR 日志，不影响其他 Handler 和主流程。
