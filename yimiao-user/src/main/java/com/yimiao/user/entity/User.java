package com.yimiao.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yimiao.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ym_user")
public class User extends BaseEntity {
    private String username;
    private String password;
    private String phone;
    private String email;
    private String avatar;
    private Integer userType;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
}
