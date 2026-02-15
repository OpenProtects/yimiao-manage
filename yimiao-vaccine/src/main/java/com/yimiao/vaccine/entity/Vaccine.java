package com.yimiao.vaccine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yimiao.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ym_vaccine")
public class Vaccine extends BaseEntity {
    private String name;
    private String code;
    private String type;
    private String manufacturer;
    private String specification;
    private Integer minAge;
    private Integer maxAge;
    private Integer doseCount;
    private Integer doseInterval;
    private BigDecimal price;
    private Boolean isFree;
    private String description;
    private Integer status;
}
