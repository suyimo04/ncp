# 基于 FISCO BCOS 的农产品承诺达标合格证可信存证与追溯系统

这是一个面向县域农产品质量安全监管场景的轻量级可信追溯系统。系统围绕“经营主体备案、农产品批次建档、检测报告上传、承诺达标合格证签发、区块链存证、公众扫码验真”形成完整业务闭环，适合作为本科毕业设计、课程设计或区块链应用开发示例项目。

项目采用前后端分离架构：

- 前端：Vue 3、Vite、Element Plus、Pinia、Vue Router、Axios、ECharts
- 后端：Spring Boot 3、MyBatis Plus、MySQL、JWT、BCrypt、SHA-256
- 区块链：FISCO BCOS，预留 WeBASE-Front HTTP 调用方式，默认支持模拟上链演示

## 项目功能

### 经营主体管理

- 经营主体备案
- 主体信息维护
- 监管人员审核
- 主体审核状态控制

### 农产品批次管理

- 创建农产品批次
- 维护产品名称、类别、产地、采收时间、批次重量等信息
- 生成批次 SHA-256 哈希
- 提交批次摘要上链

### 检测报告管理

- 新增检测报告信息
- 上传 PDF 或图片检测报告
- 对报告文件计算 SHA-256 哈希
- 提交检测报告摘要上链

### 合格证管理

- 基于合格批次和检测报告申请合格证
- 监管人员审核合格证
- 签发合格证编号
- 生成公众验真二维码链接
- 生成合格证哈希
- 提交合格证摘要上链
- 支持合格证作废

### 区块链存证管理

- 查看批次、报告、合格证、作废记录的存证信息
- 查看交易哈希、区块高度、上链状态
- 重新核验本地哈希与链上存证摘要
- 后台维护合约地址、ABI、群组 ID 等配置

### 公众验真

- 无需登录
- 支持输入合格证编号查询
- 展示产品、主体、检测报告、签发时间、有效期等信息
- 展示链上交易哈希和区块高度
- 比对本地重新计算的哈希与存证记录，判断数据是否可信

## 目录结构

```text
ncp/
├── agri-traceability-server/     # Spring Boot 后端
│   ├── src/main/java/com/agri/trace/
│   │   ├── blockchain/           # 区块链调用与配置
│   │   ├── common/               # 统一返回、异常、工具类
│   │   ├── config/               # 安全、MyBatis Plus、Web 配置
│   │   ├── controller/           # REST 接口
│   │   ├── dto/                  # 入参对象
│   │   ├── entity/               # 数据库实体
│   │   ├── mapper/               # MyBatis Plus Mapper
│   │   ├── security/             # JWT 认证过滤器
│   │   ├── service/              # 业务接口
│   │   └── vo/                   # 出参对象
│   └── src/main/resources/
│       ├── contracts/            # Solidity 合约
│       ├── application.yml
│       ├── application-dev.yml
│       ├── schema.sql
│       └── data.sql
├── agri-traceability-web/        # Vue 3 前端
│   ├── src/api/                  # Axios 接口封装
│   ├── src/components/           # 通用组件
│   ├── src/layout/               # 后台统一布局
│   ├── src/router/               # 路由与守卫
│   ├── src/store/                # Pinia 状态管理
│   ├── src/styles/               # 政务绿色主题样式
│   ├── src/utils/                # 请求、权限、哈希工具
│   └── src/views/                # 页面模块
└── README.md
```

## 环境要求

- JDK 17
- Maven 3.8+
- Node.js 18+
- MySQL 8.0
- 可选：FISCO BCOS / WeBASE-Front

## 数据库初始化

先创建数据库和表结构：

```sql
source agri-traceability-server/src/main/resources/schema.sql;
```

再初始化角色、菜单和合约配置：

```sql
source agri-traceability-server/src/main/resources/data.sql;
```

说明：

- `data.sql` 不插入用户数据
- 默认用户由后端 `DataInitializer` 首次启动时自动创建
- 密码使用 BCrypt 加密保存

## 后端启动

进入后端目录：

```bash
cd agri-traceability-server
```

根据本机 MySQL 修改：

```text
src/main/resources/application-dev.yml
```

主要配置项：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/agri_traceability
    username: root
    password: root
```

启动后端：

```bash
mvn spring-boot:run
```

默认后端地址：

```text
http://localhost:8080
```

## 前端启动

进入前端目录：

```bash
cd agri-traceability-web
```

安装依赖：

```bash
npm install
```

启动开发服务器：

```bash
npm run dev
```

默认前端地址：

```text
http://localhost:5173
```

前端已配置代理：

```text
/api -> http://localhost:8080
```

## 默认账号

| 账号 | 密码 | 角色 |
| --- | --- | --- |
| admin | admin123 | 系统管理员 |
| regulator | admin123 | 监管人员 |
| producer | admin123 | 农业经营主体 |

## 区块链说明

项目默认使用模拟上链模式，适合本地开发和答辩演示：

```yaml
webase:
  mock-mode: true
```

模拟模式会生成交易哈希和区块高度，不依赖真实 FISCO BCOS 节点。

如需接入真实链，需要：

1. 部署 `agri-traceability-server/src/main/resources/contracts/AgriCertificateEvidence.sol`
2. 在后台合约配置中填写合约地址和 ABI
3. 修改 `application-dev.yml` 中的 WeBASE-Front 地址
4. 将 `webase.mock-mode` 改为 `false`

## 智能合约

合约文件：

```text
agri-traceability-server/src/main/resources/contracts/AgriCertificateEvidence.sol
```

主要能力：

- 保存业务存证摘要
- 查询链上存证
- 标记合格证作废
- 查询合格证作废状态

## 开发约定

- 后端统一返回结构：`code`、`message`、`data`
- 前端请求统一通过 `src/utils/request.js`
- Token 使用 `Authorization: Bearer <token>`
- 批次、检测报告、合格证均使用 SHA-256 生成摘要
- 经营主体未审核通过时，不允许创建批次或申请合格证
- 检测结论为“不合格”时，不允许申请合格证
- 已上链记录只允许修改备注类信息

## 构建检查

后端打包：

```bash
cd agri-traceability-server
mvn -DskipTests package
```

前端构建：

```bash
cd agri-traceability-web
npm run build
```

## 许可证

本项目可作为学习、课程设计和毕业设计参考使用。正式生产环境使用前，请补充更完整的权限审计、日志审计、文件安全校验和真实区块链节点运维配置。
