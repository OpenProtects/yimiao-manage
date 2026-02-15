package com.yimiao.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yimiao.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ym_refund_record")
public class RefundRecord extends BaseEntity {
    private Long orderId;
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    private String reason;
    private String refundNo;
    private Integer status;
    private String failReason;
    private LocalDateTime refundTime;
}
