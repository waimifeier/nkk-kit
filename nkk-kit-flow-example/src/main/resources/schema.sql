DROP TABLE IF EXISTS flow_his_task_actor;
DROP TABLE IF EXISTS flow_form_field;
DROP TABLE IF EXISTS flow_process_form_binding;
DROP TABLE IF EXISTS flow_task_actor;
DROP TABLE IF EXISTS flow_his_task;
DROP TABLE IF EXISTS flow_task;
DROP TABLE IF EXISTS flow_ext_instance;
DROP TABLE IF EXISTS flow_instance;
DROP TABLE IF EXISTS flow_his_instance;
DROP TABLE IF EXISTS flow_process;

CREATE TABLE flow_process
(
    id              BIGINT       NOT NULL,
    tenant_id       VARCHAR(50),
    create_id       VARCHAR(50)  NOT NULL,
    create_by       VARCHAR(50)  NOT NULL,
    create_time     TIMESTAMP    NOT NULL,
    process_key     VARCHAR(100) NOT NULL,
    process_name    VARCHAR(100) NOT NULL,
    process_icon    VARCHAR(255),
    process_type    VARCHAR(100),
    process_version INT          NOT NULL DEFAULT 1,
    instance_url    VARCHAR(200),
    remark          VARCHAR(255),
    use_scope       TINYINT      NOT NULL DEFAULT 0,
    process_state   TINYINT      NOT NULL DEFAULT 1,
    model_content   CLOB,
    sort            INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE INDEX idx_flow_process_key_version ON flow_process (tenant_id, process_key, process_version);

CREATE TABLE flow_process_form_binding
(
    id           BIGINT       NOT NULL,
    tenant_id    VARCHAR(50),
    create_id    VARCHAR(50)  NOT NULL,
    create_by    VARCHAR(50)  NOT NULL,
    create_time  TIMESTAMP    NOT NULL,
    process_id   BIGINT       NOT NULL,
    source_type  VARCHAR(50)  NOT NULL,
    form_id      BIGINT,
    form_key     VARCHAR(100),
    form_version INT          NOT NULL DEFAULT 1,
    form_name    VARCHAR(100),
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_flow_process_form_binding_process ON flow_process_form_binding (process_id);
CREATE INDEX idx_flow_process_form_binding_form ON flow_process_form_binding (tenant_id, form_key, form_version);

CREATE TABLE flow_his_instance
(
    id                BIGINT       NOT NULL,
    tenant_id         VARCHAR(50),
    create_id         VARCHAR(50)  NOT NULL,
    create_by         VARCHAR(50)  NOT NULL,
    create_time       TIMESTAMP    NOT NULL,
    process_id        BIGINT       NOT NULL,
    parent_instance_id BIGINT,
    priority          TINYINT,
    instance_no       VARCHAR(50),
    business_key      VARCHAR(100),
    variable          CLOB,
    current_node_name VARCHAR(100) NOT NULL,
    current_node_key  VARCHAR(100) NOT NULL,
    expire_time       TIMESTAMP,
    last_update_by    VARCHAR(50),
    last_update_time  TIMESTAMP,
    instance_state    TINYINT      NOT NULL DEFAULT 0,
    end_time          TIMESTAMP,
    duration          BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX idx_flow_his_instance_process ON flow_his_instance (process_id);
CREATE INDEX idx_flow_his_instance_parent ON flow_his_instance (parent_instance_id);
CREATE INDEX idx_flow_his_instance_business ON flow_his_instance (tenant_id, business_key);

CREATE TABLE flow_instance
(
    id                BIGINT       NOT NULL,
    tenant_id         VARCHAR(50),
    create_id         VARCHAR(50)  NOT NULL,
    create_by         VARCHAR(50)  NOT NULL,
    create_time       TIMESTAMP    NOT NULL,
    process_id        BIGINT       NOT NULL,
    parent_instance_id BIGINT,
    priority          TINYINT,
    instance_no       VARCHAR(50),
    business_key      VARCHAR(100),
    variable          CLOB,
    current_node_name VARCHAR(100) NOT NULL,
    current_node_key  VARCHAR(100) NOT NULL,
    expire_time       TIMESTAMP,
    last_update_by    VARCHAR(50),
    last_update_time  TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_flow_instance_process ON flow_instance (process_id);
CREATE INDEX idx_flow_instance_parent ON flow_instance (parent_instance_id);
CREATE INDEX idx_flow_instance_business ON flow_instance (tenant_id, business_key);

CREATE TABLE flow_ext_instance
(
    id            BIGINT NOT NULL,
    tenant_id     VARCHAR(50),
    process_id    BIGINT NOT NULL,
    process_name  VARCHAR(100),
    process_type  VARCHAR(100),
    model_content CLOB,
    PRIMARY KEY (id)
);

CREATE INDEX idx_flow_ext_instance_process ON flow_ext_instance (process_id);

CREATE TABLE flow_task
(
    id              BIGINT       NOT NULL,
    tenant_id       VARCHAR(50),
    create_id       VARCHAR(50)  NOT NULL,
    create_by       VARCHAR(50)  NOT NULL,
    create_time     TIMESTAMP    NOT NULL,
    instance_id     BIGINT       NOT NULL,
    parent_task_id  BIGINT,
    call_process_id BIGINT,
    call_instance_id BIGINT,
    task_name       VARCHAR(100) NOT NULL,
    task_key        VARCHAR(100) NOT NULL,
    task_type       TINYINT      NOT NULL,
    perform_type    TINYINT,
    action_url      VARCHAR(200),
    variable        CLOB,
    opinion         VARCHAR(1000),
    assignor_id     VARCHAR(100),
    assignor        VARCHAR(255),
    expire_time     TIMESTAMP,
    remind_time     TIMESTAMP,
    remind_repeat   INT          NOT NULL DEFAULT 0,
    term_mode       TINYINT,
    viewed          TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE INDEX idx_flow_task_instance ON flow_task (instance_id);
CREATE INDEX idx_flow_task_key ON flow_task (instance_id, task_key);
CREATE INDEX idx_flow_task_expire_remind ON flow_task (expire_time, remind_time);

CREATE TABLE flow_his_task
(
    id              BIGINT       NOT NULL,
    tenant_id       VARCHAR(50),
    create_id       VARCHAR(50)  NOT NULL,
    create_by       VARCHAR(50)  NOT NULL,
    create_time     TIMESTAMP    NOT NULL,
    instance_id     BIGINT       NOT NULL,
    parent_task_id  BIGINT,
    call_process_id BIGINT,
    call_instance_id BIGINT,
    task_name       VARCHAR(100) NOT NULL,
    task_key        VARCHAR(100) NOT NULL,
    task_type       TINYINT      NOT NULL,
    perform_type    TINYINT,
    action_url      VARCHAR(200),
    variable        CLOB,
    opinion         VARCHAR(1000),
    assignor_id     VARCHAR(100),
    assignor        VARCHAR(255),
    expire_time     TIMESTAMP,
    remind_time     TIMESTAMP,
    remind_repeat   INT          NOT NULL DEFAULT 0,
    term_mode       TINYINT,
    viewed          TINYINT      NOT NULL DEFAULT 0,
    finish_time     TIMESTAMP,
    task_state      TINYINT      NOT NULL DEFAULT 0,
    duration        BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX idx_flow_his_task_instance ON flow_his_task (instance_id);
CREATE INDEX idx_flow_his_task_key ON flow_his_task (instance_id, task_key);
CREATE INDEX idx_flow_his_task_finish ON flow_his_task (finish_time);

CREATE TABLE flow_task_actor
(
    id          BIGINT       NOT NULL,
    tenant_id   VARCHAR(50),
    instance_id BIGINT       NOT NULL,
    task_id     BIGINT       NOT NULL,
    actor_id    VARCHAR(100) NOT NULL,
    actor_name  VARCHAR(100) NOT NULL,
    actor_type  INT          NOT NULL,
    weight      INT,
    agent_id    VARCHAR(100),
    agent_type  INT,
    ext         CLOB,
    PRIMARY KEY (id)
);

CREATE INDEX idx_flow_task_actor_task ON flow_task_actor (task_id);
CREATE INDEX idx_flow_task_actor_user ON flow_task_actor (actor_id, actor_type);

CREATE TABLE flow_his_task_actor
(
    id          BIGINT       NOT NULL,
    tenant_id   VARCHAR(50),
    instance_id BIGINT       NOT NULL,
    task_id     BIGINT       NOT NULL,
    actor_id    VARCHAR(100) NOT NULL,
    actor_name  VARCHAR(100) NOT NULL,
    actor_type  INT          NOT NULL,
    weight      INT,
    agent_id    VARCHAR(100),
    agent_type  INT,
    ext         CLOB,
    PRIMARY KEY (id)
);

CREATE INDEX idx_flow_his_task_actor_task ON flow_his_task_actor (task_id);
CREATE INDEX idx_flow_his_task_actor_user ON flow_his_task_actor (actor_id, actor_type);

CREATE TABLE flow_form_field
(
    id           BIGINT       NOT NULL,
    tenant_id    VARCHAR(50),
    create_id    VARCHAR(50)  NOT NULL,
    create_by    VARCHAR(50)  NOT NULL,
    create_time  TIMESTAMP    NOT NULL,
    form_id      BIGINT,
    form_key     VARCHAR(100),
    form_version INT          NOT NULL DEFAULT 1,
    field_key    VARCHAR(100) NOT NULL,
    field_name   VARCHAR(100) NOT NULL,
    field_type   VARCHAR(50)  NOT NULL,
    field_path   VARCHAR(255),
    source_type  VARCHAR(50)  NOT NULL,
    required     TINYINT      NOT NULL DEFAULT 0,
    options_json CLOB,
    sort         INT          NOT NULL DEFAULT 0,
    remark       VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE INDEX idx_flow_form_field_form ON flow_form_field (tenant_id, form_key, form_version);
CREATE INDEX idx_flow_form_field_key ON flow_form_field (field_key);

