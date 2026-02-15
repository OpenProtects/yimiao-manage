package com.yimiao.payment.strategy.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yimiao.payment.entity.PaymentChannel;
import com.yimiao.payment.entity.PaymentRecord;
import com.yimiao.payment.strategy.PaymentStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Component
public class WechatPayStrategy implements PaymentStrategy {

    private static final String CHANNEL_CODE = "wechat";

    @Override
    public String getChannelCode() {
        return CHANNEL_CODE;
    }

    @Override
    public String createPayment(PaymentChannel channel, PaymentRecord record, String subject) {
        JSONObject req = new JSONObject();
        req.set("appid", channel.getAppId());
        req.set("mch_id", channel.getMerchantId());
        req.set("nonce_str", cn.hutool.core.util.IdUtil.fastSimpleUUID());
        req.set("body", subject);
        req.set("out_trade_no", record.getTradeNo());
        req.set("total_fee", record.getAmount().multiply(new BigDecimal("100")).intValue());
        req.set("spbill_create_ip", "127.0.0.1");
        req.set("notify_url", channel.getNotifyUrl());
        req.set("trade_type", "NATIVE");

        log.info("微信支付创建订单: tradeNo={}", record.getTradeNo());
        return channel.getApiUrl() + "?tradeNo=" + record.getTradeNo();
    }

    @Override
    public boolean verifyNotify(PaymentChannel channel, Map<String, String> params) {
        try {
            String returnCode = params.get("return_code");
            String resultCode = params.get("result_code");
            return "SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode);
        } catch (Exception e) {
            log.error("微信支付回调验签失败", e);
            return false;
        }
    }

    @Override
    public boolean queryPayStatus(PaymentChannel channel, String tradeNo) {
        try {
            log.info("微信支付查询订单: tradeNo={}", tradeNo);
            return true;
        } catch (Exception e) {
            log.error("微信支付查询订单失败", e);
            return false;
        }
    }

    @Override
    public String refund(PaymentChannel channel, String tradeNo, String refundNo, BigDecimal amount) {
        try {
            log.info("微信支付退款: tradeNo={}, refundNo={}, amount={}", tradeNo, refundNo, amount);
            return refundNo;
        } catch (Exception e) {
            log.error("微信支付退款失败", e);
            throw new RuntimeException("退款失败: " + e.getMessage());
        }
    }
}
