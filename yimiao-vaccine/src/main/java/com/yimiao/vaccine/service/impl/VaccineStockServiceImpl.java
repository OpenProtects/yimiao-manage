package com.yimiao.vaccine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yimiao.common.constant.RedisKeyConstant;
import com.yimiao.common.redis.StockCache;
import com.yimiao.vaccine.entity.VaccineStock;
import com.yimiao.vaccine.mapper.VaccineStockMapper;
import com.yimiao.vaccine.service.VaccineStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class VaccineStockServiceImpl extends ServiceImpl<VaccineStockMapper, VaccineStock> implements VaccineStockService {

    private final StockCache stockCache;

    public VaccineStockServiceImpl(StockCache stockCache) {
        this.stockCache = stockCache;
    }

    @Override
    public Integer getAvailableStock(Long siteId, Long vaccineId) {
        String cacheKey = buildStockKey(siteId, vaccineId);
        
        Integer cachedStock = stockCache.getStock(cacheKey);
        if (cachedStock != null) {
            return cachedStock;
        }
        
        VaccineStock stock = getBySiteAndVaccine(siteId, vaccineId);
        if (stock == null) {
            return 0;
        }
        
        stockCache.setStock(cacheKey, stock.getRemainCount(), 30, TimeUnit.MINUTES);
        return stock.getRemainCount();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductStock(Long siteId, Long vaccineId, int quantity) {
        String cacheKey = buildStockKey(siteId, vaccineId);
        
        StockCache.StockResult result = stockCache.deductStock(cacheKey, quantity);
        if (!result.isSuccess()) {
            log.warn("库存扣减失败: siteId={}, vaccineId={}, quantity={}, reason={}", 
                    siteId, vaccineId, quantity, result.getMessage());
            return false;
        }
        
        try {
            VaccineStock stock = getBySiteAndVaccine(siteId, vaccineId);
            if (stock == null || stock.getRemainCount() < quantity) {
                stockCache.addStock(cacheKey, quantity);
                return false;
            }
            
            stock.setUsedCount(stock.getUsedCount() + quantity);
            stock.setRemainCount(stock.getRemainCount() - quantity);
            updateById(stock);
            
            log.info("库存扣减成功: siteId={}, vaccineId={}, quantity={}, remain={}", 
                    siteId, vaccineId, quantity, stock.getRemainCount());
            return true;
        } catch (Exception e) {
            stockCache.addStock(cacheKey, quantity);
            log.error("库存扣减异常: siteId={}, vaccineId={}", siteId, vaccineId, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addStock(Long siteId, Long vaccineId, int quantity) {
        String cacheKey = buildStockKey(siteId, vaccineId);
        
        VaccineStock stock = getBySiteAndVaccine(siteId, vaccineId);
        if (stock == null) {
            return false;
        }
        
        stock.setUsedCount(Math.max(0, stock.getUsedCount() - quantity));
        stock.setRemainCount(stock.getRemainCount() + quantity);
        updateById(stock);
        
        stockCache.addStock(cacheKey, quantity);
        
        log.info("库存回滚成功: siteId={}, vaccineId={}, quantity={}", siteId, vaccineId, quantity);
        return true;
    }

    @Override
    public void initStockCache(Long siteId, Long vaccineId) {
        VaccineStock stock = getBySiteAndVaccine(siteId, vaccineId);
        if (stock != null) {
            String cacheKey = buildStockKey(siteId, vaccineId);
            stockCache.setStock(cacheKey, stock.getRemainCount(), 30, TimeUnit.MINUTES);
            log.info("初始化库存缓存: siteId={}, vaccineId={}, stock={}", siteId, vaccineId, stock.getRemainCount());
        }
    }

    @Override
    public List<VaccineStock> listBySiteId(Long siteId) {
        return list(new LambdaQueryWrapper<VaccineStock>()
                .eq(VaccineStock::getSiteId, siteId)
                .eq(VaccineStock::getDeleted, 0));
    }

    @Override
    public VaccineStock getBySiteAndVaccine(Long siteId, Long vaccineId) {
        return getOne(new LambdaQueryWrapper<VaccineStock>()
                .eq(VaccineStock::getSiteId, siteId)
                .eq(VaccineStock::getVaccineId, vaccineId)
                .eq(VaccineStock::getDeleted, 0)
                .orderByAsc(VaccineStock::getExpireDate)
                .last("LIMIT 1"));
    }

    private String buildStockKey(Long siteId, Long vaccineId) {
        return siteId + ":" + vaccineId;
    }
}
