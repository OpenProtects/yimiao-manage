package com.yimiao.admin.service.impl;

import com.yimiao.admin.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final JdbcTemplate jdbcTemplate;

    public StatisticsServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> result = new HashMap<>();
        
        Integer totalUsers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ym_user WHERE deleted = 0", Integer.class);
        Integer totalVaccines = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ym_vaccine WHERE deleted = 0", Integer.class);
        Integer totalSites = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ym_site WHERE deleted = 0", Integer.class);
        Integer totalAppointments = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ym_appointment WHERE deleted = 0", Integer.class);
        Integer completedAppointments = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ym_appointment WHERE status = 3 AND deleted = 0", Integer.class);
        
        result.put("totalUsers", totalUsers);
        result.put("totalVaccines", totalVaccines);
        result.put("totalSites", totalSites);
        result.put("totalAppointments", totalAppointments);
        result.put("completedAppointments", completedAppointments);
        
        return result;
    }

    @Override
    public Map<String, Object> getDailyStatistics(String date) {
        Map<String, Object> result = new HashMap<>();
        
        Integer appointments = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ym_appointment WHERE DATE(create_time) = ? AND deleted = 0",
                Integer.class, date);
        Integer completed = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ym_appointment WHERE DATE(create_time) = ? AND status = 3 AND deleted = 0",
                Integer.class, date);
        Integer cancelled = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ym_appointment WHERE DATE(create_time) = ? AND status = 2 AND deleted = 0",
                Integer.class, date);
        
        result.put("date", date);
        result.put("appointments", appointments);
        result.put("completed", completed);
        result.put("cancelled", cancelled);
        
        return result;
    }

    @Override
    public Map<String, Object> getAppointmentStatistics(String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Object>> dailyStats = jdbcTemplate.queryForList(
                "SELECT DATE(create_time) as date, COUNT(*) as count, status " +
                "FROM ym_appointment " +
                "WHERE DATE(create_time) BETWEEN ? AND ? AND deleted = 0 " +
                "GROUP BY DATE(create_time), status " +
                "ORDER BY date",
                startDate, endDate);
        
        result.put("dailyStats", dailyStats);
        
        return result;
    }

    @Override
    public Map<String, Object> getVaccineStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Object>> vaccineStats = jdbcTemplate.queryForList(
                "SELECT v.name, v.code, COUNT(a.id) as appointment_count " +
                "FROM ym_vaccine v " +
                "LEFT JOIN ym_appointment a ON v.id = a.vaccine_id AND a.deleted = 0 " +
                "WHERE v.deleted = 0 " +
                "GROUP BY v.id, v.name, v.code " +
                "ORDER BY appointment_count DESC");
        
        result.put("vaccineStats", vaccineStats);
        
        return result;
    }

    @Override
    public Map<String, Object> getSiteStatistics(Long siteId) {
        Map<String, Object> result = new HashMap<>();
        
        Integer totalAppointments = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ym_appointment WHERE site_id = ? AND deleted = 0",
                Integer.class, siteId);
        Integer completedAppointments = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ym_appointment WHERE site_id = ? AND status = 3 AND deleted = 0",
                Integer.class, siteId);
        
        result.put("siteId", siteId);
        result.put("totalAppointments", totalAppointments);
        result.put("completedAppointments", completedAppointments);
        
        return result;
    }
}
