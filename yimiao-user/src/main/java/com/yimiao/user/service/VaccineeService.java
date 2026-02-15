package com.yimiao.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yimiao.api.dto.VaccineeDTO;
import com.yimiao.api.vo.VaccineeVO;
import com.yimiao.user.entity.Vaccinee;

import java.util.List;

public interface VaccineeService extends IService<Vaccinee> {
    List<VaccineeVO> listByUserId(Long userId);
    VaccineeVO getDetail(Long id);
    Long addVaccinee(VaccineeDTO dto);
    void updateVaccinee(VaccineeDTO dto);
    void deleteVaccinee(Long id, Long userId);
    void setDefault(Long id, Long userId);
    Vaccinee getByIdCard(String idCard);
}
