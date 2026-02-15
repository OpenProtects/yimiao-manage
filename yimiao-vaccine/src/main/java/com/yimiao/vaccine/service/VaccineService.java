package com.yimiao.vaccine.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yimiao.api.dto.VaccineDTO;
import com.yimiao.api.vo.VaccineVO;
import com.yimiao.vaccine.entity.Vaccine;

import java.util.List;

public interface VaccineService extends IService<Vaccine> {
    Page<VaccineVO> pageList(int pageNum, int pageSize, String name, String type, Integer status);
    VaccineVO getDetail(Long id);
    Long addVaccine(VaccineDTO dto);
    void updateVaccine(VaccineDTO dto);
    void deleteVaccine(Long id);
    List<VaccineVO> listAvailable();
    boolean checkAgeRange(Long vaccineId, Integer age);
}
