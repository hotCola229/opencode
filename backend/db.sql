CREATE TABLE IF NOT EXISTS `external_call_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `trace_id` VARCHAR(64) DEFAULT NULL COMMENT '链路追踪ID',
    `service` VARCHAR(64) NOT NULL COMMENT '服务名称',
    `target_url` VARCHAR(512) NOT NULL COMMENT '目标URL',
    `http_method` VARCHAR(16) NOT NULL COMMENT 'HTTP方法',
    `query_string` VARCHAR(1024) DEFAULT NULL COMMENT '查询参数',
    `http_status` INT(11) DEFAULT NULL COMMENT 'HTTP状态码',
    `success` INT(11) NOT NULL COMMENT '是否成功：0-失败，1-成功',
    `attempt` INT(11) NOT NULL COMMENT '重试次数',
    `duration_ms` BIGINT(20) DEFAULT NULL COMMENT '耗时（毫秒）',
    `exception_type` VARCHAR(64) DEFAULT NULL COMMENT '异常类型',
    `exception_message` TEXT DEFAULT NULL COMMENT '异常信息',
    `created_at` DATETIME NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_trace_id` (`trace_id`),
    KEY `idx_service` (`service`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外部调用日志表';
