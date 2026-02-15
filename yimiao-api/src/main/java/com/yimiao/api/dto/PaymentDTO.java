package com.yimiao.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("支付请求DTO")
public class PaymentDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单ID", required = true)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @ApiModelProperty(value = "支付金额", required = true)
    @NotNull(message = "支付金额不能为空")
    private BigDecimal amount;

    @ApiModelProperty(value = "支付方式 1微信 2支付宝 3易支付", required = true)
    @NotNull(message = "支付方式不能为空")
    private Integer payType;

    @ApiModelProperty(value = "支付渠道代码 alipay/wechat/epay", required = true)
    @NotNull(message = "支付渠道不能为空")
    private String channelCode;

    @ApiModelProperty("用户ID")
    private Long userId;
}
