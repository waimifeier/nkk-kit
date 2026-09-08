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

流程设计器的条件节点、字段选择器可以直接查询表单字段元数据。元数据由核心模块的 `flow_form_field` 表维护，Web 模块只提供查询入口。

当前接口：

| 接口 | 说明 |
| --- | --- |
| `GET /nkk/flow/designer/forms/{formKey}/fields` | 查询指定表单的字段元数据；`formVersion` 为空时返回最新版本。 |
| `POST /nkk/flow/designer/forms/{formKey}/fields` | 保存指定表单版本的字段元数据，保存时会覆盖同一 `formKey + formVersion` 的旧数据。 |

字段元数据约定：

- `fieldKey`：字段编码，设计器里用于唯一定位字段。
- `fieldName`：字段展示名。
- `fieldPath`：字段路径，适合处理嵌套对象或数组路径。
- `fieldType`：字段类型，例如 `string`、`number`、`date`、`datetime`。
- `sourceType`：字段来源，例如 `custom`、`business`。
- `optionsJson`：选择型字段的选项数据。

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
      "sourceType": "custom",
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
