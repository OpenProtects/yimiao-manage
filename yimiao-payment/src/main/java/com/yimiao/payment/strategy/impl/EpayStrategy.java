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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

@Slf4j
@Component
public class EpayStrategy implements PaymentStrategy {

    private static final String CHANNEL_CODE = "epay";

    @Override
    public String getChannelCode() {
        return CHANNEL_CODE;
    }

    @Override
    public String createPayment(PaymentChannel channel, PaymentRecord record, String subject) {
        Map<String, String> params = new TreeMap<>();
        params.put("pid", channel.getMerchantId());
        params.put("type", convertPayType(record.getPayType()));
        params.put("out_trade_no", record.getTradeNo());
        params.put("notify_url", channel.getNotifyUrl());
        params.put("return_url", channel.getReturnUrl());
        params.put("name", subject);
        params.put("money", record.getAmount().setScale(2, RoundingMode.HALF_UP).toString());
        params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        String sign = generateSign(params, channel.getMerchantPrivateKey());
        params.put("sign", sign);
        params.put("sign_type", "RSA");

        String payUrl = channel.getApiUrl() + "api/pay/submit?" + buildQueryString(params);
        log.info("易支付创建订单: tradeNo={}, payUrl={}", record.getTradeNo(), payUrl);
        return payUrl;
    }

    @Override
    public boolean verifyNotify(PaymentChannel channel, Map<String, String> params) {
        try {
            String sign = params.get("sign");
            if (StrUtil.isEmpty(sign)) {
                log.error("易支付回调签名不存在");
                return false;
            }

            String timestamp = params.get("timestamp");
            if (StrUtil.isEmpty(timestamp)) {
                log.error("易支付回调时间戳不存在");
                return false;
            }

            long now = System.currentTimeMillis() / 1000;
            long notifyTime = Long.parseLong(timestamp);
            if (Math.abs(now - notifyTime) > 300) {
                log.error("易支付回调时间戳过期");
                return false;
            }

            Map<String, String> signParams = new TreeMap<>(params);
            signParams.remove("sign");
            signParams.remove("sign_type");

            String signContent = buildSignContent(signParams);
            return verifySign(signContent, sign, channel.getPlatformPublicKey());
        } catch (Exception e) {
            log.error("易支付回调验签失败", e);
            return false;
        }
    }

    @Override
    public boolean queryPayStatus(PaymentChannel channel, String tradeNo) {
        try {
            Map<String, Object> params = new TreeMap<>();
            params.put("trade_no", tradeNo);
            params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

            String sign = generateSignForQuery(params, channel.getMerchantPrivateKey());
            params.put("sign", sign);
            params.put("sign_type", "RSA");

            String url = channel.getApiUrl() + "api/pay/query";
            String response = HttpUtil.post(url, params);
            log.info("易支付查询订单: tradeNo={}, response={}", tradeNo, response);

            JSONObject json = JSONUtil.parseObj(response);
            if (json.getInt("code") == 0 && json.getInt("status") == 1) {
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("易支付查询订单失败", e);
            return false;
        }
    }

    @Override
    public String refund(PaymentChannel channel, String tradeNo, String refundNo, BigDecimal amount) {
        try {
            Map<String, Object> params = new TreeMap<>();
            params.put("trade_no", tradeNo);
            params.put("out_refund_no", refundNo);
            params.put("money", amount.setScale(2, RoundingMode.HALF_UP).toString());
            params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

            String sign = generateSignForQuery(params, channel.getMerchantPrivateKey());
            params.put("sign", sign);
            params.put("sign_type", "RSA");

            String url = channel.getApiUrl() + "api/pay/refund";
            String response = HttpUtil.post(url, params);
            log.info("易支付退款: tradeNo={}, refundNo={}, response={}", tradeNo, refundNo, response);

            JSONObject json = JSONUtil.parseObj(response);
            if (json.getInt("code") == 0) {
                return json.getStr("refund_no");
            }
            throw new RuntimeException("退款失败: " + json.getStr("msg"));
        } catch (Exception e) {
            log.error("易支付退款失败", e);
            throw new RuntimeException("退款失败: " + e.getMessage());
        }
    }

    private String convertPayType(Integer payType) {
        switch (payType) {
            case 1: return "alipay";
            case 2: return "wxpay";
            case 3: return "qqpay";
            case 4: return "jdpay";
            default: return "alipay";
        }
    }

    private String generateSign(Map<String, String> params, String privateKeyStr) {
        try {
            String signContent = buildSignContent(params);
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(signContent.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            log.error("易支付签名失败", e);
            throw new RuntimeException("签名失败: " + e.getMessage());
        }
    }

    private String generateSignForQuery(Map<String, Object> params, String privateKeyStr) {
        try {
            String signContent = buildSignContentForObject(params);
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(signContent.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            log.error("易支付签名失败", e);
            throw new RuntimeException("签名失败: " + e.getMessage());
        }
    }

    private boolean verifySign(String content, String sign, String publicKeyStr) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.getDecoder().decode(sign));
        } catch (Exception e) {
            log.error("易支付验签失败", e);
            return false;
        }
    }

    private String buildSignContent(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (StrUtil.isNotEmpty(entry.getValue())) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        return sb.toString();
    }

    private String buildSignContentForObject(Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() != null && StrUtil.isNotEmpty(entry.getValue().toString())) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        return sb.toString();
    }

    private String buildQueryString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            try {
                sb.append(entry.getKey()).append("=")
                        .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            } catch (Exception e) {
                sb.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        return sb.toString();
    }
}
