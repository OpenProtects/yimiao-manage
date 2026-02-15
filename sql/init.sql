-- 疫苗预约系统数据库脚本
-- 数据库1: test_ym_1 (主库)
-- 数据库2: test_ym_2 (从库)

-- 创建数据库
CREATE DATABASE IF NOT EXISTS test_ym_1 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS test_ym_2 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库1
USE test_ym_1;

-- 用户表
CREATE TABLE IF NOT EXISTS `ym_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `user_type` TINYINT DEFAULT 0 COMMENT '用户类型 0普通用户 1管理员 2医生',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0正常 1禁用',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 实名认证表
CREATE TABLE IF NOT EXISTS `ym_real_name_cert` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `id_card` VARCHAR(20) NOT NULL COMMENT '身份证号',
    `status` TINYINT DEFAULT 0 COMMENT '认证状态 0待认证 1已认证 2认证失败',
    `fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    `cert_time` DATETIME DEFAULT NULL COMMENT '认证时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_id_card` (`id_card`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实名认证表';

-- 接种人表
CREATE TABLE IF NOT EXISTS `ym_vaccinee` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '接种人ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `id_card` VARCHAR(20) NOT NULL COMMENT '身份证号',
    `gender` TINYINT NOT NULL COMMENT '性别 1男 2女',
    `birth_date` DATE NOT NULL COMMENT '出生日期',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
    `relation` VARCHAR(20) DEFAULT NULL COMMENT '与用户关系',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认接种人',
    `cert_status` TINYINT DEFAULT 0 COMMENT '认证状态 0待认证 1已认证',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_id_card` (`id_card`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接种人表';

-- 接种点表
CREATE TABLE IF NOT EXISTS `ym_site` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '接种点ID',
    `name` VARCHAR(100) NOT NULL COMMENT '接种点名称',
    `code` VARCHAR(50) NOT NULL COMMENT '接种点编码',
    `region` VARCHAR(50) DEFAULT NULL COMMENT '所属区域',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `longitude` DECIMAL(10, 7) DEFAULT NULL COMMENT '经度',
    `latitude` DECIMAL(10, 7) DEFAULT NULL COMMENT '纬度',
    `business_hours` VARCHAR(100) DEFAULT NULL COMMENT '营业时间',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0正常 1禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_region` (`region`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接种点表';

-- 疫苗信息表
CREATE TABLE IF NOT EXISTS `ym_vaccine` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '疫苗ID',
    `name` VARCHAR(100) NOT NULL COMMENT '疫苗名称',
    `code` VARCHAR(50) NOT NULL COMMENT '疫苗编码',
    `type` VARCHAR(50) DEFAULT NULL COMMENT '疫苗类型',
    `manufacturer` VARCHAR(100) DEFAULT NULL COMMENT '生产厂家',
    `specification` VARCHAR(100) DEFAULT NULL COMMENT '规格',
    `min_age` INT DEFAULT 0 COMMENT '适用年龄下限',
    `max_age` INT DEFAULT 150 COMMENT '适用年龄上限',
    `dose_count` INT DEFAULT 1 COMMENT '接种剂次',
    `dose_interval` INT DEFAULT 28 COMMENT '剂次间隔(天)',
    `price` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '价格',
    `is_free` TINYINT DEFAULT 1 COMMENT '是否免费',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0正常 1禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='疫苗信息表';

-- 疫苗库存表
CREATE TABLE IF NOT EXISTS `ym_vaccine_stock` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `site_id` BIGINT NOT NULL COMMENT '接种点ID',
    `vaccine_id` BIGINT NOT NULL COMMENT '疫苗ID',
    `batch_no` VARCHAR(50) DEFAULT NULL COMMENT '批次号',
    `total_count` INT NOT NULL DEFAULT 0 COMMENT '总数量',
    `used_count` INT NOT NULL DEFAULT 0 COMMENT '已使用数量',
    `remain_count` INT NOT NULL DEFAULT 0 COMMENT '剩余数量',
    `expire_date` DATE DEFAULT NULL COMMENT '过期日期',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0正常 1禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_site_vaccine` (`site_id`, `vaccine_id`),
    KEY `idx_batch_no` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='疫苗库存表';

-- 号源表
CREATE TABLE IF NOT EXISTS `ym_slot` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '号源ID',
    `site_id` BIGINT NOT NULL COMMENT '接种点ID',
    `vaccine_id` BIGINT NOT NULL COMMENT '疫苗ID',
    `slot_date` DATE NOT NULL COMMENT '日期',
    `start_time` TIME NOT NULL COMMENT '开始时间',
    `end_time` TIME NOT NULL COMMENT '结束时间',
    `total_count` INT NOT NULL DEFAULT 0 COMMENT '总号源数',
    `booked_count` INT NOT NULL DEFAULT 0 COMMENT '已预约数',
    `remain_count` INT NOT NULL DEFAULT 0 COMMENT '剩余数',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0可预约 1已满 2已过期',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_site_vaccine_date` (`site_id`, `vaccine_id`, `slot_date`),
    KEY `idx_slot_date` (`slot_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='号源表';

-- 预约订单表
CREATE TABLE IF NOT EXISTS `ym_appointment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `vaccinee_id` BIGINT NOT NULL COMMENT '接种人ID',
    `vaccine_id` BIGINT NOT NULL COMMENT '疫苗ID',
    `site_id` BIGINT NOT NULL COMMENT '接种点ID',
    `slot_id` BIGINT NOT NULL COMMENT '号源ID',
    `dose_no` INT NOT NULL COMMENT '剂次',
    `status` TINYINT DEFAULT 0 COMMENT '订单状态 0待支付 1已支付 2已取消 3已完成 4已过期 5已退款',
    `pay_status` TINYINT DEFAULT 0 COMMENT '支付状态 0未支付 1已支付 2已退款',
    `amount` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '支付金额',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `pay_type` TINYINT DEFAULT NULL COMMENT '支付方式 1微信 2支付宝',
    `pay_trade_no` VARCHAR(100) DEFAULT NULL COMMENT '支付流水号',
    `verify_status` TINYINT DEFAULT 0 COMMENT '核销状态 0未核销 1已核销',
    `verify_time` DATETIME DEFAULT NULL COMMENT '核销时间',
    `verify_user_id` BIGINT DEFAULT NULL COMMENT '核销人ID',
    `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
    `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_vaccinee_id` (`vaccinee_id`),
    KEY `idx_slot_id` (`slot_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约订单表';

-- 支付记录表
CREATE TABLE IF NOT EXISTS `ym_payment_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
    `pay_type` TINYINT NOT NULL COMMENT '支付方式 1微信 2支付宝 3易支付',
    `channel_code` VARCHAR(50) DEFAULT NULL COMMENT '支付渠道编码',
    `trade_no` VARCHAR(100) DEFAULT NULL COMMENT '第三方交易号',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0待支付 1成功 2失败',
    `fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `notify_time` DATETIME DEFAULT NULL COMMENT '通知时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_trade_no` (`trade_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_channel_code` (`channel_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- 支付渠道表
CREATE TABLE IF NOT EXISTS `ym_payment_channel` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `channel_code` VARCHAR(50) NOT NULL COMMENT '渠道编码',
    `channel_name` VARCHAR(100) NOT NULL COMMENT '渠道名称',
    `channel_icon` VARCHAR(255) DEFAULT NULL COMMENT '渠道图标',
    `channel_type` TINYINT DEFAULT 1 COMMENT '渠道类型 1支付宝 2微信 3易支付',
    `api_url` VARCHAR(255) DEFAULT NULL COMMENT 'API地址',
    `app_id` VARCHAR(100) DEFAULT NULL COMMENT '应用ID',
    `app_secret` VARCHAR(255) DEFAULT NULL COMMENT '应用密钥',
    `merchant_id` VARCHAR(100) DEFAULT NULL COMMENT '商户ID',
    `merchant_private_key` TEXT DEFAULT NULL COMMENT '商户私钥',
    `platform_public_key` TEXT DEFAULT NULL COMMENT '平台公钥',
    `notify_url` VARCHAR(255) DEFAULT NULL COMMENT '回调地址',
    `return_url` VARCHAR(255) DEFAULT NULL COMMENT '返回地址',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_channel_code` (`channel_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付渠道表';

-- 退款记录表
CREATE TABLE IF NOT EXISTS `ym_refund_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '退款金额',
    `reason` VARCHAR(255) DEFAULT NULL COMMENT '退款原因',
    `refund_no` VARCHAR(100) DEFAULT NULL COMMENT '退款单号',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0待退款 1成功 2失败',
    `fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    `refund_time` DATETIME DEFAULT NULL COMMENT '退款时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_refund_no` (`refund_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款记录表';

-- 黑名单表
CREATE TABLE IF NOT EXISTS `ym_blacklist` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `id_card` VARCHAR(20) NOT NULL COMMENT '身份证号',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '姓名',
    `reason` VARCHAR(255) DEFAULT NULL COMMENT '原因',
    `type` TINYINT DEFAULT 1 COMMENT '类型 1永久 2临时',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0生效 1失效',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_id_card` (`id_card`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='黑名单表';

-- 风控规则表
CREATE TABLE IF NOT EXISTS `ym_risk_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `code` VARCHAR(50) NOT NULL COMMENT '规则编码',
    `type` VARCHAR(50) NOT NULL COMMENT '规则类型',
    `config` TEXT DEFAULT NULL COMMENT '规则配置JSON',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0启用 1禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控规则表';

-- 通知记录表
CREATE TABLE IF NOT EXISTS `ym_notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `type` VARCHAR(50) NOT NULL COMMENT '通知类型',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` TEXT DEFAULT NULL COMMENT '内容',
    `extra_data` TEXT DEFAULT NULL COMMENT '额外数据JSON',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读',
    `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
    `send_time` DATETIME DEFAULT NULL COMMENT '发送时间',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0待发送 1已发送 2发送失败',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知记录表';

-- 接种记录表
CREATE TABLE IF NOT EXISTS `ym_vaccination_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `vaccinee_id` BIGINT NOT NULL COMMENT '接种人ID',
    `vaccine_id` BIGINT NOT NULL COMMENT '疫苗ID',
    `site_id` BIGINT NOT NULL COMMENT '接种点ID',
    `appointment_id` BIGINT DEFAULT NULL COMMENT '预约订单ID',
    `dose_no` INT NOT NULL COMMENT '剂次',
    `vaccination_time` DATETIME NOT NULL COMMENT '接种时间',
    `batch_no` VARCHAR(50) DEFAULT NULL COMMENT '疫苗批号',
    `doctor_name` VARCHAR(50) DEFAULT NULL COMMENT '接种医生',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_vaccinee_id` (`vaccinee_id`),
    KEY `idx_vaccine_id` (`vaccine_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接种记录表';

-- 排班表
CREATE TABLE IF NOT EXISTS `ym_schedule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `site_id` BIGINT NOT NULL COMMENT '接种点ID',
    `schedule_date` DATE NOT NULL COMMENT '排班日期',
    `start_time` TIME NOT NULL COMMENT '开始时间',
    `end_time` TIME NOT NULL COMMENT '结束时间',
    `total_count` INT NOT NULL DEFAULT 0 COMMENT '总号源数',
    `vaccine_ids` VARCHAR(500) DEFAULT NULL COMMENT '可用疫苗ID列表',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0正常 1禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_site_date` (`site_id`, `schedule_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班表';

-- 管理员表
CREATE TABLE IF NOT EXISTS `ym_admin` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role` VARCHAR(50) DEFAULT 'admin' COMMENT '角色',
    `permissions` TEXT DEFAULT NULL COMMENT '权限列表JSON',
    `site_id` BIGINT DEFAULT NULL COMMENT '所属接种点ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 初始化管理员账号 (密码: admin123, 使用BCrypt加密)
INSERT IGNORE INTO `ym_user` (`username`, `password`, `phone`, `user_type`, `status`) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '13800138000', 1, 0);

INSERT IGNORE INTO `ym_admin` (`user_id`, `role`) VALUES (1, 'super_admin');

-- 初始化测试疫苗数据
INSERT INTO `ym_vaccine` (`name`, `code`, `type`, `manufacturer`, `min_age`, `max_age`, `dose_count`, `dose_interval`, `price`, `is_free`, `status`) VALUES
('新冠疫苗(灭活)', 'COVID19_INACTIVATED', '灭活疫苗', '北京科兴', 3, 150, 2, 21, 0.00, 1, 0),
('新冠疫苗(腺病毒载体)', 'COVID19_ADENOVIRUS', '腺病毒载体疫苗', '康希诺', 18, 150, 1, 0, 0.00, 1, 0),
('乙肝疫苗', 'HEPATITIS_B', '重组疫苗', '华兰生物', 0, 150, 3, 30, 0.00, 1, 0),
('流感疫苗', 'INFLUENZA', '裂解疫苗', '华兰生物', 6, 150, 1, 0, 50.00, 0, 0),
('HPV疫苗(九价)', 'HPV9', '重组疫苗', '默沙东', 16, 45, 3, 60, 1298.00, 0, 0);

-- 初始化测试接种点数据
INSERT INTO `ym_site` (`name`, `code`, `region`, `address`, `phone`, `business_hours`, `status`) VALUES
('朝阳区疫苗接种中心', 'SITE001', '朝阳区', '北京市朝阳区XX路XX号', '010-12345678', '08:00-17:00', 0),
('海淀区疫苗接种中心', 'SITE002', '海淀区', '北京市海淀区XX路XX号', '010-87654321', '08:00-17:00', 0);

-- 初始化测试库存数据
INSERT INTO `ym_vaccine_stock` (`site_id`, `vaccine_id`, `batch_no`, `total_count`, `used_count`, `remain_count`, `status`) VALUES
(1, 1, 'BATCH2024001', 1000, 0, 1000, 0),
(1, 2, 'BATCH2024002', 500, 0, 500, 0),
(2, 1, 'BATCH2024003', 800, 0, 800, 0),
(2, 3, 'BATCH2024004', 600, 0, 600, 0);

-- 初始化支付渠道数据
INSERT INTO `ym_payment_channel` (`channel_code`, `channel_name`, `channel_icon`, `channel_type`, `api_url`, `notify_url`, `return_url`, `status`, `sort`, `remark`) VALUES
('alipay', '支付宝', 'alipay.png', 1, 'https://openapi.alipay.com/gateway.do', 'http://localhost:8089/payment/notify/alipay', 'http://localhost:3000/pay/result', 1, 1, '支付宝官方支付'),
('wechat', '微信支付', 'wechat.png', 2, 'https://api.mch.weixin.qq.com', 'http://localhost:8089/payment/notify/wechat', 'http://localhost:3000/pay/result', 1, 2, '微信官方支付'),
('epay', '易支付', 'epay.png', 3, 'http://pay.www.com/', 'http://localhost:8089/payment/notify/epay', 'http://localhost:3000/pay/result', 0, 3, '彩虹易支付');

-- 同步到数据库2
USE test_ym_2;

CREATE TABLE IF NOT EXISTS `ym_user` LIKE test_ym_1.ym_user;
CREATE TABLE IF NOT EXISTS `ym_real_name_cert` LIKE test_ym_1.ym_real_name_cert;
CREATE TABLE IF NOT EXISTS `ym_vaccinee` LIKE test_ym_1.ym_vaccinee;
CREATE TABLE IF NOT EXISTS `ym_site` LIKE test_ym_1.ym_site;
CREATE TABLE IF NOT EXISTS `ym_vaccine` LIKE test_ym_1.ym_vaccine;
CREATE TABLE IF NOT EXISTS `ym_vaccine_stock` LIKE test_ym_1.ym_vaccine_stock;
CREATE TABLE IF NOT EXISTS `ym_slot` LIKE test_ym_1.ym_slot;
CREATE TABLE IF NOT EXISTS `ym_appointment` LIKE test_ym_1.ym_appointment;
CREATE TABLE IF NOT EXISTS `ym_payment_record` LIKE test_ym_1.ym_payment_record;
CREATE TABLE IF NOT EXISTS `ym_refund_record` LIKE test_ym_1.ym_refund_record;
CREATE TABLE IF NOT EXISTS `ym_blacklist` LIKE test_ym_1.ym_blacklist;
CREATE TABLE IF NOT EXISTS `ym_risk_rule` LIKE test_ym_1.ym_risk_rule;
CREATE TABLE IF NOT EXISTS `ym_notification` LIKE test_ym_1.ym_notification;
CREATE TABLE IF NOT EXISTS `ym_vaccination_record` LIKE test_ym_1.ym_vaccination_record;
CREATE TABLE IF NOT EXISTS `ym_schedule` LIKE test_ym_1.ym_schedule;
CREATE TABLE IF NOT EXISTS `ym_admin` LIKE test_ym_1.ym_admin;
CREATE TABLE IF NOT EXISTS `ym_payment_channel` LIKE test_ym_1.ym_payment_channel;
