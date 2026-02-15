# 疫苗预约系统 API 端点文档

## 用户服务 (yimiao-user: 8083/8084)

### 用户管理
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /user/login | 用户登录 |
| POST | /user/register | 用户注册 |
| POST | /user/logout | 用户登出 |
| GET | /user/info | 获取用户信息 |
| PUT | /user/password | 修改密码 |
| POST | /user/password/reset | 重置密码 |

### 短信服务
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /sms/send | 发送短信验证码 |
| POST | /sms/verify | 验证短信验证码 |

### 接种人管理
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /vaccinee/list | 获取接种人列表 |
| GET | /vaccinee/{id} | 获取接种人详情 |
| POST | /vaccinee | 添加接种人 |
| PUT | /vaccinee | 更新接种人 |
| DELETE | /vaccinee/{id} | 删除接种人 |
| PUT | /vaccinee/{id}/default | 设为默认接种人 |

### 实名认证
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /cert/apply | 申请实名认证 |
| GET | /cert/status | 获取认证状态 |
| GET | /cert/check | 检查是否已认证 |

## 疫苗库存服务 (yimiao-vaccine: 8085/8086)

### 疫苗管理
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /vaccine/page | 分页查询疫苗 |
| GET | /vaccine/{id} | 获取疫苗详情 |
| POST | /vaccine | 添加疫苗 |
| PUT | /vaccine | 更新疫苗 |
| DELETE | /vaccine/{id} | 删除疫苗 |
| GET | /vaccine/available | 获取可用疫苗列表 |
| GET | /vaccine/check-age | 检查年龄是否符合 |

### 接种点管理
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /site/page | 分页查询接种点 |
| GET | /site/{id} | 获取接种点详情 |
| POST | /site | 添加接种点 |
| PUT | /site | 更新接种点 |
| DELETE | /site/{id} | 删除接种点 |
| GET | /site/all | 获取所有接种点 |
| GET | /site/region/{region} | 按区域查询接种点 |

### 号源管理
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /slot/page | 分页查询号源 |
| GET | /slot/available | 获取可用号源 |
| GET | /slot/{id} | 获取号源详情 |
| POST | /slot | 添加号源 |
| PUT | /slot | 更新号源 |
| DELETE | /slot/{id} | 删除号源 |
| POST | /slot/book/{id} | 预约号源 |
| POST | /slot/cancel/{id} | 取消预约 |
| POST | /slot/generate | 批量生成号源 |

### 库存管理
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /stock/available | 查询可用库存 |
| POST | /stock/deduct | 扣减库存 |
| POST | /stock/add | 增加库存 |
| POST | /stock/init-cache | 初始化缓存 |
| GET | /stock/list/{siteId} | 获取接种点库存 |

## 预约订单服务 (yimiao-appointment: 8087/8088)

### 预约管理
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /appointment | 创建预约 |
| POST | /appointment/cancel/{id} | 取消预约 |
| GET | /appointment/{id} | 获取预约详情 |
| GET | /appointment/page | 分页查询预约 |

### 风控管理
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /risk/check | 风控检查 |
| GET | /risk/queue/{userId} | 获取排队信息 |
| POST | /risk/rate-limit/try | 尝试限流 |

### 黑名单管理
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /blacklist/check/{idCard} | 检查黑名单 |
| POST | /blacklist/add | 添加黑名单 |
| POST | /blacklist/remove/{idCard} | 移除黑名单 |
| GET | /blacklist/{idCard} | 获取黑名单详情 |

## 支付通知服务 (yimiao-payment: 8089/8090)

### 支付管理
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /payment/create | 创建支付 |
| GET | /payment/pay-url/{tradeNo} | 获取支付链接 |
| POST | /payment/notify/{channelCode} | 支付渠道回调 |
| POST | /payment/notify | 支付回调(内部) |
| GET | /payment/status/{orderNo} | 查询支付状态 |
| POST | /payment/close/{orderNo} | 关闭支付 |
| GET | /payment/record/{orderNo} | 获取支付记录 |
| GET | /payment/channels | 获取可用支付渠道 |

### 支付渠道管理
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /payment/channel/list | 获取所有渠道 |
| POST | /payment/channel/enable/{id} | 启用渠道 |
| POST | /payment/channel/disable/{id} | 禁用渠道 |
| POST | /payment/channel/update | 更新渠道配置 |
| GET | /payment/channel/{id} | 获取渠道详情 |

### 退款管理
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /refund/create | 创建退款 |
| POST | /refund/notify | 退款回调 |
| GET | /refund/record/{orderNo} | 获取退款记录 |

### 通知管理
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /notification/appointment-success | 预约成功通知 |
| POST | /notification/appointment-cancel | 预约取消通知 |
| POST | /notification/vaccination-reminder | 接种提醒通知 |
| POST | /notification/payment-success | 支付成功通知 |
| POST | /notification/refund-success | 退款成功通知 |

## 运营管理服务 (yimiao-admin: 8081/8082)

### 统计分析
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /statistics/overview | 数据概览 |
| GET | /statistics/daily | 每日统计 |
| GET | /statistics/appointment | 预约统计 |
| GET | /statistics/vaccine | 疫苗统计 |
| GET | /statistics/site/{siteId} | 接种点统计 |

### 管理员管理
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /admin/{userId} | 获取管理员信息 |
| GET | /admin/permission/check | 检查权限 |

## 支持的支付渠道

| 渠道代码 | 渠道名称 | 渠道类型 | 状态 |
|----------|----------|----------|------|
| alipay | 支付宝 | 1 | 启用 |
| wechat | 微信支付 | 2 | 启用 |
| epay | 易支付 | 3 | 可配置 |

## 支付渠道类型说明

- **类型1 - 支付宝**: 官方支付宝支付
- **类型2 - 微信支付**: 官方微信支付
- **类型3 - 易支付**: 彩虹易支付(支持alipay/wxpay/qqpay/jdpay)

## 默认账号

- 管理员: `admin` / `admin123`
- 测试手机号: `13800138000`
