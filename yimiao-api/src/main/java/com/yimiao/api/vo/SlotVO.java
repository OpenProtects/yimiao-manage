package com.yimiao.api.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@ApiModel("号源VO")
public class SlotVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("号源ID")
    private Long id;

    @ApiModelProperty("接种点ID")
    private Long siteId;

    @ApiModelProperty("接种点名称")
    private String siteName;

    @ApiModelProperty("疫苗ID")
    private Long vaccineId;

    @ApiModelProperty("疫苗名称")
    private String vaccineName;

    @ApiModelProperty("日期")
    private LocalDate slotDate;

    @ApiModelProperty("开始时间")
    private LocalTime startTime;

    @ApiModelProperty("结束时间")
    private LocalTime endTime;

    @ApiModelProperty("总号源数")
    private Integer totalCount;

    @ApiModelProperty("已预约数")
    private Integer bookedCount;

    @ApiModelProperty("剩余数")
    private Integer remainCount;

    @ApiModelProperty("状态 0可预约 1已满 2已过期")
    private Integer status;
}
