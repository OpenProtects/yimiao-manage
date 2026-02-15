package com.yimiao.api.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@ApiModel("接种人信息VO")
public class VaccineeVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("接种人ID")
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("真实姓名")
    private String realName;

    @ApiModelProperty("身份证号(脱敏)")
    private String idCard;

    @ApiModelProperty("性别 1男 2女")
    private Integer gender;

    @ApiModelProperty("出生日期")
    private LocalDate birthDate;

    @ApiModelProperty("年龄")
    private Integer age;

    @ApiModelProperty("联系电话")
    private String phone;

    @ApiModelProperty("地址")
    private String address;

    @ApiModelProperty("与用户关系")
    private String relation;

    @ApiModelProperty("是否默认接种人")
    private Boolean isDefault;

    @ApiModelProperty("认证状态 0待认证 1已认证 2认证失败")
    private Integer certStatus;
}
