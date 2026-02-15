package com.yimiao.appointment.service;

public interface RiskControlService {
    boolean checkAppointmentQualification(Long userId, Long vaccineeId, Long vaccineId, Integer doseNo);
    String getQueuePosition(Long userId);
    boolean tryAcquireRateLimit(Long userId, String operation);
    void releaseRateLimit(Long userId, String operation);
    String checkInterval(Long vaccineeId, Long vaccineId, Integer doseNo);
}
