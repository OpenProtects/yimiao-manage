package com.yimiao.vaccine.controller;

import com.yimiao.common.core.Result;
import com.yimiao.vaccine.entity.VaccineStock;
import com.yimiao.vaccine.service.VaccineStockService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/stock")
@Api(tags = "库存管理")
public class VaccineStockController {

    private final VaccineStockService vaccineStockService;

    public VaccineStockController(VaccineStockService vaccineStockService) {
        this.vaccineStockService = vaccineStockService;
    }

    @GetMapping("/available")
    @ApiOperation("获取可用库存")
    public Result<Integer> getAvailableStock(
            @RequestParam Long siteId,
            @RequestParam Long vaccineId) {
        return Result.success(vaccineStockService.getAvailableStock(siteId, vaccineId));
    }

    @PostMapping("/deduct")
    @ApiOperation("扣减库存")
    public Result<Boolean> deductStock(
            @RequestParam Long siteId,
            @RequestParam Long vaccineId,
            @RequestParam(defaultValue = "1") int quantity) {
        return Result.success(vaccineStockService.deductStock(siteId, vaccineId, quantity));
    }

    @PostMapping("/add")
    @ApiOperation("增加库存")
    public Result<Boolean> addStock(
            @RequestParam Long siteId,
            @RequestParam Long vaccineId,
            @RequestParam(defaultValue = "1") int quantity) {
        return Result.success(vaccineStockService.addStock(siteId, vaccineId, quantity));
    }

    @PostMapping("/init-cache")
    @ApiOperation("初始化库存缓存")
    public Result<Void> initCache(
            @RequestParam Long siteId,
            @RequestParam Long vaccineId) {
        vaccineStockService.initStockCache(siteId, vaccineId);
        return Result.success();
    }

    @GetMapping("/list/{siteId}")
    @ApiOperation("获取接种点库存列表")
    public Result<List<VaccineStock>> listBySiteId(@PathVariable Long siteId) {
        return Result.success(vaccineStockService.listBySiteId(siteId));
    }
}
