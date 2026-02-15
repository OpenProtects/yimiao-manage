package com.yimiao.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yimiao.api.dto.PaymentDTO;
import com.yimiao.payment.entity.PaymentRecord;

import java.math.BigDecimal;

public interface PaymentService extends IService<PaymentRecord> {
    String createPayment(PaymentDTO dto);
    void handlePayNotify(String tradeNo, String orderNo, boolean success);
    boolean queryPayStatus(String orderNo);
    void closePayment(String orderNo);
    PaymentRecord getByOrderNo(String orderNo);
}
