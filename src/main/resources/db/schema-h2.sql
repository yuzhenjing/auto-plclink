-- ================================
-- PLC标签配置表
-- 用于存储PLC数据点的配置信息
-- ================================
CREATE TABLE IF NOT EXISTS plc_tag_config
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tag_name    VARCHAR(100) NOT NULL UNIQUE COMMENT '标签名称',
    address     VARCHAR(50)  NOT NULL COMMENT 'PLC地址',
    data_type   VARCHAR(20)  NOT NULL COMMENT '数据类型',
    description VARCHAR(255) COMMENT '标签描述',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    -- 添加约束
    CONSTRAINT chk_tag_name_not_empty CHECK (tag_name <> ''),
    CONSTRAINT chk_address_not_empty CHECK (address <> ''),
    CONSTRAINT chk_data_type_not_empty CHECK (data_type <> '')
);
-- 添加表注释
COMMENT ON TABLE plc_tag_config IS 'PLC标签配置表';

-- ================================
-- PLC数据记录表
-- 用于存储从PLC读取的实时数据
-- ================================
CREATE TABLE IF NOT EXISTS plc_data_record
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tag_name  VARCHAR(100) NOT NULL COMMENT '标签名称',
    tag_value VARCHAR(255) COMMENT '数据值',
    data_type VARCHAR(20) COMMENT '数据类型',
    quality   VARCHAR(20) COMMENT '数据质量',
    timestamp TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间戳',
    -- 添加约束
    CONSTRAINT chk_data_tag_name_not_empty CHECK (tag_name <> '')
);
-- 添加表注释
COMMENT ON TABLE plc_data_record IS 'PLC数据记录表';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_tag_name ON plc_data_record (tag_name);
CREATE INDEX IF NOT EXISTS idx_timestamp ON plc_data_record (timestamp);
CREATE INDEX IF NOT EXISTS idx_tag_timestamp ON plc_data_record (tag_name, timestamp);

-- ================================
-- PLC连接配置表
-- 用于存储PLC连接参数配置
-- ================================
CREATE TABLE IF NOT EXISTS plc_connect_config
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    host        VARCHAR(50) NOT NULL COMMENT 'PLC IP地址',
    port        INTEGER     NOT NULL COMMENT 'PLC端口号',
    rack        INTEGER COMMENT 'PLC机架号',
    slot        INTEGER COMMENT 'PLC插槽号',
    timeout     INTEGER     DEFAULT 5000 COMMENT '连接超时时间(毫秒)',
    protocol    VARCHAR(20) DEFAULT 'S7' COMMENT '通信协议',
    is_active   BOOLEAN     DEFAULT TRUE COMMENT '是否激活',
    create_time TIMESTAMP   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 添加约束
    CONSTRAINT chk_host_not_empty CHECK (host <> ''),
    CONSTRAINT chk_port_range CHECK (port > 0 AND port <= 65535),
    CONSTRAINT chk_timeout_positive CHECK (timeout > 0),
    CONSTRAINT chk_protocol_not_empty CHECK (protocol <> '')
);
-- 添加表注释
COMMENT ON TABLE plc_connect_config IS 'PLC连接配置表';

-- ================================
-- 报警记录表
-- 用于存储PLC报警信息
-- ================================
CREATE TABLE IF NOT EXISTS alarm_record
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tag_name          VARCHAR(100) NOT NULL COMMENT '标签名称',
    alarm_type        VARCHAR(50)  NOT NULL COMMENT '报警类型',
    alarm_message     VARCHAR(500) COMMENT '报警信息',
    alarm_level       VARCHAR(20) COMMENT '报警级别',
    alarm_value       VARCHAR(255) COMMENT '报警时的数据值',
    timestamp         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报警时间',
    is_acknowledged   BOOLEAN               DEFAULT FALSE COMMENT '是否已确认',
    acknowledged_time TIMESTAMP    NULL COMMENT '确认时间',

    -- 添加约束
    CONSTRAINT chk_alarm_tag_name_not_empty CHECK (tag_name <> ''),
    CONSTRAINT chk_alarm_type_not_empty CHECK (alarm_type <> '')
);
-- 添加表注释
COMMENT ON TABLE alarm_record IS '报警记录表';

-- 创建报警记录表索引
CREATE INDEX IF NOT EXISTS idx_alarm_tag_name ON alarm_record (tag_name);
CREATE INDEX IF NOT EXISTS idx_alarm_timestamp ON alarm_record (timestamp);
CREATE INDEX IF NOT EXISTS idx_alarm_level ON alarm_record (alarm_level);
CREATE INDEX IF NOT EXISTS idx_unacknowledged_alarms ON alarm_record (is_acknowledged, timestamp);
