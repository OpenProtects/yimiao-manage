package com.yimiao.payment.strategy;

import com.yimiao.payment.entity.PaymentChannel;
import com.yimiao.payment.entity.PaymentRecord;

import java.math.BigDecimal;

public interface PaymentStrategy {
    String getChannelCode();
    String createPayment(PaymentChannel channel, PaymentRecord record, String subject);
    boolean verifyNotify(PaymentChannel channel, java.util.Map<String, String> params);
    boolean queryPayStatus(PaymentChannel channel, String tradeNo);
    String refund(PaymentChannel channel, String tradeNo, String refundNo, BigDecimal amount);
}
