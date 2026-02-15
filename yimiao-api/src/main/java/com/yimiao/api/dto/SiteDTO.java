package com.yimiao.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("接种点DTO")
public class SiteDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("接种点ID(更新时需要)")
    private Long id;

    @ApiModelProperty(value = "接种点名称", required = true)
    @NotNull(message = "接种点名称不能为空")
    private String name;

    @ApiModelProperty(value = "接种点编码", required = true)
    @NotNull(message = "接种点编码不能为空")
    private String code;

    @ApiModelProperty("所属区域")
    private String region;

    @ApiModelProperty("详细地址")
    private String address;

    @ApiModelProperty("联系电话")
    private String phone;

    @ApiModelProperty("经度")
    private Double longitude;

    @ApiModelProperty("纬度")
    private Double latitude;

    @ApiModelProperty("营业时间")
    private String businessHours;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("状态 0正常 1禁用")
    private Integer status;
}
