package com.yimiao.vaccine.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yimiao.api.dto.SlotDTO;
import com.yimiao.api.vo.SlotVO;
import com.yimiao.common.constant.RedisKeyConstant;
import com.yimiao.common.core.ResultCode;
import com.yimiao.common.exception.BusinessException;
import com.yimiao.common.redis.RedisLock;
import com.yimiao.common.redis.RedisService;
import com.yimiao.vaccine.entity.Site;
import com.yimiao.vaccine.entity.Slot;
import com.yimiao.vaccine.entity.Vaccine;
import com.yimiao.vaccine.mapper.SlotMapper;
import com.yimiao.vaccine.service.SiteService;
import com.yimiao.vaccine.service.SlotService;
import com.yimiao.vaccine.service.VaccineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SlotServiceImpl extends ServiceImpl<SlotMapper, Slot> implements SlotService {

    private final RedisService redisService;
    private final RedisLock redisLock;
    private final SiteService siteService;
    private final VaccineService vaccineService;

    public SlotServiceImpl(RedisService redisService, RedisLock redisLock, 
                          SiteService siteService, VaccineService vaccineService) {
        this.redisService = redisService;
        this.redisLock = redisLock;
        this.siteService = siteService;
        this.vaccineService = vaccineService;
    }

    @Override
    public Page<SlotVO> pageList(int pageNum, int pageSize, Long siteId, Long vaccineId, 
                                  LocalDate startDate, LocalDate endDate) {
        Page<Slot> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Slot> wrapper = new LambdaQueryWrapper<>();
        
        if (siteId != null) {
            wrapper.eq(Slot::getSiteId, siteId);
        }
        if (vaccineId != null) {
            wrapper.eq(Slot::getVaccineId, vaccineId);
        }
        if (startDate != null) {
            wrapper.ge(Slot::getSlotDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Slot::getSlotDate, endDate);
        }
        wrapper.eq(Slot::getDeleted, 0);
        wrapper.orderByAsc(Slot::getSlotDate, Slot::getStartTime);
        
        Page<Slot> result = page(page, wrapper);
        
        Page<SlotVO> voPage = new Page<>();
        voPage.setCurrent(result.getCurrent());
        voPage.setSize(result.getSize());
        voPage.setTotal(result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        
        return voPage;
    }

    @Override
    public List<SlotVO> listAvailable(Long siteId, Long vaccineId, LocalDate date) {
        LambdaQueryWrapper<Slot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Slot::getSiteId, siteId)
               .eq(Slot::getVaccineId, vaccineId)
               .eq(Slot::getSlotDate, date)
               .eq(Slot::getStatus, 0)
               .gt(Slot::getRemainCount, 0)
               .eq(Slot::getDeleted, 0)
               .orderByAsc(Slot::getStartTime);
        
        return list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public SlotVO getDetail(Long id) {
        Slot slot = getById(id);
        if (slot == null || slot.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "号源不存在");
        }
        return convertToVO(slot);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addSlot(SlotDTO dto) {
        Slot slot = new Slot();
        BeanUtil.copyProperties(dto, slot);
        slot.setBookedCount(0);
        slot.setRemainCount(dto.getTotalCount());
        slot.setStatus(0);
        
        save(slot);
        
        String cacheKey = RedisKeyConstant.VACCINE_SLOT + slot.getId();
        redisService.set(cacheKey, String.valueOf(slot.getRemainCount()), 24, TimeUnit.HOURS);
        
        log.info("添加号源成功: id={}, siteId={}, vaccineId={}, date={}", 
                slot.getId(), dto.getSiteId(), dto.getVaccineId(), dto.getSlotDate());
        return slot.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSlot(SlotDTO dto) {
        Slot slot = getById(dto.getId());
        if (slot == null || slot.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "号源不存在");
        }
        
        int bookedCount = slot.getBookedCount();
        int newTotalCount = dto.getTotalCount();
        int newRemainCount = newTotalCount - bookedCount;
        
        if (newRemainCount < 0) {
            throw new BusinessException("总号源数不能小于已预约数");
        }
        
        BeanUtil.copyProperties(dto, slot);
        slot.setRemainCount(newRemainCount);
        if (newRemainCount == 0) {
            slot.setStatus(1);
        }
        
        updateById(slot);
        
        String cacheKey = RedisKeyConstant.VACCINE_SLOT + slot.getId();
        redisService.set(cacheKey, String.valueOf(newRemainCount), 24, TimeUnit.HOURS);
        
        log.info("更新号源成功: id={}", dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSlot(Long id) {
        Slot slot = getById(id);
        if (slot == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "号源不存在");
        }
        
        if (slot.getBookedCount() > 0) {
            throw new BusinessException("存在已预约记录，无法删除");
        }
        
        slot.setDeleted(1);
        updateById(slot);
        
        String cacheKey = RedisKeyConstant.VACCINE_SLOT + id;
        redisService.delete(cacheKey);
        
        log.info("删除号源成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean bookSlot(Long slotId) {
        String lockKey = RedisKeyConstant.LOCK_SLOT + slotId;
        String lockValue = IdUtil.fastSimpleUUID();
        
        if (!redisLock.tryLock(lockKey, lockValue, 10, TimeUnit.SECONDS)) {
            log.warn("获取号源锁失败: slotId={}", slotId);
            return false;
        }
        
        try {
            Slot slot = getById(slotId);
            if (slot == null || slot.getDeleted() == 1) {
                return false;
            }
            
            if (slot.getRemainCount() <= 0) {
                return false;
            }
            
            slot.setBookedCount(slot.getBookedCount() + 1);
            slot.setRemainCount(slot.getRemainCount() - 1);
            if (slot.getRemainCount() == 0) {
                slot.setStatus(1);
            }
            
            updateById(slot);
            
            String cacheKey = RedisKeyConstant.VACCINE_SLOT + slotId;
            redisService.decrement(cacheKey);
            
            log.info("预约号源成功: slotId={}, remain={}", slotId, slot.getRemainCount());
            return true;
        } finally {
            redisLock.unlock(lockKey, lockValue);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelSlot(Long slotId) {
        String lockKey = RedisKeyConstant.LOCK_SLOT + slotId;
        String lockValue = IdUtil.fastSimpleUUID();
        
        if (!redisLock.tryLock(lockKey, lockValue, 10, TimeUnit.SECONDS)) {
            return false;
        }
        
        try {
            Slot slot = getById(slotId);
            if (slot == null) {
                return false;
            }
            
            slot.setBookedCount(Math.max(0, slot.getBookedCount() - 1));
            slot.setRemainCount(slot.getRemainCount() + 1);
            if (slot.getStatus() == 1 && slot.getRemainCount() > 0) {
                slot.setStatus(0);
            }
            
            updateById(slot);
            
            String cacheKey = RedisKeyConstant.VACCINE_SLOT + slotId;
            redisService.increment(cacheKey);
            
            log.info("取消预约号源成功: slotId={}, remain={}", slotId, slot.getRemainCount());
            return true;
        } finally {
            redisLock.unlock(lockKey, lockValue);
        }
    }

    @Override
    public boolean tryBookSlot(Long slotId, String requestId) {
        String lockKey = RedisKeyConstant.VACCINE_SLOT_LOCK + slotId;
        String cacheKey = RedisKeyConstant.VACCINE_SLOT + slotId;
        
        if (!redisLock.tryLock(lockKey, requestId, 10, TimeUnit.SECONDS, 3000)) {
            return false;
        }
        
        try {
            String remainStr = redisService.get(cacheKey);
            int remain = remainStr != null ? Integer.parseInt(remainStr) : 0;
            
            if (remain <= 0) {
                return false;
            }
            
            redisService.decrement(cacheKey);
            return true;
        } catch (Exception e) {
            log.error("尝试预约号源异常: slotId={}", slotId, e);
            return false;
        }
    }

    @Override
    public void releaseSlotLock(Long slotId, String requestId) {
        String lockKey = RedisKeyConstant.VACCINE_SLOT_LOCK + slotId;
        String cacheKey = RedisKeyConstant.VACCINE_SLOT + slotId;
        
        redisLock.unlock(lockKey, requestId);
        redisService.increment(cacheKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateSlots(Long siteId, Long vaccineId, LocalDate startDate, LocalDate endDate, int dailyCount) {
        LocalDate current = startDate;
        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(12, 0);
        
        while (!current.isAfter(endDate)) {
            Slot slot = new Slot();
            slot.setSiteId(siteId);
            slot.setVaccineId(vaccineId);
            slot.setSlotDate(current);
            slot.setStartTime(startTime);
            slot.setEndTime(endTime);
            slot.setTotalCount(dailyCount);
            slot.setBookedCount(0);
            slot.setRemainCount(dailyCount);
            slot.setStatus(0);
            
            save(slot);
            
            current = current.plusDays(1);
        }
        
        log.info("批量生成号源成功: siteId={}, vaccineId={}, startDate={}, endDate={}", 
                siteId, vaccineId, startDate, endDate);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateExpiredSlots() {
        LocalDate today = LocalDate.now();
        
        update(new LambdaUpdateWrapper<Slot>()
                .lt(Slot::getSlotDate, today)
                .eq(Slot::getStatus, 0)
                .set(Slot::getStatus, 2));
        
        log.info("更新过期号源完成");
    }

    private SlotVO convertToVO(Slot slot) {
        SlotVO vo = new SlotVO();
        BeanUtil.copyProperties(slot, vo);
        
        Site site = siteService.getById(slot.getSiteId());
        if (site != null) {
            vo.setSiteName(site.getName());
        }
        
        Vaccine vaccine = vaccineService.getById(slot.getVaccineId());
        if (vaccine != null) {
            vo.setVaccineName(vaccine.getName());
        }
        
        return vo;
    }
}
