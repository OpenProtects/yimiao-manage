package com.yimiao.vaccine.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yimiao.api.dto.SlotDTO;
import com.yimiao.api.vo.SlotVO;
import com.yimiao.vaccine.entity.Slot;

import java.time.LocalDate;
import java.util.List;

public interface SlotService extends IService<Slot> {
    Page<SlotVO> pageList(int pageNum, int pageSize, Long siteId, Long vaccineId, LocalDate startDate, LocalDate endDate);
    List<SlotVO> listAvailable(Long siteId, Long vaccineId, LocalDate date);
    SlotVO getDetail(Long id);
    Long addSlot(SlotDTO dto);
    void updateSlot(SlotDTO dto);
    void deleteSlot(Long id);
    boolean bookSlot(Long slotId);
    boolean cancelSlot(Long slotId);
    boolean tryBookSlot(Long slotId, String requestId);
    void releaseSlotLock(Long slotId, String requestId);
    void generateSlots(Long siteId, Long vaccineId, LocalDate startDate, LocalDate endDate, int dailyCount);
}
