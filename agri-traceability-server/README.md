# 农产品合格证可信存证与追溯系统后端

## 环境要求

- JDK 17
- Maven 3.8+
- MySQL 8.0
- 可选：WeBASE-Front / FISCO BCOS，用于真实上链演示

## 数据库初始化

1. 创建并初始化表结构：执行 `src/main/resources/schema.sql`
2. 初始化角色、菜单、合约配置：执行 `src/main/resources/data.sql`
3. 不要在 `data.sql` 中插入用户。默认用户由 `DataInitializer` 首次启动时使用 BCrypt 加密后写入。

## 启动

```bash
mvn spring-boot:run
```

默认后端地址：`http://localhost:8080`

## 默认账号

- `admin / admin123`：系统管理员
- `regulator / admin123`：监管人员
- `producer / admin123`：农业经营主体

## 区块链说明

默认 `application-dev.yml` 中 `webase.mock-mode=true`，适合答辩演示，会生成模拟交易哈希和区块高度。
如需真实上链，请部署 `src/main/resources/contracts/AgriCertificateEvidence.sol`，并在合约配置中填写 `contractAddress` 和 `contractAbi`。

## UI 一致性自检清单

- 前端统一使用政务绿色主题变量，主色为 `#1F8A5B`
- 所有后台页面使用统一 Layout、统一卡片、统一表格样式
- 公众验真页面独立布局，不依赖后台菜单
