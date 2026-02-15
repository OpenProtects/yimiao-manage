package com.yimiao.payment.service;

public interface NotificationService {
    void sendAppointmentSuccess(Long userId, String orderNo, String vaccineName, String siteName, String date);
    void sendAppointmentCancel(Long userId, String orderNo, String reason);
    void sendVaccinationReminder(Long userId, String orderNo, String vaccineName, String siteName, String date);
    void sendPaymentSuccess(Long userId, String orderNo, String amount);
    void sendRefundSuccess(Long userId, String orderNo, String amount);
}
