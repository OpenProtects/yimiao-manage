package com.yimiao.vaccine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yimiao.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ym_slot")
public class Slot extends BaseEntity {
    private Long siteId;
    private Long vaccineId;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer totalCount;
    private Integer bookedCount;
    private Integer remainCount;
    private Integer status;
}
