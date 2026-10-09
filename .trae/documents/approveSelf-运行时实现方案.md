# approveSelf 运行时处理实现方案

## Context

`ApprovalNodeModel.approveSelf`（[ApprovalNodeModel.java#L73](file:///f:/JAVA_PROJECT/nkk-kit/nkk-kit-flow/src/main/java/org/nkk/flow/model/node/task/ApprovalNodeModel.java#L73)）目前只被定义、序列化、下发给前端设计器选项，引擎运行时从未读取。本次补全运行时处理，让该配置真正生效。

**用户确认的语义：**
- `=0` 自己审批：不做特殊处理，发起人作为审批人正常审批。
- `=1` 自动通过：审批人列表包含发起人时，只对该发起人 actor 触发；发起人那票按"自动通过"处理。现有 TaskState 无"跳过"中性状态，用户决定复用 `AUTO_COMPLETED(8,"自动完成")`，`opinion` 写"自审自动通过"。
- `=2` 转交上级：把发起人那一个 actor 替换为发起人所在部门上一级领导，用 `FlowSupervisorResolver.resolveSupervisor(initiatorId, 1)`；上级为 null 时保留发起人（降级为 =0）。
- 触发条件：审批人列表**包含**发起人时，只对该发起人单独触发，其他审批人不变；会签/或签/票签按各人独立判断。

## 介入点选型（核心决策）

| 字段值 | 介入层 | 介入点 | 理由 |
|--------|--------|--------|------|
| `=2` 转交上级 | Provider 层 | `DefaultFlowTaskActorProvider.getTaskActors` | 属"参与人解析"职责，复用已有 `FlowSupervisorResolver`；不改 actors 数量，不破坏 SEQUENTIAL 索引一致性 |
| `=1` 自动通过 | Service 层（`FlowTaskServiceImpl.createTask`）+ 节点执行层（`FlowNodeModel.executeInternal`） | createTask 拿 actors 之后、abnormal 检查之前短路 | 全发起人需 insert+归档+通知+继续流转，跨 Provider 职责；且 `createNextSequentialTask`（[NkkFlowEngineImpl.java#L626](file:///f:/JAVA_PROJECT/nkk-kit/nkk-kit-flow/src/main/java/org/nkk/flow/service/impl/NkkFlowEngineImpl.java#L626)）会重新调 Provider 拿原始 actors 做索引匹配，若在 Provider 过滤会破坏索引 → **approveSelf=1 必须在 createTask 内部过滤** |

**决策正确性依据**：`collectVoteStats`（[NkkFlowEngineImpl.java#L685-714](file:///f:/JAVA_PROJECT/nkk-kit/nkk-kit-flow/src/main/java/org/nkk/flow/service/impl/NkkFlowEngineImpl.java#L685-714)）只统计 `flow_his_task` + `flow_task` 实际存在记录；发起人未创建任务则不参与统计，会签/或签/票签决策天然支持。

## 改动文件清单（4 个文件，无新增）

### 1. [FlowExecution.java](file:///f:/JAVA_PROJECT/nkk-kit/nkk-kit-flow/src/main/java/org/nkk/flow/core/context/FlowExecution.java)
新增 transient 标记字段，用于 createTask 与 executeInternal 间传递"本节点是否自动通过"信号：
```java
/** approveSelf=1 全发起人自动通过标记，由 createTask 置位，executeInternal 读取后决定是否继续流转 */
private transient boolean approvalAutoCompleted;
```
`@Getter @Setter` 已作用于类，自动生成 `isApprovalAutoCompleted()` / `setApprovalAutoCompleted(boolean)`。

### 2. [DefaultFlowTaskActorProvider.java](file:///f:/JAVA_PROJECT/nkk-kit/nkk-kit-flow/src/main/java/org/nkk/flow/core/extension/identity/DefaultFlowTaskActorProvider.java)
在 `getTaskActors` 末尾（return actors 前）插入 `approveSelf=2` 替换逻辑：
- 遍历 actors，对 `ActorType.USER` 且 `actorId == resolveInitiatorId(execution)` 的，用 `execution.getContext().getSupervisorResolver().resolveSupervisor(initiatorId, 1)` 替换为上级；
- 上级返回 null（已到顶层）则保留原发起人 actor（降级 =0）；
- 上级 `actorType` 为空时补 `ActorType.USER`；复用 `FlowTaskActor.of(supervisor, saveWeight)`。
- 复用现有方法：`resolveInitiatorId`（L123）、`execution.getContext().getSupervisorResolver()`（参考 L91）。
- **不动 approveSelf=1**。

幂等性：supervisor 的 actorId ≠ initiatorId，二次调 Provider 不会重复替换。

### 3. [FlowTaskServiceImpl.java](file:///f:/JAVA_PROJECT/nkk-kit/nkk-kit-flow/src/main/java/org/nkk/flow/service/impl/FlowTaskServiceImpl.java)
**3.1** `createTask` 顶部重置标记（防跨节点泄漏）：
```java
public List<FlowTask> createTask(TaskNodeModel nodeModel, FlowExecution execution) {
    execution.setApprovalAutoCompleted(false);
    FlowTask task = createTaskBase(nodeModel, execution);
    ...
}
```
**3.2** 在 `List<FlowTaskActor> actors = execution.getProviderTaskActors(nodeModel);`（约 L102）之后、abnormal 检查之前，插入 `approveSelf=1` 短路：
```java
if (nodeModel instanceof ApprovalNodeModel) {
    Integer approveSelf = ((ApprovalNodeModel) nodeModel).getApproveSelf();
    if (Integer.valueOf(1).equals(approveSelf) && actors != null && !actors.isEmpty()) {
        String initiatorId = execution.getFlowInstance() != null
                ? execution.getFlowInstance().getCreateId()
                : (execution.getFlowCreator() != null ? execution.getFlowCreator().getCreateId() : null);
        // 划分发起人 actors 与其他人
        List<FlowTaskActor> initiatorActors = new ArrayList<>();
        List<FlowTaskActor> others = new ArrayList<>();
        for (FlowTaskActor a : actors) {
            if (ActorType.USER.value().equals(a.getActorType())
                    && initiatorId != null && initiatorId.equals(a.getActorId())) {
                initiatorActors.add(a);
            } else {
                others.add(a);
            }
        }
        if (!initiatorActors.isEmpty() && others.isEmpty()) {
            // 全发起人：自动通过（参考 COPY L82-99 + START L67-72 模式）
            task.setOpinion("自审自动通过");
            taskDao.insert(task);
            for (FlowTaskActor a : initiatorActors) {
                assignTask(task, a);
            }
            List<FlowTaskActor> archived = moveToHisTask(task, TaskState.AUTO_COMPLETED, execution.getFlowCreator());
            notifyTask(FlowEventTypeEnum.TASK_AUTO_COMPLETED, task, archived, nodeModel, execution.getFlowCreator());
            execution.setFlowTask(task);
            execution.setApprovalAutoCompleted(true);
            return Collections.singletonList(task);
        }
        if (!initiatorActors.isEmpty()) {
            // 多人含发起人：仅移除发起人，后续正常 dispatch
            actors = others;
        }
        // initiatorActors 为空（actors 中本就没发起人）：actors 不变
    }
}
```
原 abnormal 检查逻辑不变，仍处理真正无 actors 的异常场景。

### 4. [FlowNodeModel.java](file:///f:/JAVA_PROJECT/nkk-kit/nkk-kit-flow/src/main/java/org/nkk/flow/model/node/FlowNodeModel.java)
修改 `executeInternal` 的 ApprovalNodeModel 分支（L144-147），改为条件性继续流转：
```java
if (this instanceof ApprovalNodeModel) {
    context.createTask(execution, (TaskNodeModel) this);
    if (execution.isApprovalAutoCompleted()) {
        // 全发起人自动通过：归档后继续推进（参考抄送分支 L138-142）
        return nextNode().map(next -> next.execute(context, execution))
                .orElseGet(() -> execution.endInstance(this, InstanceState.COMPLETED));
    }
    return true;
}
```

### 不需要改动的文件
- `ApprovalNodeModel.java`：字段已存在，仅注释更新为 =1"自动通过"语义。
- `FlowHisTask.java`：`of()` 已在 L57 复制 opinion。
- `NkkFlowEngineImpl.java`：`continueAfterTask` 与四个决策方法（会签/或签/票签/顺序签）天然支持"发起人不在记录中"，无需改。
- TaskState 枚举：复用 `AUTO_COMPLETED(8)`，无需新增。

## 多人审批场景正确性

| 场景 | 行为 | 正确性 |
|------|------|--------|
| 会签含发起人 | 发起人不创建任务，`collectVoteStats` 不统计；剩余人全通过即 PASS | ✓ |
| 或签含发起人 | 剩余任一通过即 PASS | ✓ |
| 票签含发起人 | 权重只累加剩余 actors；若 passWeight 大于剩余总权重则永不通过（属配置责任，非 bug，前端校验提示） | ✓ |
| 顺序签发起人是第一人 | createTask 内部过滤为 `others=[B,C]`，为 B 创建任务；B 完成后 `createNextSequentialTask` 重新调 Provider 拿原始 `[initiator,B,C]`，找到 B index=1 取 i+1=2 → C，无偏移 | ✓ |

## opinion 备注传递链
`task.setOpinion("自审自动通过")` → `taskDao.insert` → `moveToHisTask` 内 `FlowHisTask.of(task, state)` 在 [FlowHisTask.java#L57](file:///f:/JAVA_PROJECT/nkk-kit/nkk-kit-flow/src/main/java/org/nkk/flow/entity/FlowHisTask.java#L57) 显式复制 opinion 到 hisTask。hisTaskActor 也会保留发起人记录（moveToHisTask L634-637）。

## 验证方式

示例 JSON [请假.json](file:///f:/JAVA_PROJECT/nkk-kit/nkk-kit-flow-example/src/main/resources/请假.json) 已在 node_leader（L19）和 node_gm（L61）配置 `"approveSelf": 1`，可直接测试。

端到端场景：
1. **全发起人自动通过**：setType=1 + nodeAssigneeList 仅发起人 + approveSelf=1。预期 flow_task 无活动任务，flow_his_task 一行 taskState=8 opinion="自审自动通过"，实例 currentNode 已推进，收到 TASK_AUTO_COMPLETED 事件。
2. **多人含发起人（会签）**：nodeAssigneeList=[发起人,A,B] + examineMode=2 + approveSelf=1。预期 flow_task 2 条活动任务（A、B），无发起人任务；A、B 都完成后会签 PASS 流转。
3. **顺序签发起人是第一人**：nodeAssigneeList=[发起人,A,B] + examineMode=1 + approveSelf=1。预期第一任务是 A，A→B→流转下一节点。
4. **approveSelf=2 转交上级**：宿主注册自定义 FlowSupervisorResolver Bean，nodeAssigneeList=[发起人] + approveSelf=2。预期 flow_task_actor.actorId 是上级 ID。
5. **approveSelf=2 兜底**：不注册 Resolver（DefaultFlowSupervisorResolver 始终返回 null），approveSelf=2。预期 actor 仍是发起人自己，不抛异常。
6. **票签权重边界**：nodeAssigneeList=[发起人(w=1),A(w=1),B(w=1)] + passWeight=2 + approveSelf=1。预期 A、B 任一通过后 WAIT，两人都通过后 PASS。

编译验证：`mvn -pl nkk-kit-flow install -DskipTests` 通过即可。

## 设计要点总结
1. 职责分层：=2 归 Provider（人解析），=1 归 Service+executeInternal（任务生命周期+流转）。
2. 不破坏现有决策方法、abnormal 兜底、SEQUENTIAL 索引一致性。
3. 复用现有模式：全发起人自动通过参考 START 分支 + COPY 分支 + 既有 resolveInitiatorId/FlowSupervisorResolver/FlowHisTask.of/eventTypeOf(AUTO_COMPLETED)=TASK_AUTO_COMPLETED。
4. flag 不跨节点泄漏：createTask 顶部重置 approvalAutoCompleted。
5. 改动最小：4 文件，无新文件，无 API 签名变更，无枚举新增。
