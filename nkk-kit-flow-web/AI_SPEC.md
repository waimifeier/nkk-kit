# nkk-kit-flow-web 后端规范约束（AI 开发指南）

> **本文档是 AI 工具在 nkk-kit-flow-web Web 扩展层进行开发时的强制约束文件。**
> nkk-kit-flow-web 是审批流的 **REST API 层**，为前端 nkk-flow（Vue）提供 HTTP 接口，同时提供 Web 扩展（流程设计器、运行时审批操作等）。
> AI 在执行任何代码修改前，**必须先完整阅读本文件** + `nkk-kit-flow/AI_SPEC.md` + `JSON_SCHEMA.md`。

---

## 一、模块定位

| 维度 | 决策 |
|------|------|
| Maven 坐标 | `org.nkk.kit:nkk-kit-flow-web` |
| 类型 | Spring Boot Starter（提供 autoconfigure + Controller） |
| 核心依赖 | **nkk-kit-flow**（引擎核心）+ Spring WebMVC + Jackson + Lombok |
| 职责 | REST API Controller + Service 组装 + DTO 转换 + 前端对接 |
| 不可依赖方向 | **不能被 nkk-kit-flow import** |
| 发布目标 | Maven 仓库 |

### 依赖方向

```
前端 nkk-flow（Vue）  ←HTTP→  nkk-kit-flow-web  →  nkk-kit-flow  →  nkk-kit-core
```

nkk-kit-flow-web 是唯一的 HTTP 入口层，它**编排**核心引擎的 SPI 实现、**暴露** REST API、**转换** DTO ↔ JSON 模型。

---

## 二、包结构与职责边界

```
org.nkk.flow.web
├── autoconfigure/
│   └── NkkFlowWebAutoConfiguration.java   # Web 层自动配置（注册 Controller/SPI 实现）
│
├── controller/                            # REST API 入口
│   ├── FlowDesignerProcessController.java # 流程设计/发布（前端 nkk-flow 调用）
│   ├── FlowDesignerRuntimeController.java # 运行时审批操作（审批/拒绝/转交/加签/撤回）
│   ├── FlowDesignerTodoController.java    # 待办/已办查询
│   └── FlowDesignerOrgController.java    # 组织架构/人员选择器
│
├── extension/                             # 默认 SPI 实现（覆盖 nkk-kit-flow 的 DefaultXxx）
│   ├── FlowDesignerOrgProvider.java       # 组织架构数据源接口
│   └── DefaultFlowDesignerOrgProvider.java# 默认实现（业务侧替换）
│
├── model/                                 # 请求/响应 DTO
│   ├── FlowProcessPublishRequest.java     # 发布流程定义请求（含 modelContent JSON）
│   ├── FlowProcessInfoUpdateRequest.java
│   ├── FlowProcessCategoryResponse.java
│   ├── FlowStartProcessRequest.java       # 发起流程实例请求
│   ├── FlowRuntimeResponse.java           # 运行时通用响应
│   ├── FlowTaskOperateRequest.java        # 审批/拒绝通用请求
│   ├── FlowTaskAssignRequest.java         # 转办请求
│   ├── FlowTaskActorRequest.java          # 委派/代理请求
│   ├── FlowTaskAddSignRequest.java        # 加签请求
│   ├── FlowTaskRemoveSignRequest.java     # 减签请求
│   ├── FlowTaskRollbackRequest.java       # 回退请求
│   ├── FlowTaskCopyRequest.java           # 抄送请求
│   ├── FlowInstanceDetailResponse.java    # 实例详情
│   ├── FlowInstanceApprovalRecordResponse.java
│   ├── FlowTodoRecordResponse.java         # 待办/已办列表项
│   ├── FlowTodoTypeEnum.java
│   ├── FlowDesignerTreeNode.java          # 设计器树形组件数据
│   ├── FlowDesignerOption.java            # 设计器下拉选项数据
│   └── FlowTaskActorParam.java
│
└── service/                               # Web 层 Service（编排 + DTO 转换）
    ├── FlowDesignerProcessService.java
    ├── FlowDesignerRuntimeService.java
    ├── FlowDesignerTodoService.java
    ├── FlowDesignerOrgService.java
    └── impl/                              # 实现类（可选，直接放 service 也行）
```

---

## 三、前后端数据契约

### 3.1 前端发布流程 → 后端入参

前端 nkk-flow 调用 `POST /flow/process/publish`，请求体结构：

```typescript
// 前端 nkk-flow 发送的结构
FlowProcessPublishRequest = {
  key: string,                    // 流程唯一编码
  name: string,                   // 流程名称
  icon?: string,                  // 图标
  type?: string,                  // 分类
  remark?: string,                // 说明
  instanceUrl?: string,           // 实例详情页
  extendConfig?: object,          // 流程级扩展
  modelContent: FlowProcessJson,  // ⚠️ 就是 FlowNodeModel 的 JSON （StartNode 根节点）
}
```

后端 `FlowDesignerProcessService` 接收 → 构造 `FlowProcessModel`（包装 modelContent 为 `nodeConfig`）→ 存入 `flow_process.model_content`。

### 3.2 后端返回流程定义 → 前端解析

后端从 DB 取出 `flow_process.model_content` → `FlowProcess.model()` 反序列化为 `FlowProcessModel` → 返回时只取 `nodeConfig` → 前端直接用 `FlowProcessJson` 渲染画布。

### 3.3 字段对齐检查清单

| 检查项 | 说明 |
|--------|------|
| type 数字值 | 前端生成的 type 值必须在后端 `FlowNodeTypeEnum` 中存在 |
| 审批配置字段 | `setType` / `examineMode` / `rejectStrategy` 前端生成时建议用数字值（后端枚举） |
| rejectStrategy | 前端兼容字符串，但后端只认数字值。发布时后端统一转数字 |
| actorType in assignee | 前端建议补齐 `actorType`，后端需要此字段区分 user/role/department |
| conditionList 二维格式 | 前端发送二维，后端 `FlowConditionNode.setConditionList()` 自动归一化为一维 |
| 节点 JSON 格式 | `nodeConfig` 必须是 type=0 StartNode 开头的完整树，链路最终到 type=-1 EndNode |

---

## 四、不可触碰规则（硬约束）

### 🔴 规则 1：Controller 只做两件事

Controller 层职责：**参数校验 + 调用 Service + 返回响应**。

```java
// ✅ 正确
@PostMapping("/publish")
public R<FlowProcess> publish(@RequestBody @Valid FlowProcessPublishRequest req) {
    return R.ok(flowDesignerProcessService.publish(req));
}

// ❌ 禁止：Controller 里写业务逻辑
@PostMapping("/publish")
public R<...> publish(...) {
    FlowNodeModel root = FlowContext.fromJson(req.getModelContent(), FlowNodeModel.class);
    // ❌ 业务逻辑移到 Service
    FlowProcessModel model = new FlowProcessModel();
    model.setNodeConfig(root);
    ...
}
```

### 🔴 规则 2：Service 层做 DTO ↔ Model 转换

```
请求 DTO → Service → FlowProcessModel/FlowNodeModel → Dao → DB
DB → Dao → Entity → Service → 响应 DTO
```

转换逻辑统一在 Service 层完成，Controller 和 DAO 都不做转换。

### 🔴 规则 3：不直接暴露 Entity

Controller 响应**不能**直接返回 Entity（`FlowProcess` / `FlowInstance` / `FlowTask` 等），必须用 Response DTO（`FlowInstanceDetailResponse` / `FlowTodoRecordResponse` 等）。

### 🔴 规则 4：JSON Schema 是权威来源

前后端字段对齐的唯一权威是 `JSON_SCHEMA.md`。新增字段时：
1. 后端 `FlowNodeModel.java` 加字段 → 同步 `JSON_SCHEMA.md`
2. 前端 `types/process.ts` 加类型 → 同步 `JSON_SCHEMA.md`
3. 中间 DTO（如 `FlowProcessPublishRequest`）**直接引用后端 Model**，不要重复定义

### 🔴 规则 5：统一响应格式

所有 Controller 返回统一包装 `R<T>`（nkk-kit-core 提供）：

```java
R.ok(data)      // 成功
R.fail(msg)     // 失败
R.of(boolean)   // 条件式
```

**禁止**返回裸对象、`ResponseEntity`、`Map<String, Object>` 等非标准格式。

---

## 五、REST API 设计规范

### 5.1 路径前缀

所有 Controller 路径以 `/flow` 为前缀：

```
/flow/process/**      流程定义管理（设计器）
/flow/runtime/**      运行时审批操作
/flow/todo/**         待办/已办
/flow/org/**          组织架构数据源
```

### 5.2 Controller 对照表

| Controller | Base Path | 职责 |
|-----------|-----------|------|
| `FlowDesignerProcessController` | `/flow/process` | 发布/更新/删除/查询流程定义 |
| `FlowDesignerRuntimeController` | `/flow/runtime` | 审批/拒绝/转交/委派/加签/减签/撤回/回退/启动 |
| `FlowDesignerTodoController` | `/flow/todo` | 待办/已办/我的发起 |
| `FlowDesignerOrgController` | `/flow/org` | 人员选择器/角色列表/部门树 |

### 5.3 HTTP 方法使用

| 操作 | HTTP | 路径 |
|------|------|------|
| 创建/发布 | POST | `/flow/process/publish` |
| 查询列表 | GET | `/flow/process/list` |
| 查询详情 | GET | `/flow/process/{key}` |
| 更新 | PUT | `/flow/process/{key}` |
| 删除 | DELETE | `/flow/process/{key}` |
| 启动实例 | POST | `/flow/runtime/start` |
| 审批通过 | POST | `/flow/runtime/approve` |
| 审批拒绝 | POST | `/flow/runtime/reject` |

---

## 六、Controller 新增接口检查清单

AI 新增 REST 接口时必须逐项确认：

- [ ] Controller 有明确的 Request DTO 和 Response DTO
- [ ] Request DTO 使用 `@Valid` + Bean Validation 注解（`@NotBlank` / `@Size` / `@Min`）
- [ ] 响应用 `R<T>` 包装
- [ ] Service 层定义了对应的接口方法和实现
- [ ] Service 层内正确调用 nkk-kit-flow 的核心 Service（`NkkFlowEngine` / `FlowProcessService` 等）
- [ ] JSON 字段与 `JSON_SCHEMA.md` 对齐
- [ ] 路径以 `/flow` 为前缀，放在对应的 Controller 中

---

## 七、命名规范

### 7.1 DTO 命名

| 对象 | 格式 | 示例 |
|------|------|------|
| 请求 DTO | `FlowXxxRequest` | `FlowProcessPublishRequest` |
| 响应 DTO | `FlowXxxResponse` | `FlowInstanceDetailResponse` |
| 枚举（DTO 内部） | `FlowXxxTypeEnum` | `FlowTodoTypeEnum` |

### 7.2 Service 命名

| 对象 | 格式 | 示例 |
|------|------|------|
| Service 接口 | `FlowDesignerXxxService` | `FlowDesignerProcessService` |
| 默认 SPI 实现 | `DefaultFlowDesignerXxx` | `DefaultFlowDesignerOrgProvider` |

### 7.3 Provider / Extension

Web 层特有的组织架构 SPI：

```java
public interface FlowDesignerOrgProvider {
    List<FlowDesignerTreeNode> listUsers(String keyword);
    List<FlowDesignerTreeNode> listRoles();
    List<FlowDesignerTreeNode> listDepartments();
}
```

业务侧可注入自定义实现替换默认的 `DefaultFlowDesignerOrgProvider`。

---

## 八、发布前检查清单

- [ ] `mvn clean package -DskipTests` 编译无错误
- [ ] 所有 Controller 返回统一 `R<T>` 格式
- [ ] Controller 中没有直接操作 FlowEngine 的业务逻辑
- [ ] Service 层正确转换 DTO ↔ Model
- [ ] 新增 JSON 字段时已同步更新 `JSON_SCHEMA.md`
- [ ] 新增 Controller 时已注册到 `NkkFlowWebAutoConfiguration`
- [ ] Request DTO 有 Bean Validation 注解
- [ ] 没有在 Controller 中抛出 unchecked 异常（由全局异常处理器统一处理）

---

## 九、双项目 AI 协作规则

当 AI 同时修改前端 nkk-flow 和后端 nkk-kit 时：

1. **先读后写**：先读完两个项目的 `AI_SPEC.md` 和共享的 `JSON_SCHEMA.md`
2. **先后端后前端**：新增枚举值/字段时，先改后端（权威来源），再改前端对齐
3. **不越界**：nkk-kit-flow-web 不 import nkk-flow（前端项目），反之亦然
4. **JSON_SCHEMA.md 是唯一同步点**：任何字段变更必须更新此文件
5. **前后端独立构建**：`npm run build` 和 `mvn clean compile` 都必须通过

---

> **AI 工具注意**：Web 层的 API 是前后端的交汇点，这里的数据契约最容易出问题。每次涉及 JSON 字段变更，必须对照 `JSON_SCHEMA.md` 确认前后端类型一致。
