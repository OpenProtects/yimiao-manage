package com.yimiao.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@ApiModel("接种人信息DTO")
public class VaccineeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("接种人ID(更新时需要)")
    private Long id;

    @ApiModelProperty(value = "用户ID", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @ApiModelProperty(value = "真实姓名", required = true)
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @ApiModelProperty(value = "身份证号", required = true)
    @NotBlank(message = "身份证号不能为空")
    private String idCard;

    @ApiModelProperty(value = "性别 1男 2女", required = true)
    @NotNull(message = "性别不能为空")
    private Integer gender;

    @ApiModelProperty(value = "出生日期", required = true)
    @NotNull(message = "出生日期不能为空")
    private LocalDate birthDate;

    @ApiModelProperty("联系电话")
    private String phone;

    @ApiModelProperty("地址")
    private String address;

    @ApiModelProperty("与用户关系")
    private String relation;

    @ApiModelProperty("是否默认接种人")
    private Boolean isDefault;
}
