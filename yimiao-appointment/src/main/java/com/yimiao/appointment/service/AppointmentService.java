package com.yimiao.appointment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yimiao.api.dto.AppointmentDTO;
import com.yimiao.api.vo.AppointmentVO;
import com.yimiao.appointment.entity.Appointment;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService extends IService<Appointment> {
    Long createAppointment(AppointmentDTO dto);
    void cancelAppointment(Long id, Long userId, String reason);
    AppointmentVO getDetail(Long id);
    Page<AppointmentVO> pageList(int pageNum, int pageSize, Long userId, Integer status);
    List<AppointmentVO> listByUserId(Long userId);
    void verifyAppointment(Long id, Long operatorId);
    void expireAppointments();
    Appointment getByOrderNo(String orderNo);
    void updatePayStatus(Long id, Integer payStatus, String tradeNo);
}
