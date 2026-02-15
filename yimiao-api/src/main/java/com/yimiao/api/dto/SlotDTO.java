package com.yimiao.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@ApiModel("号源DTO")
public class SlotDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("号源ID(更新时需要)")
    private Long id;

    @ApiModelProperty(value = "接种点ID", required = true)
    @NotNull(message = "接种点ID不能为空")
    private Long siteId;

    @ApiModelProperty(value = "疫苗ID", required = true)
    @NotNull(message = "疫苗ID不能为空")
    private Long vaccineId;

    @ApiModelProperty(value = "日期", required = true)
    @NotNull(message = "日期不能为空")
    private LocalDate slotDate;

    @ApiModelProperty(value = "开始时间", required = true)
    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    @ApiModelProperty(value = "结束时间", required = true)
    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    @ApiModelProperty(value = "总号源数", required = true)
    @NotNull(message = "总号源数不能为空")
    private Integer totalCount;

    @ApiModelProperty("已预约数")
    private Integer bookedCount;

    @ApiModelProperty("剩余数")
    private Integer remainCount;

    @ApiModelProperty("状态 0可预约 1已满 2已过期")
    private Integer status;
}
