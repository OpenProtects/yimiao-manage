package com.yimiao.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yimiao.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ym_admin")
public class Admin extends BaseEntity {
    private Long userId;
    private String role;
    private String permissions;
    private Long siteId;
}
