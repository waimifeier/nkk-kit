# 流程实例详情接口增加权限标志

## Context

当前 `GET /instances/{instanceId}` 详情接口只返回实例+任务数据，前端 `form-flow-detail.vue` 靠 `scene` 参数 + 前端比对 `createId === currentUserId` 来猜测能做什么操作（撤回/审批/催办/抄送）。这导致：
- 权限判断散落在前端，不安全且不完整
- 催办需单独 `urge-able` 接口预校验，多余
- 节点级权限（`allowTransfer`/`allowAppendNode`/`allowRollback`/`allowCc`）未下发给前端

目标：详情接口返回 `permissions` 对象，后端统一计算权限，前端零猜测。

## 改动清单

### 1. `FlowRuntimeResponse.java` — 新增 Permissions 内部类

路径：`nkk-kit-flow-web/src/main/java/org/nkk/flow/web/model/FlowRuntimeResponse.java`

新增字段 `private Permissions permissions;` 和内部类：

```java
@Data
public static class Permissions implements Serializable {
    private Boolean isInitiator;   // 当前用户 == 实例 createId
    private Boolean canRevoke;     // 审批中 + 发起人（accessStrategy REVOKE）
    private Boolean canUrge;       // 审批中 + 发起人 + 任务已超时 + 催办间隔已过
    private Boolean canApprove;   // 审批中 + 当前用户是活动任务 actor
    private Boolean canTransfer;   // canApprove + allowTransfer
    private Boolean canAppendNode; // canApprove + allowAppendNode
    private Boolean canRollback;   // canApprove + allowRollback
    private Boolean canCc;         // 审批中 + allowCc != false
}
```

### 2. `FlowDesignerRuntimeService.java` — detail() 末尾追加 buildPermissions

路径：`nkk-kit-flow-web/src/main/java/org/nkk/flow/web/service/FlowDesignerRuntimeService.java`

- `detail()` 方法在 `runtimeResponse(instanceId)` 返回后，调用 `buildPermissions(instanceId, response)` 填充权限
- 新增 `private FlowRuntimeResponse.Permissions buildPermissions(Long instanceId, FlowRuntimeResponse response)`：
  - 通过 `flowEngine.resolveCreator(null)` 获取当前操作人
  - `isInitiator`：`creator.getCreateId().equals(response.getCreateId())`
  - `canRevoke`：实例审批中 + `accessStrategy.isAllowed(creator, instance, hisInstance, REVOKE)`
  - `canUrge`：复用 `flowEngine.urgeAble(taskId, creator)`（已有完整逻辑：发起人校验+间隔校验）
  - `canApprove`：遍历活动任务 actors，匹配 `creator.getCreateId()`
  - 节点级权限（`canTransfer`/`canAppendNode`/`canRollback`/`canCc`）：复用已有 `nodeOperationAllowed(node, operation)` 方法
  - 流程非审批中时全部返回 false

### 3. `FlowDesignerRuntimeController.java` — 删除 urge-able 接口

路径：`nkk-kit-flow-web/src/main/java/org/nkk/flow/web/controller/FlowDesignerRuntimeController.java`

删除 L360-369 `urgeAble` 方法。`urge` 接口（L354-358）保留。

### 4. `flow-api.ts` — 类型扩展 + 删除 urgeAble 方法

路径：`F:/WEB_PROJECT/nkk-flow/src/api/flow-api.ts`

- `FlowInstanceDetail` 接口新增 `permissions?: FlowInstancePermissions`
- 新增 `FlowInstancePermissions` 类型（8 个可选 boolean 字段）
- 工厂中删除 `urgeAble` 方法，保留 `urgeTask`

### 5. `form-flow-detail.vue` — 用 permissions 驱动按钮

路径：`F:/WEB_PROJECT/digital_admin/src/views/approval/apply/modules/form-flow-detail.vue`

- 删除前端 `isInitiator`、`canWithdraw`、`canApprove`、`withdrawHint` 的前端计算逻辑
- 改为读取 `detail.permissions` 字段：
  - `canRevoke` → 撤回按钮
  - `canUrge` → 催办按钮（调 `urgeTask`，成功后重新 load 刷新权限）
  - `canApprove` → 同意/驳回
  - `canCc` → 抄送入口
  - `canTransfer` → 转交入口（预留 UI 位）
  - `canRollback` → 回退入口（预留 UI 位）
  - `canAppendNode` → 加签入口（预留 UI 位）
- 不再依赖 `scene` 参数区分按钮行为，由 `permissions` 统一驱动
- 保留提示文案逻辑：当 `permissions` 各操作字段均为 false 时，根据权限字段组合展示原因提示（如非发起人→「仅发起人可撤回此审批」、非审批人→「仅可查看审批详情」），具体规则在实现时从现有 `withdrawHint` 改造

### 6. `apply/index.vue` — 催办按钮简化

路径：`F:/WEB_PROJECT/digital_admin/src/views/approval/apply/index.vue`

催办按钮去掉 `urgeAble` 预校验调用，直接调 `urgeTask`，后端兜底校验返回错误时 toast 展示。

## 验证

1. `mvn compile -pl nkk-kit-flow,nkk-kit-flow-web -am -q`（nkk-kit 编译）
2. `mvn install -pl nkk-kit-flow,nkk-kit-flow-web -am -DskipTests -q`（安装到本地仓库）
3. digital_admin 编译验证：`npx vue-tsc --noEmit` 检查 approval 模块零错误
4. `pnpm build:lib`（nkk-flow 库重建）
5. 刷新 digital_admin，在「我发起的」列表点详情：
   - 审批中 + 发起人 → 看到「撤回」+「催办」（超时时）
   - 审批中 + 审批人 → 看到「同意/驳回」
   - 审批中 + 非发起人非审批人 → 看抄送入口
