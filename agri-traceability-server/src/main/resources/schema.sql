CREATE DATABASE IF NOT EXISTS `agri_traceability` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `agri_traceability`;

DROP TABLE IF EXISTS `pub_query_record`;
DROP TABLE IF EXISTS `chain_contract_config`;
DROP TABLE IF EXISTS `chain_evidence_record`;
DROP TABLE IF EXISTS `biz_certificate`;
DROP TABLE IF EXISTS `biz_test_report`;
DROP TABLE IF EXISTS `biz_product_batch`;
DROP TABLE IF EXISTS `biz_producer`;
DROP TABLE IF EXISTS `sys_role_menu`;
DROP TABLE IF EXISTS `sys_user_role`;
DROP TABLE IF EXISTS `sys_menu`;
DROP TABLE IF EXISTS `sys_role`;
DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '登录账号',
  `password` varchar(100) NOT NULL COMMENT '密码(BCrypt)',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态 0禁用 1启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_code` varchar(50) NOT NULL COMMENT '角色编码 ADMIN/REGULATOR/PRODUCER',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `description` varchar(200) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单ID',
  `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
  `menu_type` tinyint(1) DEFAULT '1' COMMENT '类型 1目录 2菜单 3按钮',
  `icon` varchar(50) DEFAULT NULL,
  `path` varchar(100) DEFAULT NULL COMMENT '路由路径',
  `component` varchar(100) DEFAULT NULL COMMENT '组件路径',
  `perms` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `sort_order` int DEFAULT '0',
  `status` tinyint(1) DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `menu_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`),
  CONSTRAINT `fk_role_menu_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`),
  CONSTRAINT `fk_role_menu_menu` FOREIGN KEY (`menu_id`) REFERENCES `sys_menu` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联';

CREATE TABLE `biz_producer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL COMMENT '关联登录用户',
  `producer_code` varchar(50) NOT NULL COMMENT '主体编号',
  `producer_name` varchar(100) NOT NULL COMMENT '主体名称',
  `producer_type` varchar(20) DEFAULT NULL COMMENT '类型 企业/合作社/家庭农场',
  `credit_code` varchar(50) NOT NULL COMMENT '统一社会信用代码',
  `legal_person` varchar(50) DEFAULT NULL COMMENT '法人',
  `contact_phone` varchar(20) DEFAULT NULL,
  `town_name` varchar(50) DEFAULT NULL COMMENT '所属乡镇',
  `address` varchar(200) DEFAULT NULL,
  `business_scope` varchar(500) DEFAULT NULL,
  `audit_status` tinyint(1) DEFAULT '0' COMMENT '审核状态 0待审核 1通过 2驳回',
  `audit_user_id` bigint DEFAULT NULL,
  `audit_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_producer_code` (`producer_code`),
  UNIQUE KEY `uk_credit_code` (`credit_code`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_deleted` (`deleted`),
  CONSTRAINT `fk_producer_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='经营主体';

CREATE TABLE `biz_product_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `batch_code` varchar(50) NOT NULL COMMENT '批次编号',
  `producer_id` bigint NOT NULL COMMENT '经营主体ID',
  `producer_code` varchar(50) NOT NULL,
  `producer_name` varchar(100) DEFAULT NULL,
  `product_name` varchar(100) NOT NULL COMMENT '产品名称',
  `product_category` varchar(50) DEFAULT NULL COMMENT '产品类别',
  `origin_address` varchar(200) DEFAULT NULL COMMENT '产地',
  `harvest_time` date DEFAULT NULL COMMENT '采收日期',
  `batch_weight` decimal(10,2) DEFAULT NULL COMMENT '批次重量',
  `unit` varchar(20) DEFAULT 'kg' COMMENT '单位',
  `expected_sale_time` date DEFAULT NULL,
  `batch_status` tinyint(1) DEFAULT '0' COMMENT '批次状态 0草稿 1待检测 2待开证 3已开证',
  `quality_status` tinyint(1) DEFAULT '0' COMMENT '质量状态 0未知 1合格 2不合格',
  `batch_hash` varchar(100) DEFAULT NULL COMMENT '批次哈希',
  `chain_status` tinyint(1) DEFAULT '0' COMMENT '上链状态 0未上链 1上链中 2成功 3失败',
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_code` (`batch_code`),
  KEY `idx_producer_id` (`producer_id`),
  KEY `idx_deleted` (`deleted`),
  CONSTRAINT `fk_batch_producer` FOREIGN KEY (`producer_id`) REFERENCES `biz_producer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农产品批次';

CREATE TABLE `biz_test_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `report_code` varchar(50) NOT NULL COMMENT '报告编号',
  `batch_id` bigint NOT NULL,
  `batch_code` varchar(50) NOT NULL,
  `producer_id` bigint NOT NULL,
  `test_agency` varchar(100) DEFAULT NULL COMMENT '检测机构',
  `test_type` varchar(50) DEFAULT NULL COMMENT '检测类型',
  `test_date` date DEFAULT NULL,
  `test_items` text COMMENT '检测项目',
  `test_result` text COMMENT '检测结果',
  `conclusion` varchar(20) DEFAULT NULL COMMENT '结论 合格/不合格',
  `report_file_url` varchar(500) DEFAULT NULL COMMENT '报告文件URL',
  `report_file_hash` varchar(100) DEFAULT NULL COMMENT '文件SHA256',
  `chain_status` tinyint(1) DEFAULT '0',
  `create_user_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report_code` (`report_code`),
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_producer_id` (`producer_id`),
  KEY `idx_deleted` (`deleted`),
  CONSTRAINT `fk_report_batch` FOREIGN KEY (`batch_id`) REFERENCES `biz_product_batch` (`id`),
  CONSTRAINT `fk_report_producer` FOREIGN KEY (`producer_id`) REFERENCES `biz_producer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测报告';

CREATE TABLE `biz_certificate` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `certificate_code` varchar(50) DEFAULT NULL COMMENT '合格证编号（审核通过后生成）',
  `batch_id` bigint NOT NULL,
  `batch_code` varchar(50) NOT NULL,
  `report_id` bigint NOT NULL,
  `report_code` varchar(50) DEFAULT NULL,
  `producer_id` bigint NOT NULL,
  `producer_name` varchar(100) DEFAULT NULL,
  `issue_status` tinyint(1) DEFAULT '0' COMMENT '0草稿 1待审核 2审核通过 3审核驳回 4已签发',
  `apply_time` datetime DEFAULT NULL,
  `audit_user_id` bigint DEFAULT NULL,
  `audit_time` datetime DEFAULT NULL,
  `audit_opinion` varchar(500) DEFAULT NULL,
  `issue_time` datetime DEFAULT NULL,
  `expire_time` date DEFAULT NULL COMMENT '有效期至',
  `qr_code_url` varchar(500) DEFAULT NULL COMMENT '二维码内容（URL）',
  `certificate_hash` varchar(100) DEFAULT NULL,
  `chain_status` tinyint(1) DEFAULT '0',
  `revoke_status` tinyint(1) DEFAULT '0' COMMENT '0正常 1已作废',
  `revoke_reason` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_certificate_code` (`certificate_code`),
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_producer_id` (`producer_id`),
  KEY `idx_issue_status` (`issue_status`),
  KEY `idx_deleted` (`deleted`),
  CONSTRAINT `fk_cert_batch` FOREIGN KEY (`batch_id`) REFERENCES `biz_product_batch` (`id`),
  CONSTRAINT `fk_cert_report` FOREIGN KEY (`report_id`) REFERENCES `biz_test_report` (`id`),
  CONSTRAINT `fk_cert_producer` FOREIGN KEY (`producer_id`) REFERENCES `biz_producer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合格证';

CREATE TABLE `chain_evidence_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `business_type` varchar(20) NOT NULL COMMENT 'BATCH/REPORT/CERT/REVOKE',
  `business_id` bigint NOT NULL COMMENT '业务表主键',
  `business_code` varchar(50) NOT NULL COMMENT '业务编号',
  `evidence_hash` varchar(100) NOT NULL COMMENT '存证哈希',
  `related_code` varchar(50) DEFAULT NULL COMMENT '关联编号',
  `contract_address` varchar(100) DEFAULT NULL,
  `contract_method` varchar(50) DEFAULT NULL,
  `tx_hash` varchar(100) DEFAULT NULL COMMENT '交易哈希',
  `block_number` bigint DEFAULT NULL COMMENT '区块高度',
  `chain_status` tinyint(1) DEFAULT '1' COMMENT '0未上链 1上链中 2成功 3失败',
  `chain_time` datetime DEFAULT NULL,
  `verify_status` tinyint(1) DEFAULT '0' COMMENT '核验状态 0未核验 1通过 2失败',
  `verify_time` datetime DEFAULT NULL,
  `error_message` text,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_business` (`business_type`,`business_id`),
  KEY `idx_tx_hash` (`tx_hash`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区块链存证记录';

CREATE TABLE `chain_contract_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(50) NOT NULL COMMENT '配置项',
  `config_value` text NOT NULL COMMENT '配置值',
  `description` varchar(200) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合约配置';

CREATE TABLE `pub_query_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `certificate_code` varchar(50) NOT NULL,
  `query_ip` varchar(50) DEFAULT NULL,
  `query_result` tinyint(1) DEFAULT '1' COMMENT '1成功 0失败',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_cert_code` (`certificate_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公众查询记录';
