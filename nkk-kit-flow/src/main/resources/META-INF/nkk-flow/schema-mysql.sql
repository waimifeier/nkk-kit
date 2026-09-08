SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `flow_his_task_actor`;
DROP TABLE IF EXISTS `flow_form_field`;
DROP TABLE IF EXISTS `flow_process_form_binding`;
DROP TABLE IF EXISTS `flow_task_actor`;
DROP TABLE IF EXISTS `flow_his_task`;
DROP TABLE IF EXISTS `flow_task`;
DROP TABLE IF EXISTS `flow_ext_instance`;
DROP TABLE IF EXISTS `flow_instance`;
DROP TABLE IF EXISTS `flow_his_instance`;
DROP TABLE IF EXISTS `flow_process`;

CREATE TABLE `flow_process`
(
    `id`              bigint       NOT NULL COMMENT '主键ID',
    `tenant_id`       varchar(50)           DEFAULT NULL COMMENT '租户ID',
    `create_id`       varchar(50)  NOT NULL COMMENT '创建人ID',
    `create_by`       varchar(50)  NOT NULL COMMENT '创建人名称',
    `create_time`     datetime     NOT NULL COMMENT '创建时间',
    `process_key`     varchar(100) NOT NULL COMMENT '流程定义key',
    `process_name`    varchar(100) NOT NULL COMMENT '流程定义名称',
    `process_icon`    varchar(255)          DEFAULT NULL COMMENT '流程图标',
    `process_type`    varchar(100)          DEFAULT NULL COMMENT '流程类型',
    `process_version` int          NOT NULL DEFAULT 1 COMMENT '流程版本，默认 1',
    `instance_url`    varchar(200)          DEFAULT NULL COMMENT '实例地址',
    `remark`          varchar(255)          DEFAULT NULL COMMENT '备注',
    `use_scope`       tinyint      NOT NULL DEFAULT 0 COMMENT '使用范围',
    `process_state`   tinyint      NOT NULL DEFAULT 1 COMMENT '流程状态 0，不可用 1，可用 2，历史版本',
    `model_content`   longtext              DEFAULT NULL COMMENT '流程模型定义JSON内容',
    `sort`            int          NOT NULL DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (`id`),
    KEY `idx_flow_process_key_version` (`tenant_id`, `process_key`, `process_version`),
    KEY `idx_flow_process_name` (`process_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '流程定义表';

CREATE TABLE `flow_process_form_binding`
(
    `id`            bigint       NOT NULL COMMENT '主键ID',
    `tenant_id`     varchar(50)           DEFAULT NULL COMMENT '租户ID',
    `create_id`     varchar(50)  NOT NULL COMMENT '创建人ID',
    `create_by`     varchar(50)  NOT NULL COMMENT '创建人名称',
    `create_time`   datetime     NOT NULL COMMENT '创建时间',
    `process_id`    bigint       NOT NULL COMMENT '流程定义ID',
    `source_type`   varchar(50)  NOT NULL COMMENT '表单来源类型，取值：form(表单)、business(业务表单)',
    `form_id`       bigint                DEFAULT NULL COMMENT '表单ID',
    `form_key`      varchar(100)          DEFAULT NULL COMMENT '表单编码',
    `form_version`  int          NOT NULL DEFAULT 1 COMMENT '表单版本',
    `form_name`     varchar(100)          DEFAULT NULL COMMENT '表单名称',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_flow_process_form_binding_process` (`process_id`),
    KEY `idx_flow_process_form_binding_form` (`tenant_id`, `form_key`, `form_version`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '流程表单绑定表';

CREATE TABLE `flow_his_instance`
(
    `id`                bigint       NOT NULL COMMENT '主键ID',
    `tenant_id`         varchar(50)           DEFAULT NULL COMMENT '租户ID',
    `create_id`         varchar(50)  NOT NULL COMMENT '创建人ID',
    `create_by`         varchar(50)  NOT NULL COMMENT '创建人名称',
    `create_time`       datetime     NOT NULL COMMENT '创建时间',
    `process_id`        bigint       NOT NULL COMMENT '流程定义ID',
    `parent_instance_id` bigint               DEFAULT NULL COMMENT '父流程实例ID',
    `priority`          tinyint               DEFAULT NULL COMMENT '优先级',
    `instance_no`       varchar(50)           DEFAULT NULL COMMENT '流程实例编号',
    `business_key`      varchar(100)          DEFAULT NULL COMMENT '业务KEY',
    `variable`          longtext              DEFAULT NULL COMMENT '变量JSON',
    `current_node_name` varchar(100) NOT NULL COMMENT '当前节点名称',
    `current_node_key`  varchar(100) NOT NULL COMMENT '当前节点key',
    `expire_time`       datetime              DEFAULT NULL COMMENT '期望完成时间',
    `last_update_by`    varchar(50)           DEFAULT NULL COMMENT '最后更新人',
    `last_update_time`  datetime              DEFAULT NULL COMMENT '最后更新时间',
    `instance_state`    tinyint      NOT NULL DEFAULT 0 COMMENT '实例状态  -2，已暂停状态 -1，暂存待审 0，审批中 1，审批通过 2，审批拒绝 3，撤销审批 4，超时结束 5，强制终止 6，自动通过 7，自动拒绝',
    `end_time`          datetime              DEFAULT NULL COMMENT '结束时间',
    `duration`          bigint                DEFAULT NULL COMMENT '处理耗时毫秒',
    PRIMARY KEY (`id`),
    KEY `idx_flow_his_instance_process` (`process_id`),
    KEY `idx_flow_his_instance_parent` (`parent_instance_id`),
    KEY `idx_flow_his_instance_business` (`tenant_id`, `business_key`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '历史流程实例表';

CREATE TABLE `flow_instance`
(
    `id`                bigint       NOT NULL COMMENT '主键ID',
    `tenant_id`         varchar(50)           DEFAULT NULL COMMENT '租户ID',
    `create_id`         varchar(50)  NOT NULL COMMENT '创建人ID',
    `create_by`         varchar(50)  NOT NULL COMMENT '创建人名称',
    `create_time`       datetime     NOT NULL COMMENT '创建时间',
    `process_id`        bigint       NOT NULL COMMENT '流程定义ID',
    `parent_instance_id` bigint               DEFAULT NULL COMMENT '父流程实例ID',
    `priority`          tinyint               DEFAULT NULL COMMENT '优先级',
    `instance_no`       varchar(50)           DEFAULT NULL COMMENT '流程实例编号',
    `business_key`      varchar(100)          DEFAULT NULL COMMENT '业务KEY',
    `variable`          longtext              DEFAULT NULL COMMENT '变量JSON',
    `current_node_name` varchar(100) NOT NULL COMMENT '当前节点名称',
    `current_node_key`  varchar(100) NOT NULL COMMENT '当前节点key',
    `expire_time`       datetime              DEFAULT NULL COMMENT '期望完成时间',
    `last_update_by`    varchar(50)           DEFAULT NULL COMMENT '最后更新人',
    `last_update_time`  datetime              DEFAULT NULL COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_flow_instance_process` (`process_id`),
    KEY `idx_flow_instance_parent` (`parent_instance_id`),
    KEY `idx_flow_instance_business` (`tenant_id`, `business_key`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '活动流程实例表';

CREATE TABLE `flow_ext_instance`
(
    `id`            bigint NOT NULL COMMENT '流程实例ID',
    `tenant_id`     varchar(50)  DEFAULT NULL COMMENT '租户ID',
    `process_id`    bigint NOT NULL COMMENT '流程定义ID',
    `process_name`  varchar(100) DEFAULT NULL COMMENT '流程名称',
    `process_type`  varchar(100) DEFAULT NULL COMMENT '流程类型',
    `model_content` longtext     DEFAULT NULL COMMENT '流程模型JSON',
    PRIMARY KEY (`id`),
    KEY `idx_flow_ext_instance_process` (`process_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '流程实例扩展表';

CREATE TABLE `flow_task`
(
    `id`             bigint       NOT NULL COMMENT '主键ID',
    `tenant_id`      varchar(50)           DEFAULT NULL COMMENT '租户ID',
    `create_id`      varchar(50)  NOT NULL COMMENT '创建人ID',
    `create_by`      varchar(50)  NOT NULL COMMENT '创建人名称',
    `create_time`    datetime     NOT NULL COMMENT '创建时间',
    `instance_id`    bigint       NOT NULL COMMENT '流程实例ID',
    `parent_task_id` bigint                DEFAULT NULL COMMENT '父任务ID',
    `call_process_id` bigint              DEFAULT NULL COMMENT '调用子流程定义ID',
    `call_instance_id` bigint             DEFAULT NULL COMMENT '调用子流程实例ID',
    `task_name`      varchar(100) NOT NULL COMMENT '任务名称',
    `task_key`       varchar(100) NOT NULL COMMENT '任务key 唯一标识',
    `task_type`      tinyint      NOT NULL COMMENT '任务类型 -1，结束节点 0，主办 1，审批 2，抄送 3，条件审批 4，条件分支 5，调用外部流程任务 6，定时器任务 7，触发器任务 8，并行分支 9，包容分支 10，转办 11，委派 12，委派归还 13，代理人任务 14，代理人归还 15，代理人协办 16，被代理人自己完成 17，拿回任务 18，待撤回历史任务 19，拒绝任务 20，跳转任务 21，驳回跳转 22，路由跳转 23，路由分支 24，驳回重新审批跳转 25，暂存待审 30，自动通过 31，自动拒绝',
    `perform_type`   tinyint               DEFAULT NULL COMMENT '参与方式  0，发起 1，按顺序依次审批 2，会签 3，或签 4，票签 6，定时器 7，触发器 9，抄送',
    `action_url`     varchar(200)          DEFAULT NULL COMMENT '任务处理URL',
    `variable`       longtext              DEFAULT NULL COMMENT '变量JSON',
    `opinion`        varchar(1000)         DEFAULT NULL COMMENT '审批意见',
    `assignor_id`    varchar(100)          DEFAULT NULL COMMENT '委托人ID',
    `assignor`       varchar(255)          DEFAULT NULL COMMENT '委托人',
    `expire_time`    datetime              DEFAULT NULL COMMENT '任务到期时间',
    `remind_time`    datetime              DEFAULT NULL COMMENT '提醒时间',
    `remind_repeat`  int          NOT NULL DEFAULT 0 COMMENT '提醒次数',
    `term_mode`      tinyint               DEFAULT NULL COMMENT '超时处理模式',
    `viewed`         tinyint      NOT NULL DEFAULT 0 COMMENT '已阅  0，否 1，是',
    PRIMARY KEY (`id`),
    KEY `idx_flow_task_instance` (`instance_id`),
    KEY `idx_flow_task_key` (`instance_id`, `task_key`),
    KEY `idx_flow_task_expire_remind` (`expire_time`, `remind_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '活动任务表';

CREATE TABLE `flow_his_task`
(
    `id`             bigint       NOT NULL COMMENT '主键ID',
    `tenant_id`      varchar(50)           DEFAULT NULL COMMENT '租户ID',
    `create_id`      varchar(50)  NOT NULL COMMENT '创建人ID',
    `create_by`      varchar(50)  NOT NULL COMMENT '创建人名称',
    `create_time`    datetime     NOT NULL COMMENT '创建时间',
    `instance_id`    bigint       NOT NULL COMMENT '流程实例ID',
    `parent_task_id` bigint                DEFAULT NULL COMMENT '父任务ID',
    `call_process_id` bigint              DEFAULT NULL COMMENT '调用子流程定义ID',
    `call_instance_id` bigint             DEFAULT NULL COMMENT '调用子流程实例ID',
    `task_name`      varchar(100) NOT NULL COMMENT '任务名称',
    `task_key`       varchar(100) NOT NULL COMMENT '任务key',
    `task_type`      tinyint      NOT NULL COMMENT '任务类型 -1，结束节点 0，主办 1，审批 2，抄送 3，条件审批 4，条件分支 5，调用外部流程任务 6，定时器任务 7，触发器任务 8，并行分支 9，包容分支 10，转办 11，委派 12，委派归还 13，代理人任务 14，代理人归还 15，代理人协办 16，被代理人自己完成 17，拿回任务 18，待撤回历史任务 19，拒绝任务 20，跳转任务 21，驳回跳转 22，路由跳转 23，路由分支 24，驳回重新审批跳转 25，暂存待审 30，自动通过 31，自动拒绝',
    `perform_type`   tinyint               DEFAULT NULL COMMENT '参与类型 0，发起 1，按顺序依次审批 2，会签 3，或签 4，票签 6，定时器 7，触发器 9，抄送',
    `action_url`     varchar(200)          DEFAULT NULL COMMENT '任务处理URL',
    `variable`       longtext              DEFAULT NULL COMMENT '变量JSON',
    `opinion`        varchar(1000)         DEFAULT NULL COMMENT '审批意见',
    `assignor_id`    varchar(100)          DEFAULT NULL COMMENT '委托人ID',
    `assignor`       varchar(255)          DEFAULT NULL COMMENT '委托人',
    `expire_time`    datetime              DEFAULT NULL COMMENT '任务到期时间',
    `remind_time`    datetime              DEFAULT NULL COMMENT '提醒时间',
    `remind_repeat`  int          NOT NULL DEFAULT 0 COMMENT '提醒次数',
    `term_mode`      tinyint               DEFAULT NULL COMMENT '超时处理模式',
    `viewed`         tinyint      NOT NULL DEFAULT 0 COMMENT '已阅 0，否 1，是',
    `finish_time`    datetime              DEFAULT NULL COMMENT '完成时间',
    `task_state`     tinyint      NOT NULL DEFAULT 0 COMMENT '任务状态 0，活动 1，跳转 2，完成 3，拒绝 4，撤销审批 5，超时 6，终止 7，驳回终止 8，自动完成 9，自动驳回 10，自动跳转 11，驳回跳转 12，驳回重新审批跳转 13，路由跳转',
    `duration`       bigint                DEFAULT NULL COMMENT '处理耗时毫秒',
    PRIMARY KEY (`id`),
    KEY `idx_flow_his_task_instance` (`instance_id`),
    KEY `idx_flow_his_task_key` (`instance_id`, `task_key`),
    KEY `idx_flow_his_task_finish` (`finish_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '历史任务表';

CREATE TABLE `flow_task_actor`
(
    `id`          bigint       NOT NULL COMMENT '主键ID',
    `tenant_id`   varchar(50)           DEFAULT NULL COMMENT '租户ID',
    `instance_id` bigint       NOT NULL COMMENT '流程实例ID',
    `task_id`     bigint       NOT NULL COMMENT '任务ID',
    `actor_id`    varchar(100) NOT NULL COMMENT '参与者ID',
    `actor_name`  varchar(100) NOT NULL COMMENT '参与者名称',
    `actor_type`  int          NOT NULL COMMENT '参与者类型',
    `weight`      int                   DEFAULT NULL COMMENT '权重',
    `agent_id`    varchar(100)          DEFAULT NULL COMMENT '代理人ID',
    `agent_type`  int                   DEFAULT NULL COMMENT '代理类型',
    `ext`         longtext              DEFAULT NULL COMMENT '扩展JSON',
    PRIMARY KEY (`id`),
    KEY `idx_flow_task_actor_task` (`task_id`),
    KEY `idx_flow_task_actor_user` (`actor_id`, `actor_type`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '活动任务参与者表';

CREATE TABLE `flow_his_task_actor`
(
    `id`          bigint       NOT NULL COMMENT '主键ID',
    `tenant_id`   varchar(50)           DEFAULT NULL COMMENT '租户ID',
    `instance_id` bigint       NOT NULL COMMENT '流程实例ID',
    `task_id`     bigint       NOT NULL COMMENT '任务ID',
    `actor_id`    varchar(100) NOT NULL COMMENT '参与者ID',
    `actor_name`  varchar(100) NOT NULL COMMENT '参与者名称',
    `actor_type`  int          NOT NULL COMMENT '参与者类型',
    `weight`      int                   DEFAULT NULL COMMENT '权重',
    `agent_id`    varchar(100)          DEFAULT NULL COMMENT '代理人ID',
    `agent_type`  int                   DEFAULT NULL COMMENT '代理类型',
    `ext`         longtext              DEFAULT NULL COMMENT '扩展JSON',
    PRIMARY KEY (`id`),
    KEY `idx_flow_his_task_actor_task` (`task_id`),
    KEY `idx_flow_his_task_actor_user` (`actor_id`, `actor_type`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '历史任务参与者表';

CREATE TABLE `flow_form_field`
(
    `id`            bigint       NOT NULL COMMENT '主键ID',
    `tenant_id`     varchar(50)           DEFAULT NULL COMMENT '租户ID',
    `create_id`     varchar(50)  NOT NULL COMMENT '创建人ID',
    `create_by`     varchar(50)  NOT NULL COMMENT '创建人名称',
    `create_time`   datetime     NOT NULL COMMENT '创建时间',
    `form_id`       bigint                DEFAULT NULL COMMENT '表单ID',
    `form_key`      varchar(100)          DEFAULT NULL COMMENT '表单编码',
    `form_version`  int          NOT NULL DEFAULT 1 COMMENT '表单版本',
    `field_key`     varchar(100) NOT NULL COMMENT '字段编码',
    `field_name`    varchar(100) NOT NULL COMMENT '字段名称',
    `field_type`    varchar(50)  NOT NULL COMMENT '字段类型，取值：string(文本)、number(数字)、boolean(布尔)、date(日期)、datetime(日期时间)、select(单选)、multi_select(多选)、radio(单选按钮)、checkbox(复选框)、object(对象)、array(数组)',
    `field_path`    varchar(255)          DEFAULT NULL COMMENT '字段路径',
    `source_type`   varchar(50)  NOT NULL COMMENT '字段来源类型，取值：form(自定义表单)、business(业务表单)',
    `required`      tinyint      NOT NULL DEFAULT 0 COMMENT '是否必填 0 否 1 是',
    `options_json`  longtext              DEFAULT NULL COMMENT '字段选项JSON',
    `sort`          int          NOT NULL DEFAULT 0 COMMENT '排序',
    `remark`        varchar(255)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_flow_form_field_form` (`tenant_id`, `form_key`, `form_version`),
    KEY `idx_flow_form_field_key` (`field_key`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '流程表单字段元数据表';

SET FOREIGN_KEY_CHECKS = 1;

