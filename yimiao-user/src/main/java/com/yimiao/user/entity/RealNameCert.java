package com.yimiao.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yimiao.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ym_real_name_cert")
public class RealNameCert extends BaseEntity {
    private Long userId;
    private String realName;
    private String idCard;
    private Integer status;
    private String failReason;
    private LocalDateTime certTime;
}
