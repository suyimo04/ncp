-- 食品溯源系统 数据库表结构
-- 创建数据库（如果还没有的话）
-- CREATE DATABASE IF NOT EXISTS food_trace DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
-- USE food_trace;

-- ========== 先删除所有表（按外键依赖反序） ==========
DROP TABLE IF EXISTS `sys_log`;
DROP TABLE IF EXISTS `profile_change_request`;
DROP TABLE IF EXISTS `sys_notice`;
DROP TABLE IF EXISTS `query_record`;
DROP TABLE IF EXISTS `blockchain_record`;
DROP TABLE IF EXISTS `sales_record`;
DROP TABLE IF EXISTS `storage_record`;
DROP TABLE IF EXISTS `logistics_track`;
DROP TABLE IF EXISTS `logistics_record`;
DROP TABLE IF EXISTS `quality_test`;
DROP TABLE IF EXISTS `production_batch`;
DROP TABLE IF EXISTS `food_info`;
DROP TABLE IF EXISTS `food_brand`;
DROP TABLE IF EXISTS `food_category`;
DROP TABLE IF EXISTS `enterprise_info`;
DROP TABLE IF EXISTS `sys_user`;

-- ========== 创建表 ==========

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `role` varchar(20) NOT NULL COMMENT '角色：ADMIN/PRODUCER/LOGISTICS/MERCHANT/CONSUMER',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-停用 1-正常',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 企业信息表
CREATE TABLE IF NOT EXISTS `enterprise_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '企业ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `enterprise_name` varchar(100) NOT NULL COMMENT '企业名称',
  `enterprise_type` varchar(20) NOT NULL COMMENT '企业类型：PRODUCER/LOGISTICS/MERCHANT',
  `license_no` varchar(50) DEFAULT NULL COMMENT '营业执照号',
  `license_image` varchar(255) DEFAULT NULL COMMENT '营业执照图片',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) DEFAULT NULL COMMENT '企业地址',
  `description` text COMMENT '企业简介',
  `audit_status` varchar(20) DEFAULT 'PENDING' COMMENT '审核状态：PENDING/APPROVED/REJECTED',
  `audit_remark` varchar(255) DEFAULT NULL COMMENT '审核备注',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_enterprise_type` (`enterprise_type`),
  KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业信息表';

-- 食品分类表
CREATE TABLE IF NOT EXISTS `food_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` varchar(50) NOT NULL COMMENT '分类名称',
  `parent_id` bigint DEFAULT 0 COMMENT '父级ID',
  `sort` int DEFAULT 0 COMMENT '排序',
  `status` tinyint DEFAULT 1 COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食品分类表';

-- 品牌表
CREATE TABLE IF NOT EXISTS `food_brand` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '品牌ID',
  `brand_name` varchar(50) NOT NULL COMMENT '品牌名称',
  `brand_logo` varchar(255) DEFAULT NULL COMMENT '品牌Logo',
  `description` varchar(255) DEFAULT NULL COMMENT '品牌描述',
  `status` tinyint DEFAULT 1 COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品牌表';

-- 食品档案表
CREATE TABLE IF NOT EXISTS `food_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '食品ID',
  `food_name` varchar(100) NOT NULL COMMENT '食品名称',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `brand_id` bigint DEFAULT NULL COMMENT '品牌ID',
  `producer_id` bigint NOT NULL COMMENT '生产企业ID',
  `specification` varchar(100) DEFAULT NULL COMMENT '规格',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `shelf_life` int DEFAULT NULL COMMENT '保质期（天）',
  `storage_condition` varchar(255) DEFAULT NULL COMMENT '储存条件',
  `food_image` varchar(255) DEFAULT NULL COMMENT '食品图片',
  `description` text COMMENT '食品描述',
  `status` tinyint DEFAULT 1 COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_producer_id` (`producer_id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食品档案表';

-- 生产批次表
CREATE TABLE IF NOT EXISTS `production_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '批次ID',
  `batch_no` varchar(50) NOT NULL COMMENT '批次号',
  `food_id` bigint NOT NULL COMMENT '食品ID',
  `producer_id` bigint NOT NULL COMMENT '生产企业ID',
  `production_date` date NOT NULL COMMENT '生产日期',
  `expiry_date` date DEFAULT NULL COMMENT '过期日期',
  `quantity` int NOT NULL COMMENT '生产数量',
  `trace_code` varchar(100) DEFAULT NULL COMMENT '溯源码',
  `status` varchar(20) DEFAULT 'CREATED' COMMENT '状态：CREATED/TESTED/SHIPPED/IN_TRANSIT/STORED/ON_SHELF/COMPLETED',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_no` (`batch_no`),
  UNIQUE KEY `uk_trace_code` (`trace_code`),
  KEY `idx_food_id` (`food_id`),
  KEY `idx_producer_id` (`producer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产批次表';

-- 质检记录表
CREATE TABLE IF NOT EXISTS `quality_test` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '检测ID',
  `batch_id` bigint NOT NULL COMMENT '批次ID',
  `test_date` date NOT NULL COMMENT '检测日期',
  `test_result` varchar(20) NOT NULL COMMENT '检测结果：QUALIFIED/UNQUALIFIED',
  `test_report` varchar(255) DEFAULT NULL COMMENT '检测报告',
  `tester` varchar(50) DEFAULT NULL COMMENT '检测人',
  `test_org` varchar(100) DEFAULT NULL COMMENT '检测机构',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_batch_id` (`batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质检记录表';

-- 物流运输表
CREATE TABLE IF NOT EXISTS `logistics_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '运输ID',
  `batch_id` bigint NOT NULL COMMENT '批次ID',
  `logistics_id` bigint NOT NULL COMMENT '物流企业ID',
  `sender_id` bigint NOT NULL COMMENT '发货方ID',
  `receiver_id` bigint DEFAULT NULL COMMENT '收货方ID',
  `sender_address` varchar(255) DEFAULT NULL COMMENT '发货地址',
  `receiver_address` varchar(255) DEFAULT NULL COMMENT '收货地址',
  `ship_time` datetime DEFAULT NULL COMMENT '发货时间',
  `arrive_time` datetime DEFAULT NULL COMMENT '到达时间',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/IN_TRANSIT/ARRIVED/SIGNED',
  `temperature` varchar(50) DEFAULT NULL COMMENT '运输温度',
  `transport_type` varchar(50) DEFAULT NULL COMMENT '运输方式',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_logistics_id` (`logistics_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流运输表';

-- 物流轨迹表
CREATE TABLE IF NOT EXISTS `logistics_track` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '轨迹ID',
  `logistics_record_id` bigint NOT NULL COMMENT '运输记录ID',
  `location` varchar(255) DEFAULT NULL COMMENT '位置',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `track_time` datetime DEFAULT NULL COMMENT '记录时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_logistics_record_id` (`logistics_record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流轨迹表';

-- 入库记录表
CREATE TABLE IF NOT EXISTS `storage_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '入库ID',
  `batch_id` bigint NOT NULL COMMENT '批次ID',
  `merchant_id` bigint NOT NULL COMMENT '商家ID',
  `storage_time` datetime NOT NULL COMMENT '入库时间',
  `quantity` int NOT NULL COMMENT '入库数量',
  `storage_location` varchar(255) DEFAULT NULL COMMENT '存储位置',
  `status` varchar(20) DEFAULT 'STORED' COMMENT '状态：STORED/ON_SHELF/SOLD_OUT',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库记录表';

-- 销售记录表
CREATE TABLE IF NOT EXISTS `sales_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '销售ID',
  `storage_id` bigint NOT NULL COMMENT '入库记录ID',
  `sales_time` datetime NOT NULL COMMENT '销售时间',
  `quantity` int NOT NULL COMMENT '销售数量',
  `buyer_info` varchar(255) DEFAULT NULL COMMENT '买家信息',
  `price` decimal(10,2) DEFAULT NULL COMMENT '销售单价',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_storage_id` (`storage_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售记录表';

-- 区块链上链记录表
CREATE TABLE IF NOT EXISTS `blockchain_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '上链记录ID',
  `business_type` varchar(50) NOT NULL COMMENT '业务类型：BATCH/TEST/LOGISTICS/STORAGE',
  `business_id` bigint NOT NULL COMMENT '业务ID',
  `batch_no` varchar(50) DEFAULT NULL COMMENT '批次号',
  `tx_hash` varchar(100) DEFAULT NULL COMMENT '交易哈希',
  `block_number` bigint DEFAULT NULL COMMENT '区块号',
  `data_hash` varchar(100) DEFAULT NULL COMMENT '数据摘要哈希',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/SUCCESS/FAILED',
  `chain_time` datetime DEFAULT NULL COMMENT '上链时间',
  `error_msg` varchar(255) DEFAULT NULL COMMENT '错误信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_business_type_id` (`business_type`, `business_id`),
  KEY `idx_batch_no` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区块链上链记录表';

-- 消费者查询记录表
CREATE TABLE IF NOT EXISTS `query_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '查询ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID（可匿名）',
  `trace_code` varchar(100) NOT NULL COMMENT '溯源码',
  `batch_no` varchar(50) DEFAULT NULL COMMENT '批次号',
  `query_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '查询时间',
  `query_ip` varchar(50) DEFAULT NULL COMMENT '查询IP',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_trace_code` (`trace_code`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费者查询记录表';

-- 公告表
CREATE TABLE IF NOT EXISTS `sys_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` text COMMENT '内容',
  `notice_type` varchar(20) DEFAULT 'SYSTEM' COMMENT '类型：SYSTEM/MAINTENANCE/BUSINESS',
  `target_role` varchar(100) DEFAULT 'ALL' COMMENT '目标角色',
  `status` tinyint DEFAULT 0 COMMENT '状态：0-未发布 1-已发布',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `publisher_id` bigint DEFAULT NULL COMMENT '发布人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- 个人信息变更申请表
CREATE TABLE IF NOT EXISTS `profile_change_request` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `user_id` bigint NOT NULL COMMENT '申请用户ID',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `old_real_name` varchar(50) DEFAULT NULL COMMENT '原姓名',
  `new_real_name` varchar(50) DEFAULT NULL COMMENT '新姓名',
  `old_phone` varchar(20) DEFAULT NULL COMMENT '原手机号',
  `new_phone` varchar(20) DEFAULT NULL COMMENT '新手机号',
  `old_email` varchar(100) DEFAULT NULL COMMENT '原邮箱',
  `new_email` varchar(100) DEFAULT NULL COMMENT '新邮箱',
  `reason` varchar(500) DEFAULT NULL COMMENT '变更原因',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/APPROVED/REJECTED',
  `audit_remark` varchar(255) DEFAULT NULL COMMENT '审核备注',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `auditor_id` bigint DEFAULT NULL COMMENT '审核人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人信息变更申请表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `sys_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint DEFAULT NULL COMMENT '操作用户ID',
  `username` varchar(50) DEFAULT NULL COMMENT '操作用户名',
  `operation` varchar(100) DEFAULT NULL COMMENT '操作内容',
  `method` varchar(255) DEFAULT NULL COMMENT '请求方法',
  `params` text COMMENT '请求参数',
  `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `result` tinyint DEFAULT 1 COMMENT '结果：0-失败 1-成功',
  `error_msg` text COMMENT '错误信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
