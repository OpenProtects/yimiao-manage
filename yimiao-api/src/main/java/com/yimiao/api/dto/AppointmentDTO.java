package com.yimiao.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("预约请求DTO")
public class AppointmentDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @ApiModelProperty(value = "接种人ID", required = true)
    @NotNull(message = "接种人ID不能为空")
    private Long vaccineeId;

    @ApiModelProperty(value = "疫苗ID", required = true)
    @NotNull(message = "疫苗ID不能为空")
    private Long vaccineId;

    @ApiModelProperty(value = "号源ID", required = true)
    @NotNull(message = "号源ID不能为空")
    private Long slotId;

    @ApiModelProperty(value = "接种点ID", required = true)
    @NotNull(message = "接种点ID不能为空")
    private Long siteId;

    @ApiModelProperty(value = "剂次", required = true)
    @NotNull(message = "剂次不能为空")
    private Integer doseNo;
}
