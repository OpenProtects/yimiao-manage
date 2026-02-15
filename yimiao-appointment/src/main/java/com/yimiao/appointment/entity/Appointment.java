package com.yimiao.appointment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yimiao.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ym_appointment")
public class Appointment extends BaseEntity {
    private String orderNo;
    private Long userId;
    private Long vaccineeId;
    private Long vaccineId;
    private Long siteId;
    private Long slotId;
    private Integer doseNo;
    private Integer status;
    private Integer payStatus;
    private BigDecimal amount;
    private LocalDateTime payTime;
    private Integer payType;
    private String payTradeNo;
    private Integer verifyStatus;
    private LocalDateTime verifyTime;
    private Long verifyUserId;
    private String cancelReason;
    private LocalDateTime cancelTime;
    private String remark;
}
