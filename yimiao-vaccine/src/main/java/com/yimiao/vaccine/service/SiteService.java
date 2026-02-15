package com.yimiao.vaccine.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yimiao.api.dto.SiteDTO;
import com.yimiao.api.vo.SiteVO;
import com.yimiao.vaccine.entity.Site;

import java.util.List;

public interface SiteService extends IService<Site> {
    Page<SiteVO> pageList(int pageNum, int pageSize, String name, String region, Integer status);
    SiteVO getDetail(Long id);
    Long addSite(SiteDTO dto);
    void updateSite(SiteDTO dto);
    void deleteSite(Long id);
    List<SiteVO> listAll();
    List<SiteVO> listByRegion(String region);
}
