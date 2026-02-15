package com.yimiao.vaccine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yimiao.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ym_site")
public class Site extends BaseEntity {
    private String name;
    private String code;
    private String region;
    private String address;
    private String phone;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String businessHours;
    private String description;
    private Integer status;
}
