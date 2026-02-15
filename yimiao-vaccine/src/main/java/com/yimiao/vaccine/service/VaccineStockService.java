package com.yimiao.vaccine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yimiao.vaccine.entity.VaccineStock;

import java.util.List;

public interface VaccineStockService extends IService<VaccineStock> {
    Integer getAvailableStock(Long siteId, Long vaccineId);
    boolean deductStock(Long siteId, Long vaccineId, int quantity);
    boolean addStock(Long siteId, Long vaccineId, int quantity);
    void initStockCache(Long siteId, Long vaccineId);
    List<VaccineStock> listBySiteId(Long siteId);
    VaccineStock getBySiteAndVaccine(Long siteId, Long vaccineId);
}
