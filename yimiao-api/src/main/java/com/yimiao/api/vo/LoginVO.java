package com.yimiao.api.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("登录响应VO")
public class LoginVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("访问令牌")
    private String token;

    @ApiModelProperty("令牌类型")
    private String tokenType;

    @ApiModelProperty("过期时间(毫秒)")
    private Long expiresIn;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("用户类型 0普通用户 1管理员")
    private Integer userType;

    @ApiModelProperty("是否实名认证")
    private Boolean certified;
}
