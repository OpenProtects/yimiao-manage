# 疫苗预约系统 - 完整配置教程

## 目录

1. [环境要求](#一环境要求)
2. [数据库配置](#二数据库配置)
3. [Redis配置](#三redis配置)
4. [RabbitMQ配置](四rabbitmq配置)
5. [微服务配置](#五微服务配置)
6. [前端配置](#六前端配置)
7. [启动顺序](#七启动顺序)
8. [功能测试](#八功能测试)
9. [常见问题](#九常见问题)
10. [生产环境部署](#十生产环境部署)

---

## 一、环境要求

### 1.1 必需软件

| 软件 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 17+ | Java运行环境 |
| MySQL | 8.0+ | 主数据库 |
| Redis | 6.0+ | 缓存和分布式锁 |
| RabbitMQ | 3.8+ | 消息队列（可选） |
| Node.js | 18+ | 前端运行环境 |
| Maven | 3.8+ | Java构建工具 |

### 1.2 开发工具推荐

- IntelliJ IDEA (后端开发)
- VS Code (前端开发)
- Navicat/DBeaver (数据库管理)
- RedisInsight (Redis管理)
- Postman (API测试)

### 1.3 环境检查

```bash
# 检查Java版本
java -version

# 检查Maven版本
mvn -version

# 检查Node.js版本
node -version

# 检查npm版本
npm -version
```

---

## 二、数据库配置

### 2.1 安装MySQL

**macOS:**
```bash
brew install mysql
brew services start mysql
```

**Ubuntu:**
```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
```

**Windows:**
下载MySQL安装包，按向导安装即可。

### 2.2 创建数据库和用户

```sql
-- 创建数据库
CREATE DATABASE test_ym_1 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户
CREATE USER 'test'@'%' IDENTIFIED BY '123456';

-- 授权
GRANT ALL PRIVILEGES ON test_ym_1.* TO 'test'@'%';
FLUSH PRIVILEGES;
```

### 2.3 执行初始化脚本

```bash
# 方式1: 使用MySQL客户端
mysql -u test -p test_ym_1 < sql/init.sql

# 方式2: 在Navicat中执行
# 打开 sql/init.sql 文件，选择数据库后执行
```

### 2.4 数据库连接配置

每个微服务的 `application.yaml` 中配置:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/test_ym_1?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: test
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
```

### 2.5 数据库表结构

系统包含以下核心表:

| 表名 | 说明 |
|------|------|
| ym_user | 用户表 |
| ym_admin | 管理员表 |
| ym_vaccinee | 接种人表 |
| ym_real_name_cert | 实名认证表 |
| ym_vaccine | 疫苗表 |
| ym_vaccine_stock | 疫苗库存表 |
| ym_site | 接种点表 |
| ym_slot | 号源表 |
| ym_appointment | 预约订单表 |
| ym_payment_channel | 支付渠道表 |
| ym_payment_record | 支付记录表 |
| ym_refund_record | 退款记录表 |
| ym_blacklist | 黑名单表 |

---

## 三、Redis配置

### 3.1 安装Redis

**macOS:**
```bash
brew install redis
brew services start redis
```

**Ubuntu:**
```bash
sudo apt update
sudo apt install redis-server
sudo systemctl start redis
```

**Windows:**
下载Redis Windows版本，解压后运行 `redis-server.exe`

### 3.2 Redis配置

编辑 `redis.conf`:

```conf
# 绑定地址
bind 127.0.0.1

# 端口
port 6379

# 密码 (生产环境必须设置)
# requirepass your_redis_password

# 最大内存
maxmemory 256mb

# 内存策略
maxmemory-policy allkeys-lru
```

### 3.3 应用配置

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
```

### 3.4 验证Redis

```bash
redis-cli ping
# 输出: PONG
```

---

## 四、RabbitMQ配置

### 4.1 安装RabbitMQ

**macOS:**
```bash
brew install rabbitmq
brew services start rabbitmq
```

**Ubuntu:**
```bash
sudo apt install rabbitmq-server
sudo systemctl start rabbitmq-server
```

**Docker:**
```bash
docker run -d --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:management
```

### 4.2 RabbitMQ管理界面

- 地址: http://localhost:15672
- 默认账号: guest
- 默认密码: guest

### 4.3 创建队列和交换机

系统启动时会自动创建，也可以手动创建:

```bash
# 启用管理插件
rabbitmq-plugins enable rabbitmq_management

# 创建用户
rabbitmqctl add_user yimiao yimiao123456
rabbitmqctl set_user_tags yimiao administrator
rabbitmqctl set_permissions -p / yimiao ".*" ".*" ".*"
```

### 4.4 应用配置

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
```

**注意**: RabbitMQ是可选的，如果不需要消息队列功能，可以不启动RabbitMQ，部分功能会降级运行。

---

## 五、微服务配置

### 5.1 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| yimiao-gateway | 9000 | API网关 |
| yimiao-user | 8083 | 用户服务 |
| yimiao-vaccine | 8085 | 疫苗服务 |
| yimiao-appointment | 8087 | 预约服务 |
| yimiao-payment | 8089 | 支付服务 |
| yimiao-admin | 8081 | 管理服务 |

### 5.2 通用配置

每个服务的 `application.yaml`:

```yaml
server:
  port: 808x  # 根据服务不同

spring:
  application:
    name: yimiao-xxx  # 服务名称
  
  # 数据源配置
  datasource:
    url: jdbc:mysql://localhost:3306/test_ym_1
    username: test
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
    type: com.alibaba.druid.pool.DruidDataSource
  
  # Redis配置
  redis:
    host: localhost
    port: 6379
  
  # RabbitMQ配置 (可选)
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

# MyBatis-Plus配置
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

# 日志配置
logging:
  level:
    com.yimiao: debug
```

### 5.3 网关配置

`yimiao-gateway/application-standalone.yaml`:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: http://localhost:8083
          predicates:
            - Path=/api/user/**,/api/cert/**,/api/vaccinee/**,/api/sms/**
          filters:
            - StripPrefix=1
        
        - id: vaccine-service
          uri: http://localhost:8085
          predicates:
            - Path=/api/vaccine/**,/api/site/**,/api/slot/**,/api/stock/**
          filters:
            - StripPrefix=1
        
        - id: appointment-service
          uri: http://localhost:8087
          predicates:
            - Path=/api/appointment/**,/api/blacklist/**
          filters:
            - StripPrefix=1
        
        - id: payment-service
          uri: http://localhost:8089
          predicates:
            - Path=/api/payment/**,/api/refund/**,/api/notification/**
          filters:
            - StripPrefix=1
        
        - id: admin-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/admin/**,/api/statistics/**
          filters:
            - StripPrefix=1
```

### 5.4 关闭Nacos (本地开发)

```yaml
spring:
  cloud:
    nacos:
      discovery:
        enabled: false
      config:
        enabled: false
```

启动参数:
```bash
java -jar xxx.jar --spring.cloud.nacos.discovery.enabled=false --spring.cloud.nacos.config.enabled=false
```

---

## 六、前端配置

### 6.1 安装依赖

```bash
cd yimiao-frontend
npm install
# 或
cnpm install
# 或
pnpm install
```

### 6.2 环境配置

创建 `.env.development`:

```env
VITE_API_BASE_URL=http://localhost:9000/api
VITE_APP_TITLE=疫苗预约系统
```

创建 `.env.production`:

```env
VITE_API_BASE_URL=https://your-domain.com/api
VITE_APP_TITLE=疫苗预约系统
```

### 6.3 API请求配置

`src/api/request.js`:

```javascript
import axios from 'axios'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:9000/api',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
})

// 响应拦截器
request.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default request
```

### 6.4 启动前端

```bash
# 开发模式
npm run dev

# 生产构建
npm run build
```

---

## 七、启动顺序

### 7.1 基础服务启动

```bash
# 1. 启动MySQL
brew services start mysql
# 或
sudo systemctl start mysql

# 2. 启动Redis
brew services start redis
# 或
redis-server

# 3. 启动RabbitMQ (可选)
brew services start rabbitmq
# 或
rabbitmq-server
```

### 7.2 后端服务启动

```bash
# 编译所有服务
mvn clean package -DskipTests

# 按顺序启动服务
# 1. 网关服务
java -jar yimiao-gateway/target/yimiao-gateway-1.0.0.jar \
  --spring.profiles.active=standalone &

# 2. 用户服务
java -jar yimiao-user/target/yimiao-user-1.0.0.jar \
  --spring.cloud.nacos.discovery.enabled=false &

# 3. 疫苗服务
java -jar yimiao-vaccine/target/yimiao-vaccine-1.0.0.jar \
  --spring.cloud.nacos.discovery.enabled=false &

# 4. 预约服务
java -jar yimiao-appointment/target/yimiao-appointment-1.0.0.jar \
  --spring.cloud.nacos.discovery.enabled=false &

# 5. 支付服务
java -jar yimiao-payment/target/yimiao-payment-1.0.0.jar \
  --spring.cloud.nacos.discovery.enabled=false &

# 6. 管理服务
java -jar yimiao-admin/target/yimiao-admin-1.0.0.jar \
  --spring.cloud.nacos.discovery.enabled=false &
```

### 7.3 前端启动

```bash
cd yimiao-frontend
npm run dev
```

### 7.4 一键启动脚本

使用项目提供的 `start.sh`:

```bash
#!/bin/bash

echo "启动基础服务..."
brew services start mysql
brew services start redis
brew services start rabbitmq 2>/dev/null

sleep 5

echo "启动后端服务..."
cd /path/to/yimiao-manage

java -jar yimiao-gateway/target/yimiao-gateway-1.0.0.jar \
  --spring.profiles.active=standalone > logs/gateway.log 2>&1 &

java -jar yimiao-user/target/yimiao-user-1.0.0.jar \
  --spring.cloud.nacos.discovery.enabled=false > logs/user.log 2>&1 &

java -jar yimiao-vaccine/target/yimiao-vaccine-1.0.0.jar \
  --spring.cloud.nacos.discovery.enabled=false > logs/vaccine.log 2>&1 &

java -jar yimiao-appointment/target/yimiao-appointment-1.0.0.jar \
  --spring.cloud.nacos.discovery.enabled=false > logs/appointment.log 2>&1 &

java -jar yimiao-payment/target/yimiao-payment-1.0.0.jar \
  --spring.cloud.nacos.discovery.enabled=false > logs/payment.log 2>&1 &

java -jar yimiao-admin/target/yimiao-admin-1.0.0.jar \
  --spring.cloud.nacos.discovery.enabled=false > logs/admin.log 2>&1 &

sleep 30

echo "启动前端..."
cd yimiao-frontend
npm run dev &

echo "所有服务启动完成!"
echo "前端地址: http://localhost:3000"
echo "API文档: http://localhost:9000/doc.html"
```

---

## 八、功能测试

### 8.1 服务健康检查

```bash
# 检查所有服务状态
curl http://localhost:9000/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8085/actuator/health
curl http://localhost:8087/actuator/health
curl http://localhost:8089/actuator/health
curl http://localhost:8081/actuator/health
```

### 8.2 用户登录测试

```bash
curl -X POST "http://localhost:9000/api/user/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"newadmin","password":"admin123"}'
```

### 8.3 疫苗列表测试

```bash
curl "http://localhost:9000/api/vaccine/available"
```

### 8.4 支付渠道测试

```bash
# 获取支付渠道列表
curl "http://localhost:8089/payment/channel/list"

# 启用支付渠道
curl -X POST "http://localhost:8089/payment/channel/enable/1"

# 禁用支付渠道
curl -X POST "http://localhost:8089/payment/channel/disable/1"
```

---

## 九、常见问题

### 9.1 端口被占用

```bash
# 查看端口占用
lsof -i :8083

# 杀死进程
kill -9 <PID>
```

### 9.2 数据库连接失败

1. 检查MySQL是否启动
2. 检查用户名密码是否正确
3. 检查数据库是否存在
4. 检查防火墙设置

### 9.3 Redis连接失败

```bash
# 检查Redis状态
redis-cli ping

# 如果设置了密码
redis-cli -a your_password ping
```

### 9.4 RabbitMQ连接失败

1. 访问 http://localhost:15672 检查管理界面
2. 检查用户名密码
3. 检查虚拟主机配置

**注意**: RabbitMQ是可选的，连接失败不影响核心功能。

### 9.5 前端跨域问题

网关已配置CORS，如仍有问题，检查:

```yaml
# gateway配置
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
```

### 9.6 支付回调测试

本地开发无法接收外网回调，解决方案:

1. 使用内网穿透工具 (ngrok/frp)
2. 使用系统提供的测试接口

```bash
# 创建测试支付
POST /payment/test/create
{
  "amount": 0.01,
  "channelCode": "alipay",
  "subject": "测试支付"
}

# 完成测试支付
POST /payment/test/complete?tradeNo=xxx&orderNo=xxx&success=true
```

---

## 十、生产环境部署

### 10.1 Docker部署

```dockerfile
# Dockerfile示例
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```yaml
# docker-compose.yml
version: '3'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root123456
      MYSQL_DATABASE: test_ym_1
    volumes:
      - mysql_data:/var/lib/mysql
    ports:
      - "3306:3306"
  
  redis:
    image: redis:6
    ports:
      - "6379:6379"
  
  rabbitmq:
    image: rabbitmq:management
    ports:
      - "5672:5672"
      - "15672:15672"
  
  gateway:
    build: ./yimiao-gateway
    ports:
      - "9000:9000"
    depends_on:
      - mysql
      - redis

volumes:
  mysql_data:
```

### 10.2 Nginx配置

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端
    location / {
        root /var/www/yimiao-frontend/dist;
        try_files $uri $uri/ /index.html;
    }
    
    # API代理
    location /api/ {
        proxy_pass http://localhost:9000/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 10.3 监控与运维

#### 健康检查

每个服务都暴露了健康检查端点:

```bash
curl http://localhost:8083/actuator/health
curl http://localhost:8085/actuator/health
curl http://localhost:8087/actuator/health
curl http://localhost:8089/actuator/health
curl http://localhost:9000/actuator/health
```

#### 日志查看

```bash
# 实时查看日志
tail -f logs/gateway.log
tail -f logs/user.log
```

#### 性能监控

建议集成:
- Prometheus + Grafana (指标监控)
- ELK Stack (日志收集)
- SkyWalking (链路追踪)

---

## 附录

### A. 配置文件清单

| 文件 | 位置 | 说明 |
|------|------|------|
| application.yaml | 各服务resources目录 | 主配置文件 |
| application-standalone.yaml | gateway | 独立模式配置 |
| .env.development | yimiao-frontend | 前端开发环境配置 |
| .env.production | yimiao-frontend | 前端生产环境配置 |

### B. 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 管理员 | newadmin | admin123 |

### C. API文档

启动后访问: http://localhost:9000/doc.html

### D. 相关文档

- [README.md](../README.md) - 项目说明
- [PAYMENT_CONFIG.md](PAYMENT_CONFIG.md) - 支付配置教程
- [API_DOCUMENT.md](../API_DOCUMENT.md) - API文档
