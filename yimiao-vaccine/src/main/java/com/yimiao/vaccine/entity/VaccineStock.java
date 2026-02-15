package com.yimiao.vaccine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yimiao.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ym_vaccine_stock")
public class VaccineStock extends BaseEntity {
    private Long siteId;
    private Long vaccineId;
    private String batchNo;
    private Integer totalCount;
    private Integer usedCount;
    private Integer remainCount;
    private LocalDate expireDate;
    private Integer status;
}
