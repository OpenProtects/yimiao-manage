package com.yimiao.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yimiao.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ym_vaccinee")
public class Vaccinee extends BaseEntity {
    private Long userId;
    private String realName;
    private String idCard;
    private Integer gender;
    private LocalDate birthDate;
    private String phone;
    private String address;
    private String relation;
    private Boolean isDefault;
    private Integer certStatus;
}
