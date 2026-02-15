package com.yimiao.admin.service;

import java.util.Map;

public interface StatisticsService {
    Map<String, Object> getOverview();
    Map<String, Object> getDailyStatistics(String date);
    Map<String, Object> getAppointmentStatistics(String startDate, String endDate);
    Map<String, Object> getVaccineStatistics();
    Map<String, Object> getSiteStatistics(Long siteId);
}
