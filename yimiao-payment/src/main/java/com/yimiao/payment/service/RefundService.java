package com.yimiao.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yimiao.payment.entity.RefundRecord;

import java.math.BigDecimal;

public interface RefundService extends IService<RefundRecord> {
    String createRefund(Long orderId, String orderNo, Long userId, BigDecimal amount, String reason);
    void handleRefundNotify(String refundNo, boolean success);
    RefundRecord getByOrderNo(String orderNo);
}
