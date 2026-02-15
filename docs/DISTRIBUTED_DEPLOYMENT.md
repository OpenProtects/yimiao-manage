# 疫苗预约系统 - 分布式部署教程

## 目录

1. [分布式架构概述](#一分布式架构概述)
2. [环境准备](#二环境准备)
3. [Nacos注册中心](#三nacos注册中心)
4. [多节点部署](#四多节点部署)
5. [负载均衡配置](#五负载均衡配置)
6. [前端访问配置](#六前端访问配置)
7. [完整部署示例](#七完整部署示例)
8. [运维监控](#八运维监控)

---

## 一、分布式架构概述

### 1.1 什么是分布式部署？

分布式部署是将一个系统拆分成多个服务，部署在不同的服务器节点上，通过网络进行通信和协作。

```
┌─────────────────────────────────────────────────────────────────────┐
│                           用户/前端                                   │
│                      https://your-domain.com                         │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        Nginx 负载均衡                                 │
│                     (反向代理 + SSL终止)                              │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                    ┌─────────────┼─────────────┐
                    ▼             ▼             ▼
            ┌───────────┐ ┌───────────┐ ┌───────────┐
            │ Gateway-1 │ │ Gateway-2 │ │ Gateway-3 │
            │  节点A    │ │  节点B    │ │  节点C    │
            └───────────┘ └───────────┘ └───────────┘
                    │             │             │
                    └─────────────┼─────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        Nacos 注册中心                                 │
│                    (服务注册与发现、配置中心)                          │
└─────────────────────────────────────────────────────────────────────┘
                                  │
        ┌─────────────┬───────────┼───────────┬─────────────┐
        ▼             ▼           ▼           ▼             ▼
┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐
│  User     │ │  Vaccine  │ │ Appointment│ │  Payment  │ │  Admin    │
│ Service   │ │  Service  │ │  Service   │ │  Service  │ │  Service  │
│ (多实例)   │ │ (多实例)   │ │ (多实例)    │ │ (多实例)   │ │ (多实例)   │
└───────────┘ └───────────┘ └───────────┘ └───────────┘ └───────────┘
        │             │           │           │             │
        └─────────────┴───────────┼───────────┴─────────────┘
                                  │
                    ┌─────────────┼─────────────┐
                    ▼             ▼             ▼
            ┌───────────┐ ┌───────────┐ ┌───────────┐
            │   MySQL   │ │   Redis   │ │ RabbitMQ  │
            │  主从集群  │ │  集群模式  │ │  集群模式  │
            └───────────┘ └───────────┘ └───────────┘
```

### 1.2 分布式部署的优势

| 优势 | 说明 |
|------|------|
| 高可用性 | 单节点故障不影响整体服务 |
| 可扩展性 | 可根据负载动态增减节点 |
| 性能提升 | 多节点并行处理请求 |
| 容灾能力 | 可跨机房/跨地域部署 |

### 1.3 核心组件

| 组件 | 作用 | 推荐方案 |
|------|------|----------|
| 注册中心 | 服务注册与发现 | Nacos |
| 配置中心 | 统一配置管理 | Nacos |
| 负载均衡 | 流量分发 | Nginx / Gateway |
| 分布式缓存 | 数据缓存、分布式锁 | Redis Cluster |
| 消息队列 | 异步通信、削峰填谷 | RabbitMQ Cluster |
| 数据库 | 数据持久化 | MySQL 主从 |

---

## 二、环境准备

### 2.1 服务器规划

假设我们有3台服务器，IP分别为：

| 服务器 | IP | 部署服务 |
|--------|-----|----------|
| 节点A | 192.168.1.101 | Nacos、MySQL、Redis、Gateway、User、Vaccine |
| 节点B | 192.168.1.102 | Gateway、User、Vaccine、Appointment、Payment |
| 节点C | 192.168.1.103 | Gateway、Admin、Appointment、Payment |

### 2.2 基础环境安装

每台服务器都需要安装：

```bash
# 1. 安装JDK 17
sudo apt update
sudo apt install openjdk-17-jdk -y
java -version

# 2. 安装Docker (推荐)
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# 3. 配置防火墙
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 8848/tcp  # Nacos
sudo ufw allow 9000/tcp  # Gateway
sudo ufw allow 8081:8090/tcp  # 微服务端口
```

### 2.3 网络配置

确保各节点之间网络互通：

```bash
# 在节点A测试连接节点B
ping 192.168.1.102

# 测试端口连通性
telnet 192.168.1.102 8848
```

---

## 三、Nacos注册中心

### 3.1 什么是Nacos？

Nacos (Dynamic Naming and Configuration Service) 是阿里巴巴开源的服务注册与配置中心，提供：
- **服务注册与发现**：服务自动注册，自动发现其他服务
- **配置管理**：统一管理各服务配置，支持热更新
- **健康检查**：自动检测服务健康状态

### 3.2 安装Nacos

#### 方式一：Docker部署（推荐）

```bash
# 单机模式
docker run -d \
  --name nacos \
  -e MODE=standalone \
  -e SPRING_DATASOURCE_PLATFORM=mysql \
  -e MYSQL_SERVICE_HOST=192.168.1.101 \
  -e MYSQL_SERVICE_PORT=3306 \
  -e MYSQL_SERVICE_DB_NAME=nacos \
  -e MYSQL_SERVICE_USER=root \
  -e MYSQL_SERVICE_PASSWORD=123456 \
  -p 8848:8848 \
  -p 9848:9848 \
  nacos/nacos-server:v2.2.3

# 集群模式（3节点）
# 节点A
docker run -d \
  --name nacos \
  -e MODE=cluster \
  -e NACOS_SERVERS="192.168.1.101:8848 192.168.1.102:8848 192.168.1.103:8848" \
  -e SPRING_DATASOURCE_PLATFORM=mysql \
  -e MYSQL_SERVICE_HOST=192.168.1.101 \
  -e MYSQL_SERVICE_PORT=3306 \
  -e MYSQL_SERVICE_DB_NAME=nacos \
  -e MYSQL_SERVICE_USER=root \
  -e MYSQL_SERVICE_PASSWORD=123456 \
  -p 8848:8848 \
  -p 9848:9848 \
  nacos/nacos-server:v2.2.3
```

#### 方式二：下载安装包

```bash
# 下载
wget https://github.com/alibaba/nacos/releases/download/2.2.3/nacos-server-2.2.3.tar.gz
tar -xzf nacos-server-2.2.3.tar.gz
cd nacos

# 单机模式启动
sh bin/startup.sh -m standalone

# 集群模式启动
# 先配置 cluster.conf
cp conf/cluster.conf.example conf/cluster.conf
echo "192.168.1.101:8848" >> conf/cluster.conf
echo "192.168.1.102:8848" >> conf/cluster.conf
echo "192.168.1.103:8848" >> conf/cluster.conf

# 配置数据库
vim conf/application.properties
# 修改数据库配置

# 启动
sh bin/startup.sh
```

### 3.3 初始化Nacos数据库

```sql
-- 创建Nacos数据库
CREATE DATABASE nacos DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 导入Nacos表结构
-- 表结构文件在 nacos/conf/mysql-schema.sql
```

### 3.4 访问Nacos控制台

- 地址：http://192.168.1.101:8848/nacos
- 默认账号：nacos
- 默认密码：nacos

### 3.5 配置命名空间

在Nacos控制台创建命名空间：

| 命名空间ID | 命名空间名 | 说明 |
|------------|------------|------|
| dev | 开发环境 | 开发测试使用 |
| test | 测试环境 | 集成测试使用 |
| prod | 生产环境 | 正式环境使用 |

---

## 四、多节点部署

### 4.1 服务配置Nacos

修改各微服务的 `application.yaml`：

```yaml
spring:
  application:
    name: yimiao-user  # 服务名称
  
  cloud:
    nacos:
      # 服务注册配置
      discovery:
        server-addr: 192.168.1.101:8848,192.168.1.102:8848,192.168.1.103:8848
        namespace: prod  # 命名空间
        group: DEFAULT_GROUP
        # 服务元数据
        metadata:
          version: 1.0.0
          region: cn-east
      
      # 配置中心
      config:
        server-addr: 192.168.1.101:8848,192.168.1.102:8848,192.168.1.103:8848
        namespace: prod
        group: DEFAULT_GROUP
        file-extension: yaml
        # 共享配置
        shared-configs:
          - data-id: common.yaml
            group: DEFAULT_GROUP
            refresh: true
```

### 4.2 在Nacos创建配置

在Nacos控制台创建配置文件：

#### 公共配置 (common.yaml)

```yaml
spring:
  datasource:
    url: jdbc:mysql://192.168.1.101:3306/test_ym_1?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 10
      min-idle: 10
      max-active: 50

  redis:
    host: 192.168.1.101
    port: 6379
    password: 
    database: 0
    lettuce:
      pool:
        max-active: 50
        max-idle: 20
        min-idle: 10

  rabbitmq:
    host: 192.168.1.101
    port: 5672
    username: guest
    password: guest

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true

logging:
  level:
    com.yimiao: info
```

#### 用户服务配置 (yimiao-user.yaml)

```yaml
server:
  port: 8083

spring:
  application:
    name: yimiao-user
```

### 4.3 打包部署

#### 编译打包

```bash
# 在开发机器上编译
mvn clean package -DskipTests

# 生成的jar包在各模块的target目录下
ls -la yimiao-user/target/*.jar
```

#### 上传到服务器

```bash
# 创建部署目录
ssh user@192.168.1.101 "mkdir -p /opt/yimiao/{user,vaccine,appointment,payment,admin,gateway}"

# 上传jar包
scp yimiao-user/target/yimiao-user-1.0.0.jar user@192.168.1.101:/opt/yimiao/user/
scp yimiao-vaccine/target/yimiao-vaccine-1.0.0.jar user@192.168.1.101:/opt/yimiao/vaccine/
scp yimiao-gateway/target/yimiao-gateway-1.0.0.jar user@192.168.1.101:/opt/yimiao/gateway/

# 上传到节点B
scp yimiao-user/target/yimiao-user-1.0.0.jar user@192.168.1.102:/opt/yimiao/user/
scp yimiao-gateway/target/yimiao-gateway-1.0.0.jar user@192.168.1.102:/opt/yimiao/gateway/
```

### 4.4 创建启动脚本

在每个服务目录创建 `start.sh`：

```bash
#!/bin/bash
# /opt/yimiao/user/start.sh

APP_NAME="yimiao-user"
JAR_FILE="/opt/yimiao/user/yimiao-user-1.0.0.jar"
LOG_DIR="/opt/yimiao/logs"

mkdir -p $LOG_DIR

case "$1" in
  start)
    echo "Starting $APP_NAME..."
    nohup java -Xms512m -Xmx1024m \
      -jar $JAR_FILE \
      --spring.profiles.active=prod \
      > $LOG_DIR/$APP_NAME.log 2>&1 &
    echo $! > /tmp/$APP_NAME.pid
    echo "$APP_NAME started. PID: $(cat /tmp/$APP_NAME.pid)"
    ;;
  stop)
    echo "Stopping $APP_NAME..."
    if [ -f /tmp/$APP_NAME.pid ]; then
      kill $(cat /tmp/$APP_NAME.pid)
      rm /tmp/$APP_NAME.pid
      echo "$APP_NAME stopped."
    else
      echo "PID file not found."
    fi
    ;;
  restart)
    $0 stop
    sleep 3
    $0 start
    ;;
  status)
    if [ -f /tmp/$APP_NAME.pid ]; then
      ps -p $(cat /tmp/$APP_NAME.pid) > /dev/null
      if [ $? -eq 0 ]; then
        echo "$APP_NAME is running. PID: $(cat /tmp/$APP_NAME.pid)"
      else
        echo "$APP_NAME is not running."
      fi
    else
      echo "$APP_NAME is not running."
    fi
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|status}"
    exit 1
    ;;
esac
```

### 4.5 使用Systemd管理服务

创建 `/etc/systemd/system/yimiao-user.service`：

```ini
[Unit]
Description=Yimiao User Service
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/yimiao/user
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar yimiao-user-1.0.0.jar --spring.profiles.active=prod
ExecStop=/bin/kill -15 $MAINPID
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

管理命令：

```bash
# 重载配置
sudo systemctl daemon-reload

# 启动服务
sudo systemctl start yimiao-user

# 停止服务
sudo systemctl stop yimiao-user

# 重启服务
sudo systemctl restart yimiao-user

# 查看状态
sudo systemctl status yimiao-user

# 开机自启
sudo systemctl enable yimiao-user

# 查看日志
sudo journalctl -u yimiao-user -f
```

---

## 五、负载均衡配置

### 5.1 Nginx负载均衡

#### 安装Nginx

```bash
# Ubuntu
sudo apt install nginx -y

# CentOS
sudo yum install nginx -y
```

#### 配置负载均衡

编辑 `/etc/nginx/nginx.conf`：

```nginx
# 定义上游服务器组
upstream gateway-cluster {
    # 负载均衡策略：轮询（默认）
    server 192.168.1.101:9000 weight=1;
    server 192.168.1.102:9000 weight=1;
    server 192.168.1.103:9000 weight=1;
    
    # 健康检查
    keepalive 32;
}

# HTTP服务器
server {
    listen 80;
    server_name your-domain.com;
    
    # 重定向到HTTPS
    return 301 https://$server_name$request_uri;
}

# HTTPS服务器
server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    # SSL证书配置
    ssl_certificate /etc/nginx/ssl/your-domain.com.crt;
    ssl_certificate_key /etc/nginx/ssl/your-domain.com.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    
    # 前端静态资源
    location / {
        root /var/www/yimiao-frontend/dist;
        try_files $uri $uri/ /index.html;
        
        # 静态资源缓存
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            expires 30d;
            add_header Cache-Control "public, immutable";
        }
    }
    
    # API代理
    location /api/ {
        proxy_pass http://gateway-cluster/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时配置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # WebSocket支持
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
    
    # 健康检查端点
    location /health {
        access_log off;
        return 200 "OK";
        add_header Content-Type text/plain;
    }
}
```

#### 负载均衡策略

```nginx
upstream gateway-cluster {
    # 1. 轮询（默认）
    server 192.168.1.101:9000;
    server 192.168.1.102:9000;
    server 192.168.1.103:9000;
    
    # 2. 加权轮询
    server 192.168.1.101:9000 weight=3;  # 3/6 流量
    server 192.168.1.102:9000 weight=2;  # 2/6 流量
    server 192.168.1.103:9000 weight=1;  # 1/6 流量
    
    # 3. IP哈希（会话保持）
    ip_hash;
    server 192.168.1.101:9000;
    server 192.168.1.102:9000;
    
    # 4. 最少连接
    least_conn;
    server 192.168.1.101:9000;
    server 192.168.1.102:9000;
    
    # 5. 健康检查（需要nginx-plus或第三方模块）
    server 192.168.1.101:9000 max_fails=3 fail_timeout=30s;
    server 192.168.1.102:9000 backup;  # 备用服务器
}
```

### 5.2 Gateway负载均衡

Gateway内部使用Spring Cloud LoadBalancer实现负载均衡：

```yaml
# gateway配置
spring:
  cloud:
    loadbalancer:
      ribbon:
        enabled: false
      cache:
        enabled: true
      retry:
        enabled: true
```

### 5.3 健康检查配置

```nginx
# 主动健康检查
upstream gateway-cluster {
    server 192.168.1.101:9000 max_fails=3 fail_timeout=30s;
    server 192.168.1.102:9000 max_fails=3 fail_timeout=30s;
    server 192.168.1.103:9000 max_fails=3 fail_timeout=30s;
}

# max_fails: 在fail_timeout时间内失败次数超过此值，标记为不可用
# fail_timeout: 服务器被标记为不可用的时间
```

---

## 六、前端访问配置

### 6.1 前端部署

#### 构建生产版本

```bash
cd yimiao-frontend

# 配置生产环境变量
cat > .env.production << EOF
VITE_API_BASE_URL=https://your-domain.com/api
VITE_APP_TITLE=疫苗预约系统
EOF

# 构建
npm run build

# 生成的文件在 dist 目录
ls -la dist/
```

#### 部署到Nginx服务器

```bash
# 上传dist目录到服务器
scp -r dist/* user@192.168.1.101:/var/www/yimiao-frontend/

# 或者使用rsync
rsync -avz --delete dist/ user@192.168.1.101:/var/www/yimiao-frontend/
```

### 6.2 前端访问流程

```
用户浏览器
    │
    │ https://your-domain.com
    ▼
Nginx (192.168.1.101:443)
    │
    ├─ 静态资源 → /var/www/yimiao-frontend/dist/
    │
    └─ API请求 → http://gateway-cluster/api/
                    │
                    ├─ Gateway-1 (192.168.1.101:9000)
                    ├─ Gateway-2 (192.168.1.102:9000)
                    └─ Gateway-3 (192.168.1.103:9000)
                           │
                           ▼
                    Nacos服务发现
                           │
                           ├─ User Service (多实例)
                           ├─ Vaccine Service (多实例)
                           └─ ...其他服务
```

### 6.3 跨域配置

在Gateway中已配置CORS：

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOriginPatterns: "*"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
            maxAge: 3600
```

### 6.4 前端API请求配置

```javascript
// src/api/request.js
import axios from 'axios'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
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

---

## 七、完整部署示例

### 7.1 部署架构图

```
                         ┌─────────────────────────────────────────┐
                         │              互联网用户                   │
                         └─────────────────────────────────────────┘
                                              │
                                              ▼
                         ┌─────────────────────────────────────────┐
                         │           Nginx (负载均衡)               │
                         │           192.168.1.100                 │
                         │         SSL终止 + 反向代理               │
                         └─────────────────────────────────────────┘
                                              │
                    ┌─────────────────────────┼─────────────────────────┐
                    │                         │                         │
                    ▼                         ▼                         ▼
           ┌───────────────┐        ┌───────────────┐        ┌───────────────┐
           │   节点A        │        │   节点B        │        │   节点C        │
           │ 192.168.1.101 │        │ 192.168.1.102 │        │ 192.168.1.103 │
           ├───────────────┤        ├───────────────┤        ├───────────────┤
           │ Nacos         │        │ Nacos         │        │ Nacos         │
           │ Gateway:9000  │        │ Gateway:9000  │        │ Gateway:9000  │
           │ User:8083     │        │ User:8083     │        │ Admin:8081    │
           │ Vaccine:8085  │        │ Vaccine:8085  │        │               │
           │               │        │ Appointment   │        │ Appointment   │
           │ MySQL Master  │        │ Payment:8089  │        │ Payment:8089  │
           │ Redis Master  │        │               │        │               │
           │ RabbitMQ      │        │               │        │               │
           └───────────────┘        └───────────────┘        └───────────────┘
                    │                         │                         │
                    └─────────────────────────┼─────────────────────────┘
                                              │
                                              ▼
                         ┌─────────────────────────────────────────┐
                         │          Nacos 注册中心集群               │
                         │   服务注册发现 + 配置中心                  │
                         └─────────────────────────────────────────┘
```

### 7.2 一键部署脚本

创建 `deploy.sh`：

```bash
#!/bin/bash

# 配置变量
NODES=("192.168.1.101" "192.168.1.102" "192.168.1.103")
USER="root"
PROJECT_DIR="/opt/yimiao"
NACOS_ADDR="192.168.1.101:8848,192.168.1.102:8848,192.168.1.103:8848"

echo "========== 开始部署 =========="

# 1. 编译项目
echo ">>> 编译项目..."
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "编译失败！"
    exit 1
fi

# 2. 部署到各节点
echo ">>> 部署到节点A (192.168.1.101)..."
ssh $USER@${NODES[0]} "mkdir -p $PROJECT_DIR/{gateway,user,vaccine,admin,appointment,payment,logs}"
scp yimiao-gateway/target/yimiao-gateway-1.0.0.jar $USER@${NODES[0]}:$PROJECT_DIR/gateway/
scp yimiao-user/target/yimiao-user-1.0.0.jar $USER@${NODES[0]}:$PROJECT_DIR/user/
scp yimiao-vaccine/target/yimiao-vaccine-1.0.0.jar $USER@${NODES[0]}:$PROJECT_DIR/vaccine/
scp yimiao-admin/target/yimiao-admin-1.0.0.jar $USER@${NODES[0]}:$PROJECT_DIR/admin/

echo ">>> 部署到节点B (192.168.1.102)..."
ssh $USER@${NODES[1]} "mkdir -p $PROJECT_DIR/{gateway,user,vaccine,appointment,payment,logs}"
scp yimiao-gateway/target/yimiao-gateway-1.0.0.jar $USER@${NODES[1]}:$PROJECT_DIR/gateway/
scp yimiao-user/target/yimiao-user-1.0.0.jar $USER@${NODES[1]}:$PROJECT_DIR/user/
scp yimiao-vaccine/target/yimiao-vaccine-1.0.0.jar $USER@${NODES[1]}:$PROJECT_DIR/vaccine/
scp yimiao-appointment/target/yimiao-appointment-1.0.0.jar $USER@${NODES[1]}:$PROJECT_DIR/appointment/
scp yimiao-payment/target/yimiao-payment-1.0.0.jar $USER@${NODES[1]}:$PROJECT_DIR/payment/

echo ">>> 部署到节点C (192.168.1.103)..."
ssh $USER@${NODES[2]} "mkdir -p $PROJECT_DIR/{gateway,admin,appointment,payment,logs}"
scp yimiao-gateway/target/yimiao-gateway-1.0.0.jar $USER@${NODES[2]}:$PROJECT_DIR/gateway/
scp yimiao-admin/target/yimiao-admin-1.0.0.jar $USER@${NODES[2]}:$PROJECT_DIR/admin/
scp yimiao-appointment/target/yimiao-appointment-1.0.0.jar $USER@${NODES[2]}:$PROJECT_DIR/appointment/
scp yimiao-payment/target/yimiao-payment-1.0.0.jar $USER@${NODES[2]}:$PROJECT_DIR/payment/

# 3. 启动服务
echo ">>> 启动服务..."

# 节点A
ssh $USER@${NODES[0]} "cd $PROJECT_DIR/gateway && nohup java -jar yimiao-gateway-1.0.0.jar --spring.cloud.nacos.discovery.server-addr=$NACOS_ADDR > ../logs/gateway.log 2>&1 &"
ssh $USER@${NODES[0]} "cd $PROJECT_DIR/user && nohup java -jar yimiao-user-1.0.0.jar --spring.cloud.nacos.discovery.server-addr=$NACOS_ADDR > ../logs/user.log 2>&1 &"
ssh $USER@${NODES[0]} "cd $PROJECT_DIR/vaccine && nohup java -jar yimiao-vaccine-1.0.0.jar --spring.cloud.nacos.discovery.server-addr=$NACOS_ADDR > ../logs/vaccine.log 2>&1 &"
ssh $USER@${NODES[0]} "cd $PROJECT_DIR/admin && nohup java -jar yimiao-admin-1.0.0.jar --spring.cloud.nacos.discovery.server-addr=$NACOS_ADDR > ../logs/admin.log 2>&1 &"

# 节点B
ssh $USER@${NODES[1]} "cd $PROJECT_DIR/gateway && nohup java -jar yimiao-gateway-1.0.0.jar --spring.cloud.nacos.discovery.server-addr=$NACOS_ADDR > ../logs/gateway.log 2>&1 &"
ssh $USER@${NODES[1]} "cd $PROJECT_DIR/user && nohup java -jar yimiao-user-1.0.0.jar --spring.cloud.nacos.discovery.server-addr=$NACOS_ADDR > ../logs/user.log 2>&1 &"
ssh $USER@${NODES[1]} "cd $PROJECT_DIR/vaccine && nohup java -jar yimiao-vaccine-1.0.0.jar --spring.cloud.nacos.discovery.server-addr=$NACOS_ADDR > ../logs/vaccine.log 2>&1 &"
ssh $USER@${NODES[1]} "cd $PROJECT_DIR/appointment && nohup java -jar yimiao-appointment-1.0.0.jar --spring.cloud.nacos.discovery.server-addr=$NACOS_ADDR > ../logs/appointment.log 2>&1 &"
ssh $USER@${NODES[1]} "cd $PROJECT_DIR/payment && nohup java -jar yimiao-payment-1.0.0.jar --spring.cloud.nacos.discovery.server-addr=$NACOS_ADDR > ../logs/payment.log 2>&1 &"

# 节点C
ssh $USER@${NODES[2]} "cd $PROJECT_DIR/gateway && nohup java -jar yimiao-gateway-1.0.0.jar --spring.cloud.nacos.discovery.server-addr=$NACOS_ADDR > ../logs/gateway.log 2>&1 &"
ssh $USER@${NODES[2]} "cd $PROJECT_DIR/admin && nohup java -jar yimiao-admin-1.0.0.jar --spring.cloud.nacos.discovery.server-addr=$NACOS_ADDR > ../logs/admin.log 2>&1 &"
ssh $USER@${NODES[2]} "cd $PROJECT_DIR/appointment && nohup java -jar yimiao-appointment-1.0.0.jar --spring.cloud.nacos.discovery.server-addr=$NACOS_ADDR > ../logs/appointment.log 2>&1 &"
ssh $USER@${NODES[2]} "cd $PROJECT_DIR/payment && nohup java -jar yimiao-payment-1.0.0.jar --spring.cloud.nacos.discovery.server-addr=$NACOS_ADDR > ../logs/payment.log 2>&1 &"

echo "========== 部署完成 =========="
echo "请等待30秒后检查服务状态..."
sleep 30

# 4. 检查服务状态
echo ">>> 检查服务状态..."
for node in "${NODES[@]}"; do
    echo "节点 $node:"
    ssh $USER@$node "curl -s http://localhost:9000/actuator/health 2>/dev/null | grep -o '\"status\":\"[^\"]*\"'" || echo "  Gateway: 未响应"
done

echo ""
echo ">>> 访问地址:"
echo "前端: https://your-domain.com"
echo "API文档: https://your-domain.com/api/doc.html"
echo "Nacos控制台: http://192.168.1.101:8848/nacos"
```

### 7.3 Docker Compose部署

创建 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  # Nacos注册中心
  nacos:
    image: nacos/nacos-server:v2.2.3
    container_name: nacos
    environment:
      - MODE=standalone
      - SPRING_DATASOURCE_PLATFORM=mysql
      - MYSQL_SERVICE_HOST=mysql
      - MYSQL_SERVICE_PORT=3306
      - MYSQL_SERVICE_DB_NAME=nacos
      - MYSQL_SERVICE_USER=root
      - MYSQL_SERVICE_PASSWORD=123456
    ports:
      - "8848:8848"
      - "9848:9848"
    depends_on:
      - mysql
    networks:
      - yimiao-network

  # MySQL
  mysql:
    image: mysql:8.0
    container_name: mysql
    environment:
      - MYSQL_ROOT_PASSWORD=123456
      - MYSQL_DATABASE=test_ym_1
    volumes:
      - mysql-data:/var/lib/mysql
      - ./sql/init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "3306:3306"
    networks:
      - yimiao-network

  # Redis
  redis:
    image: redis:6
    container_name: redis
    ports:
      - "6379:6379"
    networks:
      - yimiao-network

  # RabbitMQ
  rabbitmq:
    image: rabbitmq:management
    container_name: rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    networks:
      - yimiao-network

  # Gateway服务
  gateway:
    image: openjdk:17-jdk-slim
    container_name: gateway
    working_dir: /app
    volumes:
      - ./yimiao-gateway/target/yimiao-gateway-1.0.0.jar:/app/app.jar
    command: java -jar app.jar --spring.cloud.nacos.discovery.server-addr=nacos:8848
    ports:
      - "9000:9000"
    depends_on:
      - nacos
    networks:
      - yimiao-network

  # User服务
  user:
    image: openjdk:17-jdk-slim
    container_name: user
    working_dir: /app
    volumes:
      - ./yimiao-user/target/yimiao-user-1.0.0.jar:/app/app.jar
    command: java -jar app.jar --spring.cloud.nacos.discovery.server-addr=nacos:8848
    ports:
      - "8083:8083"
    depends_on:
      - nacos
      - mysql
      - redis
    networks:
      - yimiao-network

  # 其他服务类似...

networks:
  yimiao-network:
    driver: bridge

volumes:
  mysql-data:
```

启动：

```bash
docker-compose up -d
```

---

## 八、运维监控

### 8.1 服务健康检查

```bash
# 检查所有服务
for port in 9000 8081 8083 8085 8087 8089; do
    echo -n "端口 $port: "
    curl -s http://localhost:$port/actuator/health | grep -o '"status":"[^"]*"'
done
```

### 8.2 Nacos服务列表

访问 Nacos 控制台查看服务注册情况：
- 地址：http://192.168.1.101:8848/nacos
- 菜单：服务管理 -> 服务列表

### 8.3 日志收集

建议使用 ELK Stack 收集日志：

```yaml
# filebeat配置
filebeat.inputs:
  - type: log
    paths:
      - /opt/yimiao/logs/*.log
    fields:
      service: yimiao

output.elasticsearch:
  hosts: ["localhost:9200"]
```

### 8.4 监控告警

建议使用 Prometheus + Grafana：

```yaml
# prometheus配置
scrape_configs:
  - job_name: 'yimiao-services'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
          - '192.168.1.101:9000'
          - '192.168.1.101:8083'
          - '192.168.1.101:8085'
          - '192.168.1.102:9000'
          - '192.168.1.102:8083'
```

---

## 总结

### 分布式部署核心步骤

1. **安装Nacos注册中心** - 服务注册与发现
2. **配置服务连接Nacos** - 各微服务配置Nacos地址
3. **多节点部署服务** - 将服务部署到多个服务器
4. **配置负载均衡** - Nginx分发流量到多个Gateway
5. **前端访问** - 通过Nginx统一入口访问

### 关键配置清单

| 配置项 | 配置文件 | 说明 |
|--------|----------|------|
| Nacos地址 | application.yaml | spring.cloud.nacos.discovery.server-addr |
| 服务名称 | application.yaml | spring.application.name |
| 命名空间 | application.yaml | spring.cloud.nacos.discovery.namespace |
| 负载均衡 | nginx.conf | upstream配置 |
| 前端API地址 | .env.production | VITE_API_BASE_URL |

### 常见问题

1. **服务注册不上Nacos**：检查网络连通性和Nacos配置
2. **服务间调用失败**：检查服务名称是否正确
3. **负载不均衡**：检查Nginx配置和权重设置
4. **前端跨域**：检查Gateway CORS配置

---

更多详细信息请参考：
- [README.md](../README.md)
- [SYSTEM_CONFIG.md](SYSTEM_CONFIG.md)
