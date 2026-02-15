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

-- 初始化支付渠道数据
INSERT INTO `ym_payment_channel` (`channel_code`, `channel_name`, `channel_icon`, `channel_type`, `api_url`, `notify_url`, `return_url`, `status`, `sort`, `remark`) VALUES
('alipay', '支付宝', 'alipay.png', 1, 'https://openapi.alipay.com/gateway.do', 'http://localhost:8089/payment/notify/alipay', 'http://localhost:3000/pay/result', 1, 1, '支付宝官方支付'),
('wechat', '微信支付', 'wechat.png', 2, 'https://api.mch.weixin.qq.com', 'http://localhost:8089/payment/notify/wechat', 'http://localhost:3000/pay/result', 1, 2, '微信官方支付'),
('epay', '易支付', 'epay.png', 3, 'http://pay.www.com/', 'http://localhost:8089/payment/notify/epay', 'http://localhost:3000/pay/result', 0, 3, '彩虹易支付');

-- 修改支付记录表添加渠道字段
ALTER TABLE `ym_payment_record` ADD COLUMN `channel_code` VARCHAR(50) DEFAULT NULL COMMENT '支付渠道编码' AFTER `pay_type`;
ALTER TABLE `ym_payment_record` MODIFY COLUMN `pay_type` TINYINT NOT NULL COMMENT '支付方式 1微信 2支付宝 3易支付';

-- 同步到数据库2
USE test_ym_2;
CREATE TABLE IF NOT EXISTS `ym_payment_channel` LIKE test_ym_1.ym_payment_channel;
ALTER TABLE `ym_payment_record` ADD COLUMN `channel_code` VARCHAR(50) DEFAULT NULL COMMENT '支付渠道编码' AFTER `pay_type`;
