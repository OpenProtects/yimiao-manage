package com.yimiao.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("疫苗信息DTO")
public class VaccineDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("疫苗ID(更新时需要)")
    private Long id;

    @ApiModelProperty(value = "疫苗名称", required = true)
    @NotNull(message = "疫苗名称不能为空")
    private String name;

    @ApiModelProperty(value = "疫苗编码", required = true)
    @NotNull(message = "疫苗编码不能为空")
    private String code;

    @ApiModelProperty("疫苗类型")
    private String type;

    @ApiModelProperty("生产厂家")
    private String manufacturer;

    @ApiModelProperty("规格")
    private String specification;

    @ApiModelProperty("适用年龄下限")
    private Integer minAge;

    @ApiModelProperty("适用年龄上限")
    private Integer maxAge;

    @ApiModelProperty("接种剂次")
    private Integer doseCount;

    @ApiModelProperty("剂次间隔(天)")
    private Integer doseInterval;

    @ApiModelProperty("价格")
    @Min(value = 0, message = "价格不能为负数")
    private java.math.BigDecimal price;

    @ApiModelProperty("是否免费")
    private Boolean isFree;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("状态 0正常 1禁用")
    private Integer status;
}
