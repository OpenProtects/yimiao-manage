package com.yimiao.admin.controller;

import com.yimiao.admin.service.StatisticsService;
import com.yimiao.common.core.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/statistics")
@Api(tags = "数据统计")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/overview")
    @ApiOperation("获取总览数据")
    public Result<Map<String, Object>> getOverview() {
        return Result.success(statisticsService.getOverview());
    }

    @GetMapping("/daily")
    @ApiOperation("获取每日统计")
    public Result<Map<String, Object>> getDailyStatistics(@RequestParam String date) {
        return Result.success(statisticsService.getDailyStatistics(date));
    }

    @GetMapping("/appointment")
    @ApiOperation("获取预约统计")
    public Result<Map<String, Object>> getAppointmentStatistics(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return Result.success(statisticsService.getAppointmentStatistics(startDate, endDate));
    }

    @GetMapping("/vaccine")
    @ApiOperation("获取疫苗统计")
    public Result<Map<String, Object>> getVaccineStatistics() {
        return Result.success(statisticsService.getVaccineStatistics());
    }

    @GetMapping("/site/{siteId}")
    @ApiOperation("获取接种点统计")
    public Result<Map<String, Object>> getSiteStatistics(@PathVariable Long siteId) {
        return Result.success(statisticsService.getSiteStatistics(siteId));
    }
}
