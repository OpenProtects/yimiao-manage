package com.yimiao.api.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@ApiModel("预约订单VO")
public class AppointmentVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("订单ID")
    private Long id;

    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("接种人ID")
    private Long vaccineeId;

    @ApiModelProperty("接种人姓名")
    private String vaccineeName;

    @ApiModelProperty("接种人身份证")
    private String vaccineeIdCard;

    @ApiModelProperty("疫苗ID")
    private Long vaccineId;

    @ApiModelProperty("疫苗名称")
    private String vaccineName;

    @ApiModelProperty("接种点ID")
    private Long siteId;

    @ApiModelProperty("接种点名称")
    private String siteName;

    @ApiModelProperty("接种点地址")
    private String siteAddress;

    @ApiModelProperty("号源ID")
    private Long slotId;

    @ApiModelProperty("预约日期")
    private LocalDate appointmentDate;

    @ApiModelProperty("预约时间段开始")
    private LocalTime startTime;

    @ApiModelProperty("预约时间段结束")
    private LocalTime endTime;

    @ApiModelProperty("剂次")
    private Integer doseNo;

    @ApiModelProperty("订单状态 0待支付 1已支付 2已取消 3已完成 4已过期 5已退款")
    private Integer status;

    @ApiModelProperty("支付状态 0未支付 1已支付 2已退款")
    private Integer payStatus;

    @ApiModelProperty("支付金额")
    private BigDecimal amount;

    @ApiModelProperty("支付时间")
    private LocalDateTime payTime;

    @ApiModelProperty("核销状态 0未核销 1已核销")
    private Integer verifyStatus;

    @ApiModelProperty("核销时间")
    private LocalDateTime verifyTime;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("取消原因")
    private String cancelReason;

    @ApiModelProperty("备注")
    private String remark;
}
