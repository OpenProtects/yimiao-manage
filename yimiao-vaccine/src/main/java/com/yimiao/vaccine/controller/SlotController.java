package com.yimiao.vaccine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yimiao.api.dto.SlotDTO;
import com.yimiao.api.vo.SlotVO;
import com.yimiao.common.core.Result;
import com.yimiao.vaccine.service.SlotService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/slot")
@Api(tags = "号源管理")
public class SlotController {

    private final SlotService slotService;

    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    @GetMapping("/page")
    @ApiOperation("分页查询号源列表")
    public Result<Page<SlotVO>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) Long vaccineId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(slotService.pageList(pageNum, pageSize, siteId, vaccineId, startDate, endDate));
    }

    @GetMapping("/available")
    @ApiOperation("获取可用号源列表")
    public Result<List<SlotVO>> listAvailable(
            @RequestParam Long siteId,
            @RequestParam Long vaccineId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(slotService.listAvailable(siteId, vaccineId, date));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取号源详情")
    public Result<SlotVO> getDetail(@PathVariable Long id) {
        return Result.success(slotService.getDetail(id));
    }

    @PostMapping
    @ApiOperation("添加号源")
    public Result<Long> add(@Valid @RequestBody SlotDTO dto) {
        return Result.success(slotService.addSlot(dto));
    }

    @PutMapping
    @ApiOperation("更新号源")
    public Result<Void> update(@Valid @RequestBody SlotDTO dto) {
        slotService.updateSlot(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除号源")
    public Result<Void> delete(@PathVariable Long id) {
        slotService.deleteSlot(id);
        return Result.success();
    }

    @PostMapping("/book/{id}")
    @ApiOperation("预约号源")
    public Result<Boolean> book(@PathVariable Long id) {
        return Result.success(slotService.bookSlot(id));
    }

    @PostMapping("/cancel/{id}")
    @ApiOperation("取消预约号源")
    public Result<Boolean> cancel(@PathVariable Long id) {
        return Result.success(slotService.cancelSlot(id));
    }

    @PostMapping("/generate")
    @ApiOperation("批量生成号源")
    public Result<Void> generate(
            @RequestParam Long siteId,
            @RequestParam Long vaccineId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "100") int dailyCount) {
        slotService.generateSlots(siteId, vaccineId, startDate, endDate, dailyCount);
        return Result.success();
    }
}
