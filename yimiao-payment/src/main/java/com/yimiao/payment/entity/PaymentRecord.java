package com.yimiao.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yimiao.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ym_payment_record")
public class PaymentRecord extends BaseEntity {
    private Long orderId;
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    private Integer payType;
    private String channelCode;
    private String tradeNo;
    private Integer status;
    private String failReason;
    private LocalDateTime payTime;
    private LocalDateTime notifyTime;
}
