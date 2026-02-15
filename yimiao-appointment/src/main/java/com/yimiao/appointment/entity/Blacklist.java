package com.yimiao.appointment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yimiao.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ym_blacklist")
public class Blacklist extends BaseEntity {
    private String idCard;
    private String realName;
    private String reason;
    private Integer type;
    private LocalDateTime expireTime;
    private Integer status;
    private Long operatorId;
}
