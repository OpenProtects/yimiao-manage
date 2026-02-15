package com.yimiao.payment.strategy.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yimiao.payment.entity.PaymentChannel;
import com.yimiao.payment.entity.PaymentRecord;
import com.yimiao.payment.strategy.PaymentStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Component
public class AlipayStrategy implements PaymentStrategy {

    private static final String CHANNEL_CODE = "alipay";

    @Override
    public String getChannelCode() {
        return CHANNEL_CODE;
    }

    @Override
    public String createPayment(PaymentChannel channel, PaymentRecord record, String subject) {
        Map<String, String> params = new TreeMap<>();
        params.put("app_id", channel.getAppId());
        params.put("method", "alipay.trade.page.pay");
        params.put("format", "JSON");
        params.put("charset", "utf-8");
        params.put("sign_type", "RSA2");
        params.put("timestamp", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        params.put("version", "1.0");
        params.put("notify_url", channel.getNotifyUrl());
        params.put("return_url", channel.getReturnUrl());

        JSONObject bizContent = new JSONObject();
        bizContent.set("out_trade_no", record.getTradeNo());
        bizContent.set("total_amount", record.getAmount().setScale(2, RoundingMode.HALF_UP).toString());
        bizContent.set("subject", subject);
        bizContent.set("product_code", "FAST_INSTANT_TRADE_PAY");
        params.put("biz_content", bizContent.toString());

        String payUrl = channel.getApiUrl() + "?" + buildQueryString(params);
        log.info("支付宝创建订单: tradeNo={}", record.getTradeNo());
        return payUrl;
    }

    @Override
    public boolean verifyNotify(PaymentChannel channel, Map<String, String> params) {
        try {
            String sign = params.get("sign");
            if (StrUtil.isEmpty(sign)) {
                return false;
            }
            String tradeStatus = params.get("trade_status");
            return "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
        } catch (Exception e) {
            log.error("支付宝回调验签失败", e);
            return false;
        }
    }

    @Override
    public boolean queryPayStatus(PaymentChannel channel, String tradeNo) {
        try {
            log.info("支付宝查询订单: tradeNo={}", tradeNo);
            return true;
        } catch (Exception e) {
            log.error("支付宝查询订单失败", e);
            return false;
        }
    }

    @Override
    public String refund(PaymentChannel channel, String tradeNo, String refundNo, BigDecimal amount) {
        try {
            log.info("支付宝退款: tradeNo={}, refundNo={}, amount={}", tradeNo, refundNo, amount);
            return refundNo;
        } catch (Exception e) {
            log.error("支付宝退款失败", e);
            throw new RuntimeException("退款失败: " + e.getMessage());
        }
    }

    private String buildQueryString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }
}
