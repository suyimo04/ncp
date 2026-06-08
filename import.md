你是一个资深全栈工程师，精通 Java Spring Boot 生态、Vue3 前端工程化、FISCO BCOS 区块链应用开发以及 MySQL 数据库设计。你的任务是按照以下详细需求，直接生成一个完整的、可运行的本科毕业设计项目《基于 FISCO BCOS 的农产品承诺达标合格证可信存证与追溯系统》的全部前后端代码文件。禁止只提供思路、伪代码或部分片段，必须输出完整文件内容。

---

# 一、总体目标与项目定位

开发面向县域农产品质量安全监管场景的轻量化区块链可信追溯系统，完成“农产品批次 + 检测报告 + 合格证 + 区块链存证 + 公众验真”核心闭环。

**核心闭环流程**：经营主体备案 → 监管审核 → 创建批次 → 上传检测报告 → 生成文件 SHA-256 哈希 → 申请合格证 → 监管审核合格证 → 生成编号与二维码 → 批次/报告/合格证摘要上链 FISCO BCOS → 公众扫码/输入编号验真（重新计算哈希并与链上比对）。

**暂不实现**：生产过程记录、流通追溯、信用评分、抽检任务、复杂日志审计、真实国家平台接口、WeBASE 运维监控（仅保留交易查看与合约配置）。

---

# 二、技术栈与项目结构

## 2.1 技术栈

- **前端**：Vue 3.4 + Vite 5 + Element Plus 2.5 + Pinia 2 + Vue Router 4 + Axios + ECharts 5 + qrcode.vue
- **后端**：Spring Boot 3.2.5 + MyBatis Plus 3.5.5 + MySQL 8.0 + JWT (jjwt 0.11.5) + BCrypt + SHA-256
- **区块链**：FISCO BCOS（通过 WeBASE-Front HTTP 接口调用）+ Solidity 0.4.25/0.6.10（通用存证合约）

## 2.2 目录结构

后端 `agri-traceability-server/`：
```
pom.xml
src/main/java/com/agri/trace/
  ├── AgriTraceabilityApplication.java
  ├── config/          // MyBatisPlus、CORS、Web、Security、JWT
  ├── controller/      // 按模块划分
  ├── service/         // 接口
  ├── service/impl/    // 实现
  ├── mapper/          // DAO
  ├── entity/          // 实体（与表一一对应）
  ├── dto/             // 入参 DTO
  ├── vo/              // 出参 VO
  ├── common/
  │   ├── result/      // 统一返回 R<T>
  │   ├── exception/   // 全局异常、业务异常
  │   └── util/        // JWT、Hash、Date 工具
  ├── security/        // JWT 过滤器、用户详情服务
  └── blockchain/      // 合约配置、WeBASE 调用服务、存证服务
src/main/resources/
  ├── application.yml
  ├── application-dev.yml
  ├── mapper/xml/      // 自定义 XML
  ├── schema.sql       // 完整建表（含索引、外键）
  └── data.sql         // 初始菜单、角色、合约配置（禁止插入用户）
```

前端 `agri-traceability-web/`：
```
package.json
vite.config.js
index.html
src/
  ├── main.js
  ├── App.vue
  ├── api/             // 按模块封装的 Axios 接口
  ├── assets/          // 图片、字体
  ├── components/
  │   └── common/      // TagsView、Breadcrumb、AppCard、SvgIcon
  ├── layout/
  │   ├── index.vue    // 后台布局（Sidebar + Navbar + AppMain）
  │   └── components/  // Sidebar、Navbar、AppMain
  ├── router/
  │   └── index.js     // 路由表 + 路由守卫
  ├── store/           // Pinia：user、permission、app（sidebar/tagsview）
  ├── utils/
  │   ├── request.js   // Axios 拦截器
  │   ├── permission.js// 权限检查
  │   └── hash.js      // 前端辅助哈希（如需要）
  ├── views/
  │   ├── dashboard/   // 首页驾驶舱
  │   ├── producer/    // 经营主体
  │   ├── batch/       // 农产品批次
  │   ├── test-report/ // 检测报告
  │   ├── certificate/ // 合格证（列表/申请/审核/详情）
  │   ├── chain/       // 存证记录、链上核验、合约配置
  │   ├── system/      // 用户、角色、菜单
  │   └── public-verify/ // 公众验真（独立布局，移动端友好）
  └── styles/
      ├── variables.scss   // 政务绿主题变量
      ├── element-theme.scss // Element Plus 主题覆盖
      └── index.scss
```

## 2.3 README 要求

前后端均需提供 `README.md`，至少包含：
- 环境要求（JDK 17、Node 18+、MySQL 8.0）
- 数据库初始化：执行 `schema.sql` 再执行 `data.sql`
- 启动步骤（后端 `mvn spring-boot:run`，前端 `npm install && npm run dev`）
- **默认账号**（必须在 README 中写明）：
  - `admin / admin123`（系统管理员）
  - `regulator / admin123`（监管人员）
  - `producer / admin123`（农业经营主体）

---

# 三、后端详细要求

## 3.1 核心依赖（pom.xml）

必须提供可运行的 `pom.xml`，Spring Boot 3.2.5，JDK 17。关键依赖：
- `spring-boot-starter-web`
- `spring-boot-starter-validation`
- `spring-boot-starter-data-redis`（可选，用于JWT黑名单或缓存，如无则用本地内存）
- `mybatis-plus-boot-starter` 3.5.5
- `mysql-connector-j` 8.x
- `lombok`
- `jjwt-api`、`jjwt-impl`、`jjwt-jackson` 0.11.5
- `spring-boot-starter-test`

## 3.2 数据库设计（schema.sql）

**规则**：
- 所有表必须含 `create_time`、`update_time`、`deleted`（逻辑删除）。
- 使用 InnoDB & utf8mb4。
- `data.sql` **严禁插入 `sys_user`、`sys_user_role` 数据**；用户由 `DataInitializer` 在首次启动时 BCrypt 加密后写入。
- `data.sql` 可插入：角色、菜单、合约配置默认项。

以下是 **12 张核心表的完整 DDL**，必须在 `schema.sql` 中体现：

```sql
-- 1. 系统用户表
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

-- 2. 角色表
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

-- 3. 菜单表
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

-- 4. 用户角色关联
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

-- 5. 角色菜单关联
CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `menu_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联';

-- 6. 经营主体表
CREATE TABLE `biz_producer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
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
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='经营主体';

-- 7. 农产品批次表
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
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农产品批次';

-- 8. 检测报告表
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
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测报告';

-- 9. 合格证表
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
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合格证';

-- 10. 区块链存证记录表
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

-- 11. 合约配置表
CREATE TABLE `chain_contract_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(50) NOT NULL COMMENT '配置项',
  `config_value` varchar(500) NOT NULL COMMENT '配置值',
  `description` varchar(200) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合约配置';

-- 12. 公众查询记录表
CREATE TABLE `pub_query_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `certificate_code` varchar(50) NOT NULL,
  `query_ip` varchar(50) DEFAULT NULL,
  `query_result` tinyint(1) DEFAULT '1' COMMENT '1成功 0失败',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_cert_code` (`certificate_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公众查询记录';
```

## 3.3 权限与角色设计

- **角色**：`ADMIN`（系统管理员）、`REGULATOR`（监管人员）、`PRODUCER`（农业经营主体）。
- **权限模型**：简化 RBAC。用户-角色-菜单。
- **数据权限**：`PRODUCER` 只能查看和操作自己主体下的批次、报告、合格证；`REGULATOR` 和 `ADMIN` 查看全部。
- **接口鉴权**：Spring Security + JWT 过滤器。Token 格式 `Authorization: Bearer <token>`。
- **注册限制**：系统不提供公开注册，用户由 ADMIN 在后台创建；若开放注册接口，默认角色只能是 `PRODUCER`，禁止直接注册为 `ADMIN` 或 `REGULATOR`。

## 3.4 API 规范（统一返回）

统一返回结构：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

HTTP 状态码与业务 code：
- `200` 成功
- `400` 参数校验失败/业务校验失败
- `401` 未登录或 Token 过期/无效
- `403` 无权限访问
- `500` 系统内部异常

**必须实现的接口清单**：

| 模块 | 方法 | 路径 | 说明 | 主要角色 |
|---|---|---|---|---|
| 认证 | POST | /api/auth/login | 登录，返回 JWT | 全部 |
| 认证 | GET | /api/auth/info | 获取当前登录用户信息及角色菜单 | 全部 |
| 认证 | POST | /api/auth/logout | 退出（前端清除 Token） | 全部 |
| 经营主体 | GET | /api/producer/page | 分页查询 | ADMIN/REGULATOR/PRODUCER |
| 经营主体 | GET | /api/producer/{id} | 详情 | ADMIN/REGULATOR/PRODUCER |
| 经营主体 | POST | /api/producer | 新增 | ADMIN/PRODUCER |
| 经营主体 | PUT | /api/producer/{id} | 修改 | ADMIN/PRODUCER(自己的) |
| 经营主体 | DELETE | /api/producer/{id} | 删除（逻辑删） | ADMIN |
| 经营主体 | PUT | /api/producer/{id}/audit | 审核（传状态+意见） | ADMIN/REGULATOR |
| 农产品批次 | GET | /api/batch/page | 分页查询 | 全部登录 |
| 农产品批次 | GET | /api/batch/{id} | 详情 | 全部登录 |
| 农产品批次 | POST | /api/batch | 新增 | PRODUCER |
| 农产品批次 | PUT | /api/batch/{id} | 修改 | PRODUCER(未上链) |
| 农产品批次 | DELETE | /api/batch/{id} | 删除 | PRODUCER/ADMIN |
| 农产品批次 | POST | /api/batch/{id}/hash | 生成批次哈希 | PRODUCER |
| 农产品批次 | POST | /api/batch/{id}/chain | 批次上链 | PRODUCER |
| 检测报告 | GET | /api/test-report/page | 分页查询 | 全部登录 |
| 检测报告 | GET | /api/test-report/{id} | 详情 | 全部登录 |
| 检测报告 | POST | /api/test-report | 新增检测信息+上传文件 | PRODUCER |
| 检测报告 | POST | /api/test-report/upload | 仅上传文件，返回URL | PRODUCER |
| 检测报告 | POST | /api/test-report/{id}/hash | 生成报告文件哈希 | PRODUCER |
| 检测报告 | POST | /api/test-report/{id}/chain | 检测报告上链 | PRODUCER |
| 合格证 | GET | /api/certificate/page | 分页查询 | 全部登录 |
| 合格证 | GET | /api/certificate/{id} | 详情 | 全部登录 |
| 合格证 | POST | /api/certificate/apply | 申请合格证 | PRODUCER |
| 合格证 | PUT | /api/certificate/{id}/audit | 审核（通过/驳回） | ADMIN/REGULATOR |
| 合格证 | POST | /api/certificate/{id}/issue | 审核通过后签发（生成编号、二维码、有效期、哈希） | ADMIN/REGULATOR |
| 合格证 | POST | /api/certificate/{id}/chain | 合格证上链 | ADMIN/REGULATOR |
| 合格证 | PUT | /api/certificate/{id}/revoke | 作废合格证 | ADMIN/REGULATOR |
| 区块链存证 | GET | /api/chain/evidence/page | 存证记录分页 | ADMIN/REGULATOR |
| 区块链存证 | GET | /api/chain/evidence/{id} | 存证详情 | ADMIN/REGULATOR |
| 区块链存证 | POST | /api/chain/evidence/{id}/verify | 重新核验（本地哈希比对链上） | ADMIN/REGULATOR |
| 合约配置 | GET | /api/chain/contract/config | 查询合约配置 | ADMIN |
| 合约配置 | PUT | /api/chain/contract/config | 修改合约配置 | ADMIN |
| 公众验真 | GET | /api/public/verify/{certificateCode} | 公众验真（无需登录） | 公众 |

## 3.5 工程质量要求

1. **分层清晰**：Controller → Service → Mapper → Entity。入参使用 DTO（如 `ProducerAuditDTO`），出参使用 VO（如 `CertificateVO` 含关联信息）。
2. **MyBatis Plus 配置**：
   - 逻辑删除字段 `deleted`，未删除 0，已删除 1。
   - 自动填充 `create_time`、`update_time`；实现 `MetaObjectHandler`。
   - 实体类主键使用 `@TableId(type = IdType.AUTO)`。
3. **统一异常处理**：使用 `@RestControllerAdvice` + `@ExceptionHandler`，捕获 `BusinessException`（自定义业务异常，返回 400/403）、`MethodArgumentNotValidException`（返回 400）及全局 Exception（返回 500），均包装为 `R<T>`。
4. **JWT 与安全**：
   - JWT 含 `userId`、`username`、`roleCode`，有效期 24 小时。
   - 登录接口放行，其余需认证。
   - 配置 CORS，允许前端 `http://localhost:5173`。
5. **日志与配置**：使用 `application-dev.yml`，包含数据库连接、JWT Secret、文件上传路径、WeBASE-Front 服务地址。
6. **文件上传**：检测报告 PDF/图片上传到 `uploads/` 目录（或项目 `static/uploads`），通过 `ResourceHandler` 映射 `/uploads/**` 提供访问。
7. **哈希工具类**：提供 `HashUtil.sha256(String content)` 与 `HashUtil.sha256File(MultipartFile file)`，严格按以下规则拼接字符串后哈希：
   - **批次哈希**：`batchCode|producerCode|productName|productCategory|originAddress|harvestTime|batchWeight`（日期格式 `yyyy-MM-dd`，重量取原始值字符串）
   - **合格证哈希**：`certificateCode|batchCode|reportCode|producerCode|issueTime|expireTime|auditUserId`（日期时间格式 `yyyy-MM-dd HH:mm:ss`，日期格式 `yyyy-MM-dd`）
   - **作废哈希**：`certificateCode|revokeReason|revokeTime|operatorId`（时间格式 `yyyy-MM-dd HH:mm:ss`）
   - **报告哈希**：直接对文件字节数组做 SHA-256。

## 3.6 默认账号初始化（极其重要）

创建 `DataInitializer.java`，实现 `CommandLineRunner`，**严禁在 data.sql 中插入用户**：

```java
@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired private UserMapper userMapper;
    @Autowired private RoleMapper roleMapper;
    @Autowired private UserRoleMapper userRoleMapper;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 1. 初始化角色（若为空）
        if (roleMapper.selectCount(null) == 0) {
            roleMapper.insert(new Role("ADMIN", "系统管理员"));
            roleMapper.insert(new Role("REGULATOR", "监管人员"));
            roleMapper.insert(new Role("PRODUCER", "农业经营主体"));
        }
        // 2. 初始化用户（若为空）
        if (userMapper.selectCount(null) == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRealName("系统管理员");
            admin.setStatus(1);
            userMapper.insert(admin);
            userRoleMapper.insert(new UserRole(admin.getId(), getRoleId("ADMIN")));

            User regulator = new User();
            regulator.setUsername("regulator");
            regulator.setPassword(passwordEncoder.encode("admin123"));
            regulator.setRealName("监管人员");
            regulator.setStatus(1);
            userMapper.insert(regulator);
            userRoleMapper.insert(new UserRole(regulator.getId(), getRoleId("REGULATOR")));

            User producer = new User();
            producer.setUsername("producer");
            producer.setPassword(passwordEncoder.encode("admin123"));
            producer.setRealName("示范农户");
            producer.setStatus(1);
            userMapper.insert(producer);
            userRoleMapper.insert(new UserRole(producer.getId(), getRoleId("PRODUCER")));
        }
        // 3. 初始化合约配置默认值
        // ...
    }
}
```

## 3.7 区块链服务层（WeBASE 调用）

封装 `BlockchainService`：
- 通过 `RestTemplate` 调用本地或远程 WeBASE-Front 的 `POST /WeBASE-Front/trans/handle`。
- 调用参数包括：`groupId`、`contractName`、`contractAddress`、`contractAbi`、`funcName`、`funcParam`、`user`。
- 上链前，先从数据库取最新 `contractAddress` 与 ABI 配置。
- **上链内容仅包含摘要**：业务编号、存证类型、哈希值、关联编号、操作人 ID、时间戳。
- 调用成功返回后，解析 `transactionHash`、`blockNumber` 存入 `chain_evidence_record`。
- **调用失败必须捕获异常**，记录 `error_message`，`chain_status` 置为 3（失败），禁止吞掉异常。
- 若 WeBASE 未配置或调用超时，应有降级：记录错误，不阻断业务流程提示，但毕业设计演示时应可配置为模拟模式或直接报错。

## 3.8 核心校验规则（后端必须实现）

1. `biz_producer.credit_code` 全局唯一。
2. 经营主体 `audit_status != 1`（未通过）时，禁止创建批次或申请合格证。
3. `batch_weight` 必须 > 0。
4. 已上链（`chain_status = 2`）的批次、报告、合格证，禁止修改核心字段（编码、哈希等），只能修改备注类字段。
5. 检测结论为“不合格”的批次，禁止申请合格证。
6. 一个 `batch_id` 只能存在一个有效合格证：申请时检查该批次下是否存在 `issue_status IN (2,4)` 且 `revoke_status = 0` 且 `deleted = 0` 的记录。
7. 合格证审核通过后才生成 `certificate_code`、二维码 URL、有效期（建议自签发日起 30 天），并计算 `certificate_hash`。
8. 作废时必须填写 `revoke_reason`，调用合约 `revokeCertificate` 写入链上作废存证，更新 `revoke_status = 1`。
9. 公众验真接口 `/api/public/verify/{certificateCode}` 内部逻辑：
   - 查询合格证及关联批次、报告、主体。
   - 重新计算本地批次哈希、报告文件哈希、合格证哈希。
   - 调用链上 `getEvidence` 比对哈希是否一致，返回核验结果。
   - 记录 `pub_query_record`。

---

# 四、前端详细要求

## 4.1 技术与依赖（package.json）

- `vue`: ^3.4.21
- `vite`: ^5.0.0
- `element-plus`: ^2.5.0
- `vue-router`: ^4.2.0
- `pinia`: ^2.1.0
- `axios`: ^1.6.0
- `echarts`: ^5.4.0
- `vue-echarts`: ^6.6.0
- `qrcode.vue`: ^3.4.0
- `@element-plus/icons-vue`: ^2.3.0
- `js-cookie`: ^3.0.5
- `nprogress`: ^0.2.0
- `sass`: ^1.70.0

## 4.2 页面、路由与功能

**后台布局路由**（使用统一 Layout，含 Sidebar + Navbar + TagsView + Breadcrumb）：

| 路径 | 组件 | 说明 |
|---|---|---|
| /login | Login/index.vue | 登录页 |
| / | Layout → Dashboard | 首页驾驶舱（ECharts 统计卡片 + 趋势图） |
| /producer | Layout → Producer/index | 经营主体管理（筛选、表格、审核按钮） |
| /batch | Layout → Batch/index | 农产品批次（表格、生成哈希、上链按钮） |
| /test-report | Layout → TestReport/index | 检测报告（上传文件、生成哈希） |
| /certificate | Layout → Certificate/index | 合格证列表（申请、查看） |
| /certificate/audit | Layout → Certificate/audit | 合格证审核（监管角色可见） |
| /certificate/detail/:id | Layout → Certificate/detail | 合格证详情（含二维码展示） |
| /chain/evidence | Layout → Chain/Evidence | 存证记录 |
| /chain/verify | Layout → Chain/Verify | 链上核验 |
| /chain/contract | Layout → Chain/Contract | 合约配置（ADMIN） |
| /system/user | Layout → System/User | 用户管理 |
| /system/role | Layout → System/Role | 角色管理 |
| /system/menu | Layout → System/Menu | 菜单管理 |

**独立页面**：
| 路径 | 组件 | 说明 |
|---|---|---|
| /public/verify | PublicVerify/index.vue | 公众验真（无 Layout，移动端适配，输入框查询，展示产品/主体/检测/链上结果） |
| /public/verify/:code | PublicVerify/index.vue | 扫码直接进入 |

## 4.3 权限控制

- **路由守卫**：`router.beforeEach`，检查 `Pinia` 中 `token` 是否存在，无则跳转 `/login`；已登录但路由不存在则跳 404。
- **菜单过滤**：登录后获取 `auth/info`，返回当前用户的 `roles` 和 `menus`，前端根据 `menu_type` 动态生成侧边栏；无权限菜单不渲染。
- **按钮权限**：封装 `v-permission` 指令，根据 `perms` 判断；或统一函数 `hasPerm('certificate:audit')` 控制按钮显示。
- **角色数据隔离**：`PRODUCER` 登录后，经营主体列表、批次列表等自动带 `producerId` 参数（后端控制，前端正常传当前用户关联主体 ID 即可）。

## 4.4 工程要求

1. **Axios 封装** (`utils/request.js`)：
   - baseURL: `/api`
   - 请求拦截器：自动在 Header 添加 `Authorization: Bearer ${token}`
   - 响应拦截器：
     - `code === 401`：清除 Token，跳转 `/login`
     - `code === 403`：`ElMessage.error('无权限访问')`
     - `code === 500`：`ElMessage.error('系统异常')`
     - 其他错误：`ElMessage.error(message || '请求失败')`
2. **Pinia Store**：
   - `userStore`：token、userInfo、roles
   - `permissionStore`：routes、menus、addRoutes
   - `appStore`：sidebar opened、tagsViewList、cachedViews
3. **TagsView 实现**：
   - 监听 `$route`，自动将路由加入 `tagsViewList`，首页 `/` 固定不可关闭。
   - 右键菜单：刷新当前、关闭当前、关闭其他、关闭全部。
   - 刷新当前：使用 `router.replace` 结合 `nextTick` 实现。
   - 关闭标签后自动激活最近一个标签。
   - 页面刷新后尽量保留（`sessionStorage` 缓存 tags 列表）。
4. **面包屑**：基于当前路由 `matched` 自动生成。

## 4.5 UI/UX 设计规范（政务绿色监管风，强制统一）

本项目 UI 必须严格遵循“政务绿色监管风”，**禁止**使用 Element Plus 默认蓝色主题。

**全局主题变量** (`styles/variables.scss`)：

```scss
:root {
  --agri-primary: #1F8A5B;      // 主色
  --agri-secondary: #2F9E7E;    // 辅色
  --agri-accent: #F2B84B;       // 强调色
  --agri-success: #2EAD67;
  --agri-warning: #E6A23C;
  --agri-danger: #D94841;
  --agri-info: #3A7CA5;
  --agri-bg: #F5F7F6;           // 页面背景
  --agri-sidebar-bg: #173B2F;   // 侧边栏背景
  --agri-header-bg: #FFFFFF;    // 顶部栏背景
  --agri-text: #1F2D2A;         // 正文
  --agri-text-secondary: #6B7C76;
  --agri-border: #DDE5E1;
  --agri-table-header: #EEF6F2; // 表头背景
  --agri-card-bg: #FFFFFF;
  --agri-radius: 8px;           // 统一圆角
  --agri-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}
```

**覆盖规则**：
- Element Plus 主题色：通过 CSS 变量覆盖 `--el-color-primary: #1F8A5B; --el-color-success: #2EAD67; --el-color-warning: #E6A23C; --el-color-danger: #D94841;`。
- 侧边栏：`background: #173B2F;`，菜单文字 `#bfcbd9`，激活/悬停 `#1F8A5B` 背景 + `#FFFFFF` 文字。
- 顶部栏：`background: #FFFFFF; border-bottom: 1px solid #DDE5E1;`。
- 内容区：`background: #F5F7F6; padding: 16px;`。
- 卡片：白色背景、圆角 8px、阴影 `0 2px 12px 0 rgba(0,0,0,0.05)`、边框 `1px solid #DDE5E1`。
- 表格：表头背景 `#EEF6F2`，表头文字 `#1F2D2A`，行悬停 `#F5F7F6`。
- 按钮：主按钮使用 `#1F8A5B`，危险按钮使用 `#D94841`。
- 公众验真页：独立页面，背景渐变或浅色 `#F5F7F6`，内容卡片居中，最大宽度 480px，适配手机。

**一致性约束**：
- 必须定义并复用上述 SCSS 变量，全站统一引用。
- 所有后台页面必须使用统一 Layout，导航结构、容器边距、卡片样式保持一致。
- 至少封装 `AppButton`、`AppCard`、`AppTable` 三类基础风格组件（或全局覆盖 Element Plus 样式），禁止页面内重复写相同样式。
- 最终交付时，在 README 中提供“UI 一致性自检清单”，确认每个页面符合政务绿色规范。

---

# 五、智能合约要求（Solidity）

合约文件：`AgriCertificateEvidence.sol`

```solidity
pragma solidity ^0.6.10;

contract AgriCertificateEvidence {
    struct Evidence {
        string businessCode;
        string evidenceType;   // BATCH / REPORT / CERT / REVOKE
        string evidenceHash;
        string relatedCode;
        string operatorId;
        uint256 timestamp;
        bool exists;
    }

    mapping(string => Evidence) private evidences;
    mapping(string => bool) private revokedCertificates;

    event EvidenceSaved(string businessCode, string evidenceType, uint256 timestamp);
    event CertificateRevoked(string certificateCode, uint256 timestamp);

    function saveEvidence(
        string memory businessCode,
        string memory evidenceType,
        string memory evidenceHash,
        string memory relatedCode,
        string memory operatorId
    ) public {
        require(!evidences[businessCode].exists, "Evidence already exists");
        evidences[businessCode] = Evidence(
            businessCode,
            evidenceType,
            evidenceHash,
            relatedCode,
            operatorId,
            block.timestamp,
            true
        );
        emit EvidenceSaved(businessCode, evidenceType, block.timestamp);
    }

    function getEvidence(string memory businessCode) public view returns (
        string memory, string memory, string memory, string memory, string memory, uint256, bool
    ) {
        Evidence memory e = evidences[businessCode];
        require(e.exists, "Evidence not found");
        return (e.businessCode, e.evidenceType, e.evidenceHash, e.relatedCode, e.operatorId, e.timestamp, e.exists);
    }

    function revokeCertificate(
        string memory certificateCode,
        string memory revokeHash,
        string memory operatorId
    ) public {
        require(!revokedCertificates[certificateCode], "Already revoked");
        revokedCertificates[certificateCode] = true;
        // 同时写入一条 REVOKE 类型的存证，businessCode 使用 certificateCode + "_REVOKE"
        string memory revokeCode = string(abi.encodePacked(certificateCode, "_REVOKE"));
        if (!evidences[revokeCode].exists) {
            evidences[revokeCode] = Evidence(
                revokeCode,
                "REVOKE",
                revokeHash,
                certificateCode,
                operatorId,
                block.timestamp,
                true
            );
        }
        emit CertificateRevoked(certificateCode, block.timestamp);
    }

    function isCertificateRevoked(string memory certificateCode) public view returns (bool) {
        return revokedCertificates[certificateCode];
    }
}
```

**要求**：
- 合约需通过 WeBASE 部署，系统后台保存 `contractAddress` 与 ABI。
- 上链时 `businessCode` 规则：
  - 批次：`BATCH_{batchCode}`
  - 报告：`REPORT_{reportCode}`
  - 合格证：`CERT_{certificateCode}`
  - 作废：`REVOKE_{certificateCode}_REVOKE`（或合约内部拼接）

---

# 六、工程护栏（强制遵守，不可偏离）

1. **默认账号初始化**：必须由后端 `CommandLineRunner` 在首次启动时初始化 `admin / regulator / producer`，密码使用 `BCryptPasswordEncoder` 加密。**严禁在 `data.sql` 中插入用户数据或密码哈希**。
2. **统一返回结构**：全后端统一使用 `code`、`message`、`data`，禁止混用 `msg`。
3. **Token 传递**：必须使用 `Authorization: Bearer <token>`，前端拦截器自动携带，统一处理 401/403。
4. **角色提升限制**：禁止前端传参直接注册为 ADMIN；只有 ADMIN 能分配角色。
5. **MySQL 护栏**：
   - `schema.sql` 必须含完整字段、类型、索引、外键（如 `biz_product_batch.producer_id` 关联 `biz_producer.id`）。
   - `data.sql` 只插入菜单、角色、配置，**用户表留空**。
   - 唯一性约束：`credit_code`、`batch_code`、`report_code`、`certificate_code`、`username`、`role_code`、`config_key` 必须唯一。
6. **Spring Boot 护栏**：
   - 提供完整 `pom.xml` 与 `application-dev.yml`（含数据库、JWT、上传路径、WeBASE 地址）。
   - 必须使用 `@RestControllerAdvice` 全局异常处理并返回统一结构。
   - `DataInitializer` 必须实现并生效。
7. **前端工程护栏**：
   - 提供完整 `package.json` 与 `vite.config.js`（含代理 `/api` 到 `http://localhost:8080`）。
   - 统一 `request.js` 封装与错误处理。
   - 路由守卫 + Pinia 存储登录态。
   - 公众验真页 `/public/verify` 不使用后台 Layout。
8. **UI 一致性护栏**：
   - Element Plus 必须主题定制为政务绿色（`#1F8A5B` 等），禁止出现默认蓝与政务绿混用。
   - 必须定义全局 SCSS 变量并全站复用。
   - 所有后台页面统一 Layout、统一边距、统一卡片样式。

---

# 七、联调约定

- **后端地址**：`http://localhost:8080`
- **前端地址**：`http://localhost:5173`
- **API 前缀**：`/api`
- **跨域**：后端 CORS 允许 `http://localhost:5173`，前端 Vite 代理 `/api` → `http://localhost:8080`
- **Token**：`Authorization: Bearer <jwt>`
- **静态资源**：上传文件通过 `/uploads/{filename}` 访问
- **默认账号**：
  - admin / admin123
  - regulator / admin123
  - producer / admin123

---

# 八、输出与交付方式

你必须输出**完整的、可直接运行的全部前后端文件**，不要省略任何文件。每个文件都要给出**完整内容**。

包括但不限于：
- 后端：所有 `pom.xml`、`application.yml`、`schema.sql`、`data.sql`、所有 Java 文件（Config、Controller、Service、Impl、Mapper、XML、Entity、DTO、VO、Common、Security、Blockchain、DataInitializer）。
- 前端：所有 `package.json`、`vite.config.js`、`index.html`、所有 Vue 组件（页面 + Layout + Common）、所有 JS/TS 文件（router、store、api、utils、styles）。

**要求**：
1. 先输出完整目录树。
2. 然后逐个文件输出完整代码内容。
3. 代码注释使用简体中文，重点解释业务意图、状态流转、哈希生成、上链逻辑、核验逻辑；禁止给简单 getter/setter 写无意义注释。
4. 不要只给思路，直接给可用的文件内容。
5. 帮我完成整个项目的全部前后端文件。

开始生成代码。不要只给思路，直接给可用的文件内容。帮我完成整个项目的全部前后端文件。