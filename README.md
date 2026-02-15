# 疫苗预约系统

基于 Spring Cloud 微服务架构的高并发疫苗预约系统，支持多渠道支付、智能风控、库存管理等核心功能。

## 项目简介

本项目是一个完整的疫苗预约管理系统，采用微服务架构设计，支持高并发场景下的疫苗预约业务。系统包含用户管理、疫苗库存、预约订单、规则风控、支付通知、运营管理六大核心模块。

## 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                     前端 (Vue3 + NaiveUI)                    │
│                     http://localhost:3000                    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    网关服务 (端口: 9000)                      │
│                  路由、鉴权、限流、熔断                        │
└─────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│   用户服务     │   │   疫苗服务     │   │   预约服务     │
│    (8083)     │   │    (8085)     │   │    (8087)     │
└───────────────┘   └───────────────┘   └───────────────┘
        │                     │                     │
        └─────────────────────┼─────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│   支付服务     │   │   管理服务     │   │   基础设施     │
│    (8089)     │   │    (8081)     │   │ MySQL/Redis   │
└───────────────┘   └───────────────┘   │   RabbitMQ    │
                                        └───────────────┘
```

## 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.1.x | 核心框架 |
| Spring Cloud | 2022.x | 微服务框架 |
| MyBatis-Plus | 3.5.x | ORM框架 |
| Redis | 6.0+ | 分布式缓存 |
| RabbitMQ | 3.8+ | 消息队列 |
| Redisson | 3.24.x | 分布式锁 |
| Knife4j | 4.x | API文档 |
| Druid | 1.2.x | 数据库连接池 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.x | 前端框架 |
| Vite | 5.x | 构建工具 |
| NaiveUI | 2.x | UI组件库 |
| Pinia | 2.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |

## 项目结构

```
yimiao-manage/
├── yimiao-gateway/        # API网关服务
├── yimiao-user/           # 用户服务
├── yimiao-vaccine/        # 疫苗服务
├── yimiao-appointment/    # 预约服务
├── yimiao-payment/        # 支付服务
├── yimiao-admin/          # 管理服务
├── yimiao-common/         # 公共模块
├── yimiao-api/            # API接口定义
├── yimiao-frontend/       # 前端项目
├── sql/                   # 数据库脚本
├── docs/                  # 文档目录
├── logs/                  # 日志目录
├── pom.xml                # Maven父工程
├── start.sh               # 启动脚本
└── README.md              # 项目说明
```

## 核心功能

### 1. 用户管理模块
- 用户注册/登录
- JWT Token认证
- 实名认证
- 接种人管理

### 2. 疫苗库存模块
- 疫苗信息管理
- 多级缓存库存查询
- 原子化库存扣减 (Lua脚本)
- 号源管理

### 3. 预约订单模块
- 完整预约流程
- 风控资格校验
- 订单状态管理
- 订单核销

### 4. 规则风控模块
- 年龄校验
- 接种间隔校验
- 限流策略
- 黑名单管理

### 5. 支付通知模块
- 多渠道支付 (支付宝/微信/易支付)
- 支付回调处理
- 退款功能
- MQ异步通知

### 6. 运营管理模块
- 接种点管理
- 疫苗管理
- 号源排班
- 订单管理
- 数据统计

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.8+ (可选)
- Node.js 18+

### 1. 安装依赖

```bash
# 安装前端依赖
cd yimiao-frontend
npm install

# 编译后端项目
cd ..
mvn clean package -DskipTests
```

### 2. 启动基础服务

```bash
# 启动MySQL
brew services start mysql

# 启动Redis
brew services start redis

# 启动RabbitMQ (可选)
brew services start rabbitmq
```

### 3. 初始化数据库

```bash
mysql -u root -p < sql/init.sql
```

### 4. 启动后端服务

```bash
# 使用启动脚本
./start.sh

# 或手动启动
java -jar yimiao-gateway/target/yimiao-gateway-1.0.0.jar \
  --spring.profiles.active=standalone &

java -jar yimiao-user/target/yimiao-user-1.0.0.jar \
  --spring.cloud.nacos.discovery.enabled=false &

java -jar yimiao-vaccine/target/yimiao-vaccine-1.0.0.jar \
  --spring.cloud.nacos.discovery.enabled=false &

java -jar yimiao-appointment/target/yimiao-appointment-1.0.0.jar \
  --spring.cloud.nacos.discovery.enabled=false &

java -jar yimiao-payment/target/yimiao-payment-1.0.0.jar \
  --spring.cloud.nacos.discovery.enabled=false &

java -jar yimiao-admin/target/yimiao-admin-1.0.0.jar \
  --spring.cloud.nacos.discovery.enabled=false &
```

### 5. 启动前端

```bash
cd yimiao-frontend
npm run dev
```

### 6. 访问系统

- 前端地址: http://localhost:3000
- API文档: http://localhost:9000/doc.html
- 管理后台: http://localhost:3000/admin/dashboard

## 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| yimiao-gateway | 9000 | API网关 |
| yimiao-admin | 8081 | 运营管理服务 |
| yimiao-user | 8083 | 用户服务 |
| yimiao-vaccine | 8085 | 疫苗库存服务 |
| yimiao-appointment | 8087 | 预约订单服务 |
| yimiao-payment | 8089 | 支付通知服务 |

## 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 管理员 | newadmin | admin123 |

## 高并发设计

### 多级缓存

```
请求 → 本地缓存(Caffeine) → Redis缓存 → 数据库
```

- 本地缓存: 减少网络开销
- Redis缓存: 分布式共享
- 数据库: 最终数据源

### 分布式锁

使用 Redisson + Lua 脚本实现:
- 预约防重复提交
- 库存原子扣减
- 号源并发控制

### 库存扣减流程

```
1. Redis预扣减 (Lua脚本原子操作)
2. 创建预约订单
3. 异步同步到数据库
4. 失败自动回滚
```

## 文档

- [系统配置教程](docs/SYSTEM_CONFIG.md)
- [支付配置教程](docs/PAYMENT_CONFIG.md)
- [API文档](API_DOCUMENT.md)

## 开发指南

### 代码规范

- 遵循阿里巴巴Java开发规范
- 使用Lombok简化代码
- 统一异常处理
- 统一返回格式

### 分支管理

- main: 生产分支
- develop: 开发分支
- feature/*: 功能分支

## 常见问题

### 1. 端口被占用

```bash
# 查看端口占用
lsof -i :8083

# 杀死进程
kill -9 <PID>
```

### 2. 数据库连接失败

检查数据库配置是否正确，确保MySQL服务已启动。

### 3. Redis连接失败

```bash
# 检查Redis状态
redis-cli ping
```

### 4. RabbitMQ连接失败

RabbitMQ是可选的，如果不需要消息队列功能，可以跳过相关配置。

## License

MIT License

## 联系方式

如有问题，请提交 Issue 或 Pull Request。
