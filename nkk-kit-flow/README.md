# nkk-kit-flow

`nkk-kit-flow` 是审批流 starter 模块，定位类似 FlowLong 的 `flowlong-spring-boot-starter`：引入依赖后自动装配流程引擎，并默认使用 MyBatis-Plus 接入数据库持久化。

## 模块边界

- `nkk-kit-flow`：starter 和核心能力。负责自动配置、MyBatis-Plus 持久化、部署流程、启动实例、创建任务、完成任务、驳回、撤回、拿回、转办、委派、代理、加签、减签、定时/触发、子流程扩展。
- `nkk-kit-flow-web`：Web 扩展。后续负责流程设计器页面、流程定义配置接口、管理端 API 和可选审批页面。
- 使用方系统：只需要准备数据源、执行 SQL，并按需要提供业务扩展 Bean，例如参与人解析、权限策略、事件监听、消息通知等。

## 包结构

- `org.nkk.flow.core.context`：上下文、执行对象、操作人。
- `org.nkk.flow.core.engine`：引擎构建器。
- `org.nkk.flow.core.extension.ai`：AI 节点处理和 AI 分支决策扩展。
- `org.nkk.flow.core.extension.condition`：条件表达式和条件分支扩展。
- `org.nkk.flow.core.extension.id`：ID 生成器扩展。
- `org.nkk.flow.core.extension.identity`：参与人解析和任务权限扩展。
- `org.nkk.flow.core.extension.json`：JSON 适配。
- `org.nkk.flow.core.extension.listener`：节点、任务和实例事件监听。
- `org.nkk.flow.core.extension.schedule`：超时、提醒、触发任务扩展。
- `org.nkk.flow.core.extension.subprocess`：子流程扩展。
- `org.nkk.flow.dao`：核心 DAO 接口。
- `org.nkk.flow.dao.mybatis`：MyBatis-Plus 默认 DAO 实现。
- `org.nkk.flow.entity`：流程定义、实例、任务、参与者实体。
- `org.nkk.flow.enums`：流程枚举，统一以 `XxxEnum` 命名并实现 `IEnum`。
- `org.nkk.flow.mapper`：MyBatis-Plus Mapper。
- `org.nkk.flow.autoconfigure`：Spring Boot starter 自动配置。
- `org.nkk.flow.model`：流程 JSON 模型。
- `org.nkk.flow.service`：核心服务门面。

## Spring Boot 使用

引入依赖：

```xml
<dependency>
    <groupId>io.github.waimifeier</groupId>
    <artifactId>nkk-kit-flow</artifactId>
    <version>${nkk-kit.version}</version>
</dependency>
```

执行建表脚本：

```text
META-INF/nkk-flow/schema-mysql.sql
```

可选配置：

```yaml
flow:
  banner: true
  eventing:
    instance: true
    task: true
```

启动后会自动装配：

- `NkkFlowEngine`
- `FlowProcessService`
- `FlowRuntimeService`
- `FlowTaskService`
- `FlowQueryService`
- MyBatis-Plus Mapper 和 8 个默认 DAO 实现

starter 按 Spring Boot 规范拆分 Bean。使用方可以通过注册同类型 Bean 覆盖任意默认实现，例如：

- `FlowIdGenerator`
- `FlowExpression`
- `FlowConditionHandler`
- `FlowCreatorProvider`
- `FlowTaskActorProvider`
- `FlowActorAccessStrategy`：统一判断当前用户是否命中用户、角色、部门等参与人配置。
- `FlowNodeListener`
- `FlowSubProcessHandler`
- `FlowTaskListener`
- `FlowInstanceListener`
- `FlowInstanceAccessStrategy`：控制撤回、终止、挂起、激活、作废等实例级操作权限。

也支持以下扩展 Bean：

- `FlowCreateTimeHandler`：自定义流程实例和任务创建/完成时间。
- `FlowTaskCreateInterceptor`：任务创建前后拦截。
- `FlowCreateTaskHandler`：任务入库前自定义任务属性。
- `FlowModelCache`：替换默认内存模型缓存。
- `FlowAiHandler`：接入外部 AI 智能体，支持审批自动通过/拒绝、低置信度转人工、失败兜底、AI 条件分支。
- `FlowJobLock`：替换默认本地调度锁，接入 Redis、数据库等分布式锁。
- `FlowDesignerFormProvider`：表单数据提供者，提供设计器可选表单下拉和字段元数据。
- `FlowApprovalCallbackHandler`：审批结果回调处理器，支持多个 Bean，实例/任务生命周期节点触发。
- `FlowBusinessApprovalLauncher`：业务发起审批桥接入口。

## 业务流转与数据表关系

`nkk-kit-flow` 当前使用 8 张核心表承载流程定义、流程实例、任务、任务参与者和表单字段元数据。SQL 位于：

```text
META-INF/nkk-flow/schema-mysql.sql
```

这些表没有声明数据库物理外键，核心模块通过代码维护逻辑关系。这样做的好处是 starter 可以更容易被业务系统接入，后续如果使用方需要分库、归档、按租户拆表或替换 DAO，也不会被物理外键强绑定。

### 核心表职责

| 表名 | 对应实体 | 生命周期 | 主要作用 |
| --- | --- | --- | --- |
| `flow_process` | `FlowProcess` | 流程定义部署后长期保留 | 保存流程定义、版本、状态和流程模型 JSON（含 `extendConfig.metaForm` 表单绑定信息）。一个 `process_key` 可以有多个版本。 |
| `flow_instance` | `FlowInstance` | 只保存正在运行的实例 | 保存活动流程实例的当前节点、发起人、业务 key、实例变量等。流程结束后会从该表删除。 |
| `flow_his_instance` | `FlowHisInstance` | 实例创建时插入，结束后继续保留 | 保存实例生命周期记录。实例启动时就会插入一条历史记录，运行中同步当前节点和变量，结束时更新最终状态和结束时间。 |
| `flow_ext_instance` | `FlowExtInstance` | 实例创建时插入，默认随实例历史保留 | 保存实例级流程模型快照。流程定义后续改版不会影响已经启动的实例。 |
| `flow_task` | `FlowTask` | 只保存待处理任务 | 保存当前待办任务、任务类型、参与方式、变量、超时提醒、委派转办信息、子流程等待信息等。任务完成后会从该表删除。 |
| `flow_his_task` | `FlowHisTask` | 任务完成、关闭或异常结束后写入 | 保存任务处理历史。活动任务完成时会复制到历史任务表，并记录任务状态、完成时间和耗时。 |
| `flow_task_actor` | `FlowTaskActor` | 只保存活动任务参与者 | 保存当前任务的办理人、角色、部门、候选人快照以及权重、代理信息。任务完成后会从该表删除。 |
| `flow_his_task_actor` | `FlowHisTaskActor` | 任务转历史时写入 | 保存任务参与者历史快照，便于后续查看当时是谁可审批、谁被委派、谁参与会签/票签。 |
| ~~已移除~~ | ~~已移除~~ | ~~已移除~~ | 字段元数据随流程模型 JSON 存储在 `extendConfig.metaForm.fields`，通过 `FlowDesignerFormProvider` SPI 按需查询，不再落独立表。 |

### 逻辑关系

| 关系 | 说明 |
| --- | --- |
| `flow_process.id` -> `flow_instance.process_id` | 一个流程定义可以启动多个活动流程实例。 |
| `flow_process.id` -> `flow_his_instance.process_id` | 历史实例记录所属流程定义。 |
| `flow_process.id` -> `flow_ext_instance.process_id` | 实例模型快照来源于哪个流程定义。 |
| `flow_instance.id` -> `flow_task.instance_id` | 一个活动实例下可以有多个活动任务，例如并行、会签、票签。 |
| `flow_instance.id` -> `flow_task_actor.instance_id` | 活动任务参与者同时冗余实例 ID，方便按实例清理和查询。 |
| `flow_instance.id` = `flow_his_instance.id` | 同一个实例在活动表和历史表中使用同一个 ID。 |
| `flow_instance.id` = `flow_ext_instance.id` | 实例扩展表主键就是实例 ID。 |
| `flow_task.id` -> `flow_task_actor.task_id` | 一个活动任务可以有一个或多个参与者。 |
| `flow_his_task.id` -> `flow_his_task_actor.task_id` | 一个历史任务对应当时的参与者快照。 |
| `flow_instance.parent_instance_id` | 子流程实例指向父流程实例。 |
| `flow_task.parent_task_id` | 当前任务由哪个上游任务流转而来。 |
| `flow_task.call_process_id`、`call_instance_id` | 父流程等待子流程时，任务会记录调用的子流程定义和子流程实例。 |
| 表单字段元数据来源 | 流程模型 JSON 的 `extendConfig.metaForm.fields` + `FlowDesignerFormProvider.listFormFields()` | 设计器条件节点先从 Provider 查实时字段，Provider 未实现时读流程模型快照。 |

### 一次完整审批的数据流

1. 部署流程定义。

   调用 `FlowProcessService.deploy(...)` 或封装后的部署接口时，系统解析流程 JSON、执行模型校验，然后写入 `flow_process`。

   如果同一个 `process_key` 已经存在并且允许重复部署，会把旧版本的 `process_state` 更新为历史版本，再插入新版本。运行中的实例不会直接读取最新定义，而是读取自己的 `flow_ext_instance.model_content`。
   如果发布请求携带了表单绑定信息，会写入 `flow_process.model_content` 的 `extendConfig.metaForm`，字段元数据随 `metaForm.fields` 一起持久化，不再落独立表。

2. 发起流程实例。

   调用 `NkkFlowEngine.startInstanceByProcessId(...)` 或 `startInstanceByProcessKey(...)` 后，系统先创建实例数据：

   - 插入 `flow_instance`：当前活动实例。
   - 插入 `flow_his_instance`：实例生命周期历史，初始状态通常是审批中；暂存时是暂存待审。
   - 插入 `flow_ext_instance`：当前实例使用的流程模型快照。

   然后执行开始节点。开始节点会生成一条任务，再立即完成并转入历史任务表，所以开始节点一般不会长期停留在 `flow_task`。

3. 创建第一个真实待办。

   流程走到审批、抄送、定时器、触发器、子流程等待等节点时，调用 `FlowTaskService.createTask(...)` 创建活动任务：

   - 插入 `flow_task`。
   - 如果节点需要办理人，插入 `flow_task_actor`。
   - 更新 `flow_instance.current_node_key/current_node_name`。
   - 同步更新 `flow_his_instance.current_node_key/current_node_name`。

   审批人由 `FlowTaskActorProvider` 解析。核心模块只保存解析结果，不查询用户、角色、部门等业务组织表。

4. 办理任务。

   调用 `executeTask(...)`、`rejectTask(...)`、`transferTask(...)`、`delegateTask(...)`、`addTaskActor(...)` 等任务入口时，会根据操作类型处理活动任务。

   普通完成任务时：

   - 读取 `flow_task`。
   - 校验当前操作人是否在 `flow_task_actor` 允许范围内。
   - 把本次传入变量合并到任务变量。
   - 插入 `flow_his_task`。
   - 把当前任务参与者复制到 `flow_his_task_actor`。
   - 删除 `flow_task_actor`。
   - 删除 `flow_task`。
   - 继续执行流程模型，创建下一个节点任务或结束实例。

5. 流程结束。

   当流程走到结束节点，并且当前实例没有其他活动任务时：

   - 删除 `flow_instance`。
   - 更新 `flow_his_instance.instance_state/end_time/current_node_key/current_node_name`。
   - 默认保留 `flow_ext_instance`，用于历史实例继续按启动时的模型快照查看。
   - 已完成任务和参与者保留在 `flow_his_task`、`flow_his_task_actor`。

### 操作与数据写入关系

| 操作 | 主要写入/更新/删除的表 | 说明 |
| --- | --- | --- |
| 部署流程定义 | 插入 `flow_process`；可能更新旧版本 `flow_process.process_state` | 保存流程模型 JSON 和版本。重复部署时旧版本转历史版本。 |
| 发布流程绑定表单 | 写入 `flow_process.model_content` 的 `extendConfig.metaForm`， | 元表单信息随流程模型 JSON 一起存储。 |
| 启用/禁用流程定义 | 更新 `flow_process.process_state` | 只影响后续是否允许使用该定义，不修改已运行实例的模型快照。 |
| 修改流程定义资料 | 更新 `flow_process` | 通常修改名称、图标、分类、实例地址、备注、排序等展示和管理字段。 |
| 发起流程实例 | 插入 `flow_instance`、`flow_his_instance`、`flow_ext_instance` | 活动实例、历史生命周期、实例模型快照同时创建。 |
| 暂存草稿 | 插入 `flow_instance`、`flow_his_instance`、`flow_ext_instance` | 历史实例状态为暂存待审，后续可恢复继续流转。 |
| 创建审批任务 | 插入 `flow_task`、`flow_task_actor`；更新 `flow_instance`、`flow_his_instance` 当前节点 | 普通审批、抄送、定时器、触发器、子流程等待都会落活动任务。 |
| 顺序审批 | 插入 1 条 `flow_task` 和当前顺序办理人的 `flow_task_actor` | 当前人完成后再创建下一个顺序办理人的任务。 |
| 会签/票签 | 可能按参与人插入多条 `flow_task` 和多条 `flow_task_actor` | 每个参与人独立处理，完成后根据会签、票签规则判断是否收口。 |
| 或签 | 通常插入 1 条 `flow_task` 和多个 `flow_task_actor` | 任一允许参与者处理后，任务进入历史并继续流转。 |
| 完成任务 | 插入 `flow_his_task`、`flow_his_task_actor`；删除 `flow_task_actor`、`flow_task` | 活动任务转历史任务。完成后继续执行下一个节点。 |
| 反对票/弃权票 | 插入 `flow_his_task`、`flow_his_task_actor`；删除对应活动任务和参与者 | 用于会签、票签的投票收口；是否结束流程取决于权重和策略。 |
| 驳回/驳回跳转 | 当前任务和相关活动任务转入 `flow_his_task`；删除对应活动表；可能创建目标节点新任务 | 驳回到指定节点、上一审批节点、发起人等都通过任务历史状态记录跳转语义。 |
| 任意跳转/路由跳转/触发跳转 | 活动任务转历史；更新实例当前节点；创建目标节点任务 | 历史任务状态记录跳转类型，目标任务可标记为对应跳转任务类型。 |
| 转办 | 更新 `flow_task.task_type/assignor_id/assignor/variable`；替换 `flow_task_actor` | 任务仍是同一条活动任务，只是办理人替换为被转办人。 |
| 委派 | 更新 `flow_task`；替换 `flow_task_actor` | 委派完成后可以归还给原办理人。 |
| 委派归还 | 更新 `flow_task.task_type`；替换 `flow_task_actor` | 把任务参与者恢复为委派人。 |
| 代理/角色认领/部门认领 | 更新 `flow_task`；替换或新增 `flow_task_actor` | 认领后一般把角色/部门候选任务转换为具体用户任务。 |
| 加签 | 插入 `flow_task_actor`；会签/票签场景可能额外插入 `flow_task` | 非多人独立任务时只加参与者；多人独立任务时给新增人创建独立任务。 |
| 减签 | 删除指定 `flow_task_actor` | 不允许把任务所有参与者全部移除。 |
| 修改办理人 | 删除旧 `flow_task_actor`，插入新 `flow_task_actor` | 用于管理员或外围业务直接调整当前任务办理人。 |
| 已阅 | 更新 `flow_task.viewed` | 只标记当前活动任务已读，不结束任务。 |
| 更新实例变量 | 更新 `flow_instance.variable`；同步更新 `flow_his_instance.variable` | 运行中变量以活动实例为主，历史实例同步一份用于查询。 |
| 更新任务变量 | 更新或转历史前写入 `flow_task.variable`；完成后保存在 `flow_his_task.variable` | 每次处理任务传入的参数会合并到任务变量。 |
| 追加/移除临时节点 | 更新 `flow_ext_instance.model_content` | 只影响当前实例的模型快照，不修改 `flow_process.model_content`。 |
| 暂停/激活实例 | 更新 `flow_his_instance.instance_state` | 当前实现把暂停/激活状态记录在历史实例状态中，活动实例数据仍保留。 |
| 恢复实例 | 插入 `flow_instance`；更新 `flow_his_instance.instance_state/end_time` | 当活动实例不存在但历史实例存在时，可恢复成活动实例继续处理。 |
| 撤回/拿回 | 相关活动任务转历史；可能恢复或重新创建可处理任务 | 通过历史任务状态区分撤回、拿回、恢复等语义。 |
| 超时/终止/撤销/作废 | 活动任务转 `flow_his_task`；删除活动任务和参与者；删除 `flow_instance`；更新 `flow_his_instance` | 这些属于实例结束类操作，最终状态写入历史实例。 |
| 级联删除实例 | 删除 `flow_task_actor`、`flow_his_task_actor`、`flow_task`、`flow_his_task`、`flow_ext_instance`、`flow_instance`、`flow_his_instance` | 用于彻底清理父流程和子流程数据，不是普通审批结束。 |

### 活动表与历史表规则

- 活动实例表 `flow_instance` 和活动任务表 `flow_task` 只代表当前还需要继续流转的数据。
- 历史实例表 `flow_his_instance` 不是流程结束后才插入，而是实例启动时就插入，并在运行中同步当前节点和变量。
- 任务完成不是简单更新状态，而是“活动任务复制到历史任务，再删除活动任务”。因此待办查询查 `flow_task`，已办/轨迹查询查 `flow_his_task`。
- 参与者表也遵循同样规则：活动参与者在 `flow_task_actor`，任务完成后复制到 `flow_his_task_actor`。
- `flow_ext_instance` 保存实例模型快照，是流程审计和历史查看的重要依据。流程定义升级后，老实例仍按自己的快照继续流转。

### 子流程数据流

子流程本质上也是一个普通流程实例，只是多了父子关系：

- 子流程启动时，会新增自己的 `flow_instance`、`flow_his_instance`、`flow_ext_instance`。
- 子流程的 `parent_instance_id` 指向父流程实例 ID。
- 同步子流程会在父流程中创建一个等待任务，父任务的 `call_process_id`、`call_instance_id` 分别记录子流程定义 ID 和子流程实例 ID。
- 子流程正常结束后，父流程等待任务会被自动完成并转入历史任务表，父流程继续向下执行。
- 子流程拒绝、撤销、终止、超时、作废时，默认会把相同语义传递给父流程。
- 级联删除父流程实例时，会递归清理父流程和所有子流程的活动实例、历史实例、模型快照、活动任务、历史任务和参与者数据。

### 查询建议

| 场景 | 建议查询表 |
| --- | --- |
| 流程定义列表、版本记录、启用禁用状态 | `flow_process` |
| 我的待办 | `flow_task` + `flow_task_actor` |
| 当前实例在哪个节点 | `flow_instance`，没有活动实例时查 `flow_his_instance` |
| 我发起的流程 | `flow_his_instance`，必要时关联 `flow_process` |
| 已办任务 | `flow_his_task` + `flow_his_task_actor` |
| 审批轨迹 | `flow_his_task`，按 `instance_id/create_time/finish_time` 排序 |
| 历史流程图回显 | `flow_ext_instance.model_content` |
| 子流程关系 | `flow_instance.parent_instance_id` 或 `flow_his_instance.parent_instance_id` |
| 表单字段元数据 | `flow_process.model_content` 的 `extendConfig.metaForm.fields` + `FlowDesignerFormProvider` SPI |
| 流程绑定表单 | `flow_process.model_content` 的 `extendConfig.metaForm` |

## 组织参与人扩展

`nkk-kit-flow` 不内置用户、角色、部门、岗位、候选人等组织数据表，也不直接查询组织架构。核心模块只保存任务参与者快照，并通过扩展点把组织能力交给使用方系统实现。

当前暴露方式：

- `FlowCreatorProvider`：负责从使用方登录上下文中获取当前流程操作人，以及提供系统操作人身份（用于自动超时、触发器、子流程等无人操作场景）。显式传入 `FlowCreator` 时优先使用显式参数；未传时才从该扩展点获取。
- `FlowTaskActorProvider`：负责把节点模型解析成任务参与者。使用方可覆盖 `getTaskActors(...)`、`getNodeAssignees(...)`、`getDynamicAssignee(...)`。
- `FlowActorAccessStrategy`：统一判断当前用户是否命中用户、角色、部门等参与人配置。开始节点发起权限和审批任务办理权限都会委托给它。
- `FlowStartAccessStrategy`：发起权限内部适配层，默认遍历开始节点 `nodeAssigneeList` 后调用 `FlowActorAccessStrategy`。
- `FlowTaskAccessStrategy`：任务权限内部适配层，默认遍历 `flow_task_actor` 后调用 `FlowActorAccessStrategy`。
- `FlowNodeSetTypeEnum`：定义节点参与人配置类型，当前包含指定成员、主管、角色、发起人自选、发起人本人、部门、候选人。
- `FlowActorTypeEnum`：定义任务参与者快照类型，当前包含用户、角色、部门。
- `FlowDataTransfer.dynamicAssignee(...)`：支持单次调用线程内动态指定节点办理人。
- `FlowDynamicAssignee`：动态指定办理人及参与者类型，`type` 取值为 `0=用户`、`1=角色`、`2=部门`。

## 实例操作权限扩展

`FlowInstanceAccessStrategy` 用来控制流程实例级操作的权限，典型入口包括：

- `revoke`
- `terminate`
- `suspend`
- `active`
- `destroy`
- `timeout`

默认实现 `DefaultFlowInstanceAccessStrategy` 的规则是：

- 撤回：只允许流程发起人。
- 挂起、激活、终止、作废、超时结束：只允许管理员。

如果你希望“流程管理员、部门主管、角色负责人、单据拥有者”也能操作实例，可以自己覆盖这个 Bean：

```java
@Bean
public FlowInstanceAccessStrategy flowInstanceAccessStrategy(UserOrgService userOrgService) {
    return (creator, instance, hisInstance, operateType) -> {
        if (creator != null && "0".equals(creator.getCreateId())) {
            return true;
        }
        if (hisInstance == null) {
            return false;
        }
        if (Objects.equals(creator.getCreateId(), hisInstance.getCreateId())) {
            return true;
        }
        return userOrgService.hasRole(creator.getCreateId(), "FLOW_ADMIN");
    };
}
```

说明：

- `instance` 优先表示当前活动实例。
- `hisInstance` 一定会尽量提供，便于读取历史状态和发起人信息。
- 这个策略只管“能不能操作实例”，不管“能不能办理任务”。
- 任务办理权限仍然由 `FlowTaskAccessStrategy` 控制。

当前操作人扩展示例：

```java
@Bean
public FlowCreatorProvider flowCreatorProvider() {
    return new FlowCreatorProvider() {
        @Override
        public FlowCreator getCurrentCreator() {
            return FlowCreator.of("u1", "张三");
        }

        @Override
        public FlowCreator getSystemCreator() {
            return FlowCreator.of("0", "系统操作人");
        }
    };
}
```

配置后可以使用不显式传 `FlowCreator` 的入口：

```java
engine.startInstanceByProcessKey("leave", null, variables);
engine.executeTask(taskId, variables);
engine.rejectTask(taskId, variables);
```

参与人权限扩展示例：

```java
@Bean
public FlowActorAccessStrategy flowActorAccessStrategy(UserOrgService userOrgService) {
    return new DefaultFlowActorAccessStrategy() {
        @Override
        protected boolean hasRole(String userId, String roleId) {
            return userOrgService.hasRole(userId, roleId);
        }

        @Override
        protected boolean inDepartment(String userId, String departmentId) {
            return userOrgService.inDepartment(userId, departmentId);
        }
    };
}
```

说明：

- `FlowCreatorProvider` 只提供当前操作人的基础身份，不要求携带角色、部门、岗位。
- 角色、部门、岗位、候选人等真实组织关系由使用方在 `FlowActorAccessStrategy` 中查询和判断。
- 开始节点的 `nodeAssigneeList` 负责保存设计器选择的发起范围；审批节点创建任务后会保存到 `flow_task_actor`。
- `FlowStartAccessStrategy` 和 `FlowTaskAccessStrategy` 默认都是适配器，通常不需要业务方再单独注册。

动态指定办理人示例：

```java
Map<String, Object> dynamic = new HashMap<>();
dynamic.put("audit", FlowDynamicAssignee.roleList(roleAssignees));
FlowDataTransfer.dynamicAssignee(dynamic);
engine.startInstanceByProcessKey("leave", null, FlowCreator.of("u1", "张三"), variables);
```

参考 FlowLong 的设计：

- FlowLong 同样通过 `TaskActorProvider` 暴露参与人解析，通过 `TaskAccessStrategy` 暴露任务权限判断。
- FlowLong 默认 `GeneralTaskActorProvider` 只处理模型中的 `nodeAssigneeList`，不会查询真实组织架构。
- FlowLong 默认 `GeneralAccessStrategy` 也是按 `actorId` 精确匹配，角色、部门关系需要使用方自己扩展。
- FlowLong 的 `NodeSetType` 包含指定成员、主管、角色、发起人自选、发起人本人、连续多级主管、部门、指定候选人。
- FlowLong 额外有 `NodeCandidate` 模型字段，用于保存候选人/角色/部门配置。

与 FlowLong 的差异清单：

| 能力 | FlowLong | nkk-kit-flow 当前实现 | 说明 |
| --- | --- | --- | --- |
| 用户/角色/部门真实查询 | 不内置，通过扩展实现 | 不内置，通过扩展实现 | 保持 starter 轻量，不绑定业务组织表 |
| 参与人解析扩展 | `TaskActorProvider` | `FlowTaskActorProvider` | 命名按 NKK 风格调整 |
| 发起权限扩展 | 可通过自定义策略处理 | `FlowActorAccessStrategy` + `FlowStartAccessStrategy` 适配 | 开始节点未配置发起范围时允许；配置后按 actorType 判断 |
| 任务权限扩展 | `TaskAccessStrategy` | `FlowActorAccessStrategy` + `FlowTaskAccessStrategy` 适配 | 任务参与者按 actorType 判断 |
| 默认参与人来源 | `nodeAssigneeList` | `nodeAssigneeList`、`FlowDataTransfer.dynamicAssignee`、`getDynamicAssignee` | NKK 默认实现直接支持线程内动态办理人 |
| 参与者快照类型 | 用户、角色、部门 | 用户、角色、部门 | 与任务参与者表字段保持一致 |
| 节点 setType | 指定成员、主管、角色、发起人自选、发起人本人、连续多级主管、部门、指定候选人 | 指定成员、主管、角色、发起人自选、发起人本人、部门、候选人 | 暂未内置连续多级主管 |
| 候选人模型 | `NodeCandidate` | 暂未单独建模，使用 `nodeAssigneeList` 或动态参与人承载 | 如后续设计器需要候选池，可补 `FlowNodeCandidate` |
| 岗位/用户组/表单联系人 | 注释中提到部分扩展场景，但核心枚举未完整内置 | 暂未内置 | 建议放到使用方 `FlowTaskActorProvider` 或后续 web 扩展 |
| 角色/部门发起或认领权限 | 需要扩展策略判断用户是否属于角色/部门 | 统一扩展 `FlowActorAccessStrategy` | 核心模块不关心组织成员关系 |

参考 Warm-Flow 后的取舍：

| 设计点 | Warm-Flow | nkk-kit-flow 处理方式 | 取舍原因 |
| --- | --- | --- | --- |
| 当前办理人 | `PermissionHandler#getHandler()`，`FlowParams#getHandler()` 未传时回退获取 | `FlowCreatorProvider#getCurrentCreator()`，引擎入口未传 `FlowCreator` 时回退获取 | 借鉴“显式优先、上下文兜底”，但返回完整 `FlowCreator`，能携带租户、用户 ID、显示名 |
| 当前权限标识 | `PermissionHandler#permissions()` 返回当前用户权限集合 | 使用 `FlowActorAccessStrategy` 判断参与者是否可处理 | 权限判断放在策略里比把权限集合塞进操作参数更清楚，也更适合角色、部门、岗位等复杂组织 |
| 操作参数对象 | `FlowParams` 同时承载 handler、权限、变量、下一办理人、意见等 | 当前仍使用 `FlowCreator + Map<String,Object>`，动态办理人用 `FlowDataTransfer` 或 `FlowTaskActorProvider` | 暂不引入大而全参数对象，避免 API 语义变宽；后续如要统一意见、下一办理人，可新增 `FlowOperateCommand` |
| 全局引擎 | 静态 `FlowEngine` 保存服务和处理器 | Spring Bean + `FlowContext`，手动构建走 `FlowEngineBuilder` | starter 场景下 Bean 更符合 Spring 规范，测试和替换更容易 |
| 节点监听 | 全局监听 + 节点/定义上的 `listenerType/listenerPath` 字符串配置 | 新增 `FlowNodeListener` Bean，提供节点执行前后、任务创建前后钩子 | 保留节点生命周期能力，不把 Java 类路径写进流程 JSON，设计器后续可把业务配置放 `extendConfig` |

## 事件发布

开启 `flow.eventing.task=true` 后，starter 会把任务事件发布为 `NkkFlowTaskEvent`；开启 `flow.eventing.instance=true` 后，会把实例事件发布为 `NkkFlowInstanceEvent`。

节点生命周期可以通过注册 `FlowNodeListener` 监听：

```java
@Bean
public FlowNodeListener flowNodeListener() {
    return new FlowNodeListener() {
        @Override
        public void beforeExecute(FlowNodeEvent event) {
            // 节点开始执行前
        }

        @Override
        public void afterCreateTask(FlowNodeEvent event) {
            // 节点创建任务后，可读取 event.getTasks()
        }
    };
}
```

节点监听、任务事件、实例事件的区别：

| 类型 | 关注点 | 适合做什么 |
| --- | --- | --- |
| `FlowNodeListener` | 流程模型节点执行过程 | 节点轨迹、节点级业务扩展、任务创建前后补充处理 |
| `FlowTaskListener` | 任务状态变化 | 待办消息、审批通知、任务审计 |
| `FlowInstanceListener` | 流程实例状态变化 | 实例启动/结束通知、业务单据状态同步 |

任务事件覆盖：

- 创建、完成、自动完成、拒绝、自动拒绝。
- 多人审批投反对票、弃权。
- 转办、委派、委派归还、代理、认领、角色/部门认领。
- 加签、减签、修改办理人、已阅。
- 跳转、路由跳转、触发器跳转、驳回跳转、驳回重新审批跳转。
- 撤回、拿回、超时、提醒、触发、终止、撤销、作废、驳回终止。
- 子流程启动、子流程结束。

实例事件覆盖：

- 启动、结束、暂停、激活、恢复。
- 拒绝、撤销、超时、终止、作废。
- 变量更新、模型更新、临时节点追加、临时节点移除、级联删除。

## AI 节点

节点配置 `callAi` 后，starter 会在任务创建后调用使用方提供的 `FlowAiHandler`。

```java
@Bean
public FlowAiHandler flowAiHandler() {
    return (context, execution, nodeModel) -> {
        // 使用 nodeModel.getCallAi() 和 nodeModel.getAiConfig() 调用自己的 AI 服务
        return FlowAiResponse.success("PASS", "AI 自动通过", 0.92D);
    };
}
```

节点 JSON 可在 `extendConfig.aiConfig` 下放置 AI 配置：

```json
{
  "nodeKey": "audit",
  "type": 1,
  "callAi": "leave-agent",
  "extendConfig": {
    "aiConfig": {
      "agentId": "leave-agent",
      "confidenceThreshold": 0.8,
      "fallbackStrategy": "MANUAL"
    }
  }
}
```

默认策略：

- `PASS`：自动完成当前 AI 节点任务并继续向下流转。
- `REJECT`：自动拒绝并结束当前流程实例。
- `ASYNC`：保留当前任务，等待使用方自行回调处理。
- 低置信度、失败或超时：按 `fallbackStrategy` 处理，默认转人工。

条件分支和包容分支配置 `callAi` 后，会优先调用 `decideRoute` 或 `decideInclusiveRoutes`，返回空时再走普通条件表达式。

## 调度

starter 默认装配 `NkkFlowScheduler` 和本地 `FlowJobLock`。使用方可以用自己的调度器触发：

```java
@Scheduled(cron = "*/5 * * * * ?")
public void flowRemind() {
    nkkFlowScheduler.remind();
}
```

集群部署时，注册一个自己的 `FlowJobLock` Bean 即可换成分布式锁。

## 手动构建

非 Spring 环境也可以手动构建引擎，但需要自己提供 DAO 实现。

```java
NkkFlowEngine engine = FlowEngineBuilder.create()
        .processDao(processDao)
        .instanceDao(instanceDao)
        .hisInstanceDao(hisInstanceDao)
        .extInstanceDao(extInstanceDao)
        .taskDao(taskDao)
        .hisTaskDao(hisTaskDao)
        .taskActorDao(taskActorDao)
        .hisTaskActorDao(hisTaskActorDao)
        .taskActorProvider(taskActorProvider)
        .taskAccessStrategy(taskAccessStrategy)
        .conditionHandler(conditionHandler)
        .build();
```

## 基础调用

```java
Long processId = engine.processService().deploy(json, FlowCreator.of("u1", "张三"), true);
engine.startInstanceByProcessKey("leave", null, FlowCreator.of("u1", "张三"), variables);
engine.executeTask(taskId, FlowCreator.of("u2", "李四"), variables);
```

多人审批中，`rejectTask` 表示驳回跳转。如果只是会签、或签、票签中的反对票或弃权票，使用下面两个入口：

```java
engine.voteRejectTask(taskId, FlowCreator.of("u2", "李四"), variables);
engine.abstainTask(taskId, FlowCreator.of("u3", "王五"), variables);
```

节点可通过 `extendConfig` 配置多人审批收口策略：

```json
{
  "examineMode": 4,
  "passWeight": 60,
  "extendConfig": {
    "rejectWeight": 50,
    "vetoWeight": 80,
    "allowAbstain": true,
    "abstainAsPass": false
  }
}
```

- `passWeight`：票签通过权重，默认 50。
- `rejectWeight`：拒绝权重达到该值时结束为拒绝。
- `vetoWeight`：某个反对票参与人权重大于等于该值时一票否决。
- `allowAbstain`：是否允许调用 `abstainTask`。
- `abstainAsPass`：弃权是否按通过权重计算。

## 子流程

流程节点 `type = FlowTaskTypeEnum.CALL_PROCESS.value()` 时，会根据 `callProcess` 启动子流程。

- `callProcess`：子流程定义，支持 `processKey`、`processKey:version`、`processId`。
- `callAsync = true`：启动子流程后，父流程继续向下执行。
- `callAsync = false` 或未配置：父流程先生成一个 `CALL_PROCESS` 等待任务，再启动子流程。

默认子流程处理器是 `DefaultFlowSubProcessHandler`：

- 子流程正常完成或自动通过：关闭父流程等待任务，合并子流程变量，继续执行父流程后续节点。
- 子流程拒绝或自动拒绝：关闭父流程等待任务，并将父流程结束为拒绝。
- 子流程撤销、终止、超时、作废：父流程按相同语义结束。
- 删除父流程实例时，会递归删除活动和历史子流程实例、任务和模型数据。

如果需要绑定业务单据、复制变量、改变子流程结束后的父流程策略，可以通过注册 `FlowSubProcessHandler` Bean 或 `FlowEngineBuilder.subProcessHandler(...)` 替换。

## 已迁移能力

- JSON 流程定义部署与版本管理。
- 活动表、历史表流转模型。
- 发起、审批、抄送、条件分支、并行分支、包容分支、自动通过、自动拒绝、结束节点。
- 路由分支，可根据条件或 AI 决策跳到目标节点，并记录路由跳转历史状态。
- 会签、或签、顺序审批、票签任务创建和完成后收口。
- 驳回到指定节点、发起人、上级审批节点、上一审批节点、终止审批。
- 转办、委派、归还、代理、认领、加签、减签。
- 撤回、拿回、恢复实例。
- 超时、提醒、触发任务扩展。
- 子流程默认启动和同步等待任务。
- 子流程父子实例联动，支持同步等待任务恢复、子流程变量合并、异常状态向父流程传递、父流程删除递归清理子流程。
- starter 自动配置和 MyBatis-Plus 默认持久化。
- 按流程定义 ID 启动流程、按 processKey 启动流程。
- 启动流程时传业务 key、自定义实例 supplier、暂存草稿。
- 自动完成任务、触发器完成、自动驳回、任意跳转。
- 任务已阅、角色认领、部门认领、修改任务办理人。
- 实例级流程模型缓存、重载、追加临时节点、移除临时节点。
- 线程内动态分支指定和动态办理人传递。
- AI 节点处理、AI 分支决策、变量回写和失败兜底策略。
- 流程模型部署前增强校验，覆盖重复节点、分支结构、路由目标、参与人配置、票签权重、可结束路径等。
- 调度门面和本地调度锁。
- 路由跳转历史状态、路由跳转事件、路由目标任务类型标记。
- 任务和实例事件覆盖补齐，支持外围模块统一监听落库、消息、审计。
- 会签、或签、票签支持投反对票、弃权、拒绝权重和加权否决策略。
- 设计器模型校验增强，覆盖节点名称/长度、节点 key 空白字符、分支优先级重复、条件操作符和多人审批策略配置。

## 外围需要实现的能力

- 用户、角色、部门、岗位等组织解析。
- 角色/部门认领前的成员权限判断。
- 消息提醒、任务到期扫描调度、业务事件发布。
- 管理端流程设计器、表单页面、审批页面。

## 表单与业务审批接入

`nkk-kit-flow-web` 模块提供了表单元数据和业务审批的扩展 SPI，让使用方可以把自定义表单、业务表单接入流程设计器和运行时。

### FlowDesignerFormProvider — 表单数据提供者

设计器条件分支节点需要可选字段时，通过此 SPI 查询表单字段元数据。

```java
@Bean
public FlowDesignerFormProvider flowDesignerFormProvider() {
    return new FlowDesignerFormProvider() {
        @Override
        public List<FlowDesignerFormOption> listForms(String sourceType) {
            // 返回表单下拉选项，sourceType=form 自定义表单，business 业务表单
        }

        @Override
        public List<FlowFieldMeta> listFormFields(String formKey, Integer formVersion, String sourceType) {
            // 返回指定表单的字段元数据
        }
    };
}
```

未实现时返回空列表，流程发布时前端会把字段快照直接写入 `extendConfig.metaForm.fields`，后续条件分支和表单渲染从流程模型 JSON 读取即可。

### FlowApprovalCallbackHandler — 审批结果回调

审批引擎在实例/任务生命周期节点触发回调，业务系统同步自己的单据状态。推荐在业务 Service 类上直接实现此接口，同时配合 `@FlowApproval` 注解完成"发起 + 回调"的闭环。

```java
@Service
public class OrderService implements FlowApprovalCallbackHandler {

    private final OrderRepository orderRepository;

    // @FlowApproval 发起，FlowApprovalCallbackHandler 回调
    @Transactional
    @FlowApproval(processKey = "purchase-order", businessKeyFrom = "orderNo")
    public Order submit(Order order) {
        return orderRepository.save(order);
    }

    /** 只处理订单审批流程。 */
    @Override
    public boolean supports(String processKey) {
        return "purchase-order".equals(processKey);
    }

    @Override
    public void onInstanceComplete(FlowHisInstance instance) {
        orderRepository.updateFlowStatus(instance.getBusinessKey(), "APPROVED");
    }

    @Override
    public void onInstanceReject(FlowHisInstance instance) {
        orderRepository.updateFlowStatus(instance.getBusinessKey(), "REJECTED");
    }
}
```

**`supports(processKey)` 是路由过滤器**：适配器在 dispatch 前先调用它，只有返回 true 的 Handler 才会收到回调。可以有多个 Handler Bean，按 order 值从小到大逐个触发。某个 Handler 抛异常不影响其他 Handler 和主流程。

### FlowBusinessApprovalLauncher — 业务发起审批桥接

业务系统通过此 SPI 便捷地把业务单据发起为审批实例：

```java
@Bean
public FlowBusinessApprovalLauncher flowBusinessApprovalLauncher(FlowDesignerRuntimeService runtimeService) {
    return request -> {
        FlowStartProcessRequest start = new FlowStartProcessRequest();
        start.setProcessKey(request.getProcessKey());
        start.setBusinessKey(request.getBusinessKey());
        start.setVariables(request.getVariables());
        return runtimeService.start(start).getInstanceId();
    };
}
```

### @FlowApproval — 无侵入式自动发起

业务方法加注解即可自动发起审批，详见 `nkk-kit-flow-web/README.md` 的「无侵入式自动发起审批」章节。

```java
@Transactional
@FlowApproval(processKey = "purchase-order", businessKeyFrom = "orderNo")
public Order submit(Order order) {
    return orderRepository.save(order);
}
```

## 模型校验

部署流程定义时会自动执行 `FlowModelValidator.validate(model)`。当前校验范围：

- 流程 key、根节点、节点 key、节点类型不能为空。
- 节点 key 全流程唯一。
- 根节点必须是发起节点。
- 至少存在一个审批节点。
- 至少存在一条可结束路径。
- 条件、并行、包容、路由分支必须配置对应分支列表。
- 条件、包容、路由分支最多只能有一个空条件默认分支。
- 无公共后续节点时，普通条件分支最多只能有一个空子节点分支。
- 路由目标节点必须存在，且不能指向自身。
- 审批/抄送节点按 `setType` 校验参与人配置。
- 票签 `passWeight` 必须在 1-100 之间，参与人 `weight` 必须在 0-100 之间。
- 子流程节点必须配置 `callProcess`。
- 节点名称不能为空，节点 key 不能包含空白字符。
- 分支 `priorityLevel` 不能重复。
- 条件操作符仅支持 `eq`、`ne`、`gt`、`ge`、`lt`、`le` 及对应符号。
- 多人审批策略 `rejectWeight`、`vetoWeight`、`allowAbstain`、`abstainAsPass` 类型和值必须合法。

