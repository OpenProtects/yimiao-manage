package com.yimiao.common.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Slf4j
@Component
public class DatabaseHealthChecker {

    private final Map<String, DataSource> dataSources;

    @Value("${db.health-check.timeout:3000}")
    private int timeout;

    public DatabaseHealthChecker(Map<String, DataSource> dataSources) {
        this.dataSources = dataSources;
    }

    @Scheduled(fixedDelay = 30000)
    public void checkHealth() {
        for (Map.Entry<String, DataSource> entry : dataSources.entrySet()) {
            String name = entry.getKey();
            DataSource dataSource = entry.getValue();
            boolean healthy = checkConnection(dataSource);
            
            if ("master".equals(name)) {
                if (healthy && !FailoverDataSource.isMasterAvailable()) {
                    FailoverDataSource.markMasterAvailable();
                    log.info("主数据库已恢复，准备切换回主数据库");
                } else if (!healthy && FailoverDataSource.isMasterAvailable()) {
                    FailoverDataSource.markMasterUnavailable();
                    log.error("主数据库不可用，已切换到从数据库");
                }
            }
            
            log.debug("数据库健康检查: {} - {}", name, healthy ? "正常" : "异常");
        }
    }

    public boolean checkConnection(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(timeout / 1000);
        } catch (SQLException e) {
            log.error("数据库连接检查失败: {}", e.getMessage());
            return false;
        }
    }

    public boolean checkConnection(String dataSourceName) {
        DataSource dataSource = dataSources.get(dataSourceName);
        if (dataSource == null) {
            return false;
        }
        return checkConnection(dataSource);
    }

    public Map<String, Boolean> getAllStatus() {
        Map<String, Boolean> status = new java.util.HashMap<>();
        for (Map.Entry<String, DataSource> entry : dataSources.entrySet()) {
            status.put(entry.getKey(), checkConnection(entry.getValue()));
        }
        return status;
    }
}
