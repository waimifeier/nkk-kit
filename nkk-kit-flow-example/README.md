# nkk-kit-flow-example

`nkk-kit-flow-example` 是 `nkk-kit-flow-web` 的本地测试工程，默认使用 H2 内存数据库，启动后自动执行 `schema.sql` 建表。

该模块不再维护自己的流程测试 Controller，接口由 `nkk-kit-flow-web` 自动配置提供。示例工程只负责：

- 引入 `nkk-kit-flow-web`。
- 提供 H2 数据库配置。
- 提供测试用 `FlowCreatorProvider`，默认当前用户为 `1 / 系统管理员`，用于匹配示例 `请假.json` 的发起人范围。
- 提供测试用发起/审批权限策略，统一按 `nodeAssigneeList` 的 `actorType` 判断用户、角色、部门权限。
- 提供测试用 `FlowDesignerOrgProvider`。

## 启动

```bash
mvn -pl nkk-kit-flow-example -am -DskipTests install
cd nkk-kit-flow-example
mvn spring-boot:run
```

服务地址：

```text
http://localhost:18080
```

H2 控制台：

```text
http://localhost:18080/h2-console
JDBC URL: jdbc:h2:mem:nkk_flow_example
User: sa
Password:
```

## 设计器组织数据接口

```text
GET /nkk/flow/designer/org/departments/tree
GET /nkk/flow/designer/org/employees/tree
GET /nkk/flow/designer/org/roles
```

示例：

```bash
curl http://localhost:18080/nkk/flow/designer/org/departments/tree
curl http://localhost:18080/nkk/flow/designer/org/employees/tree
curl http://localhost:18080/nkk/flow/designer/org/roles
```

## 流程定义接口

```text
POST /nkk/flow/designer/processes/publish
GET  /nkk/flow/designer/processes/categories
GET  /nkk/flow/designer/processes/{processId}
POST /nkk/flow/designer/processes/{processId}/enable
POST /nkk/flow/designer/processes/{processId}/disable
PUT  /nkk/flow/designer/processes/{processId}/info
GET  /nkk/flow/designer/processes/key/{processKey}/versions
```

发布流程示例：

```bash
curl -X POST http://localhost:18080/nkk/flow/designer/processes/publish ^
  -H "Content-Type: application/json" ^
  -d "{\"processName\":\"请假审批\",\"processKey\":\"leave\",\"processIcon\":\"icon-leave\",\"processType\":\"hr\",\"instanceUrl\":\"/flow/instances/{instanceId}\",\"remark\":\"员工请假审批流程\",\"repeat\":true,\"modelContent\":{\"nodeKey\":\"start\",\"nodeName\":\"发起人\",\"type\":0,\"childNode\":{\"nodeKey\":\"audit\",\"nodeName\":\"主管审批\",\"type\":1,\"setType\":1,\"examineMode\":1,\"nodeAssigneeList\":[{\"id\":\"u2\",\"name\":\"李四\"}],\"childNode\":{\"nodeKey\":\"end\",\"nodeName\":\"结束\",\"type\":2}}}}"
```

查询版本：

```bash
curl http://localhost:18080/nkk/flow/designer/processes/key/leave/versions
```

按分类查询当前流程：

```bash
curl http://localhost:18080/nkk/flow/designer/processes/categories
```

修改基础信息：

```bash
curl -X PUT http://localhost:18080/nkk/flow/designer/processes/{processId}/info ^
  -H "Content-Type: application/json" ^
  -d "{\"processName\":\"请假审批V2\",\"processIcon\":\"icon-leave\",\"processType\":\"hr\",\"remark\":\"修改后的描述\"}"
```

## 流程运行时接口

```text
POST /nkk/flow/designer/runtime/instances/start
GET  /nkk/flow/designer/runtime/instances/{instanceId}
POST /nkk/flow/designer/runtime/instances/{instanceId}/submit
GET  /nkk/flow/designer/runtime/instances/{instanceId}/tasks
GET  /nkk/flow/designer/runtime/instances/{instanceId}/history/tasks
POST /nkk/flow/designer/runtime/instances/{instanceId}/revoke
POST /nkk/flow/designer/runtime/instances/{instanceId}/suspend
POST /nkk/flow/designer/runtime/instances/{instanceId}/active
POST /nkk/flow/designer/runtime/instances/{instanceId}/destroy
GET  /nkk/flow/designer/runtime/tasks/{taskId}
GET  /nkk/flow/designer/runtime/tasks/{taskId}/actors
POST /nkk/flow/designer/runtime/tasks/{taskId}/complete
POST /nkk/flow/designer/runtime/tasks/{taskId}/reject
POST /nkk/flow/designer/runtime/tasks/{taskId}/transfer
POST /nkk/flow/designer/runtime/tasks/{taskId}/delegate
POST /nkk/flow/designer/runtime/tasks/{taskId}/resolve
POST /nkk/flow/designer/runtime/tasks/{taskId}/view
POST /nkk/flow/designer/runtime/tasks/{taskId}/terminate
```

发起流程：

```bash
curl -X POST http://localhost:18080/nkk/flow/designer/runtime/instances/start ^
  -H "Content-Type: application/json" ^
  -d "{\"processKey\":\"leave\",\"businessKey\":\"leave-001\",\"variables\":{\"day\":3,\"reason\":\"事假\"}}"
```

暂存草稿：

```bash
curl -X POST http://localhost:18080/nkk/flow/designer/runtime/instances/start ^
  -H "Content-Type: application/json" ^
  -d "{\"processKey\":\"leave\",\"businessKey\":\"leave-001\",\"saveAsDraft\":true,\"variables\":{\"day\":3,\"reason\":\"事假\"}}"
```

提交草稿继续流转：

```bash
curl -X POST http://localhost:18080/nkk/flow/designer/runtime/instances/{instanceId}/submit ^
  -H "Content-Type: application/json" ^
  -d "{\"variables\":{\"day\":5,\"reason\":\"事假，补充修改\"}}"
```

办理通过：

```bash
curl -X POST http://localhost:18080/nkk/flow/designer/runtime/tasks/{taskId}/complete ^
  -H "Content-Type: application/json" ^
  -d "{\"variables\":{\"comment\":\"同意\"}}"
```
