package com.yimiao.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yimiao.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ym_payment_channel")
public class PaymentChannel extends BaseEntity {
    private String channelCode;
    private String channelName;
    private String channelIcon;
    private Integer channelType;
    private String apiUrl;
    private String appId;
    private String appSecret;
    private String merchantId;
    private String merchantPrivateKey;
    private String platformPublicKey;
    private String notifyUrl;
    private String returnUrl;
    private Integer status;
    private Integer sort;
    private String remark;
}
