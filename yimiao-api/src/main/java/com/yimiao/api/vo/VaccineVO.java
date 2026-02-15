package com.yimiao.api.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel("疫苗详情VO")
public class VaccineVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("疫苗ID")
    private Long id;

    @ApiModelProperty("疫苗名称")
    private String name;

    @ApiModelProperty("疫苗编码")
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
    private BigDecimal price;

    @ApiModelProperty("是否免费")
    private Boolean isFree;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("状态 0正常 1禁用")
    private Integer status;

    @ApiModelProperty("总库存")
    private Integer totalStock;

    @ApiModelProperty("可用库存")
    private Integer availableStock;
}
