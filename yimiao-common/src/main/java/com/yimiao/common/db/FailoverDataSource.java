package com.yimiao.common.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class FailoverDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();
    
    private static final AtomicInteger counter = new AtomicInteger(0);
    
    private static List<String> availableDataSources;
    private static volatile boolean masterAvailable = true;
    private static volatile long lastCheckTime = 0;
    private static final long CHECK_INTERVAL = 30000L;

    public static void setDataSource(String dataSource) {
        CONTEXT_HOLDER.set(dataSource);
    }

    public static String getDataSource() {
        return CONTEXT_HOLDER.get();
    }

    public static void clearDataSource() {
        CONTEXT_HOLDER.remove();
    }

    public static void useMaster() {
        setDataSource("master");
    }

    public static void useSlave() {
        setDataSource("slave");
    }

    public static void markMasterUnavailable() {
        masterAvailable = false;
        lastCheckTime = System.currentTimeMillis();
        log.warn("主数据库标记为不可用，切换到从数据库");
    }

    public static void markMasterAvailable() {
        masterAvailable = true;
        lastCheckTime = System.currentTimeMillis();
        log.info("主数据库已恢复可用");
    }

    public static boolean isMasterAvailable() {
        return masterAvailable;
    }

    public static boolean shouldCheckMaster() {
        return !masterAvailable && 
               (System.currentTimeMillis() - lastCheckTime) > CHECK_INTERVAL;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String dataSource = getDataSource();
        if (dataSource != null) {
            clearDataSource();
            return dataSource;
        }
        
        if (!masterAvailable) {
            return "slave";
        }
        
        return "master";
    }

    public static void setAvailableDataSources(List<String> dataSources) {
        availableDataSources = dataSources;
    }

    public static List<String> getAvailableDataSources() {
        return availableDataSources;
    }
}
