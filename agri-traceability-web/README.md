# 农产品合格证可信存证与追溯系统前端

## 环境要求

- Node 18+
- 后端服务：`http://localhost:8080`

## 启动

```bash
npm install
npm run dev
```

默认访问地址：`http://localhost:5173`

## 默认账号

- `admin / admin123`：系统管理员
- `regulator / admin123`：监管人员
- `producer / admin123`：农业经营主体

## 主要页面

- 后台首页：`/dashboard`
- 公众验真：`/public/verify`
- 合格证详情：`/certificate/detail/:id`

## UI 一致性自检清单

- 全站 SCSS 变量统一定义在 `src/styles/variables.scss`
- Element Plus 主题色已覆盖为政务绿色，主色 `#1F8A5B`
- 后台页面统一使用 `Layout + AppCard + AppTable`
- 公众验真页为独立布局，移动端宽度下可正常展示
