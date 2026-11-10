# 腊制品商铺系统

这是一个面向单商家的腊制品线上展示与经营管理系统。项目采用前后端分离架构，围绕商品浏览、购物车结算和订单流转实现完整业务流程，并提供商品、订单、用户、广告、通知、客服和经营数据管理功能。

## 技术栈

- 前端：Vue 3、Vite、Element Plus、Pinia、Vue Router、Axios
- 后端：Java 17、Spring Boot 3、Spring Security、Spring Data JPA、JWT
- 数据库：MySQL 8

## 主要功能

- 用户注册、登录、资料与收货地址管理
- 按部位、熏制工艺和口味筛选商品
- 商品搜索、详情展示、购物车与结算
- 订单创建、扫码支付确认、管理员支付审核、发货、收货与评价
- 在线客服、聊天记录、通知管理与已读状态
- 商品、订单、用户、广告和经营数据后台管理

## 项目结构

```text
shop/
├─ backend/      Spring Boot 后端
├─ frontend/     Vue 3 前端
└─ sql/          数据库初始化与升级脚本
```

## 本地运行

### 1. 初始化数据库

创建名为 `braised_pork_db` 的 MySQL 数据库，并依次执行 `sql` 目录中的初始化脚本。`init.sql` 内置的管理员账号仅用于本地演示，部署前请修改默认凭据。

### 2. 配置后端

后端通过环境变量读取数据库密码和 JWT 密钥。可参考根目录 `.env.example` 配置：

```text
DB_USERNAME=root
DB_PASSWORD=你的数据库密码
JWT_SECRET=一段足够长的随机字符串
```

进入后端目录启动服务：

```bash
cd backend
mvn spring-boot:run
```

默认后端地址为 `http://localhost:8099`。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认前端地址为 `http://localhost:5173`。

## 说明

- 仓库中的支付二维码为演示占位图，不包含个人收款信息。
- `backend/uploads`、构建产物和本地环境配置不会提交到仓库。
- 项目当前用于毕业设计与学习交流，请勿直接将演示账号和默认配置用于生产环境。
