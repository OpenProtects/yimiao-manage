package com.yimiao.common.db;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Order(-1)
@Component
public class DataSourceAspect {

    @Around("@annotation(readOnly)")
    public Object around(ProceedingJoinPoint point, ReadOnly readOnly) throws Throwable {
        try {
            FailoverDataSource.useSlave();
            log.debug("切换到从数据库");
            return point.proceed();
        } finally {
            FailoverDataSource.clearDataSource();
        }
    }

    @Around("@annotation(writeOnly)")
    public Object around(ProceedingJoinPoint point, WriteOnly writeOnly) throws Throwable {
        try {
            FailoverDataSource.useMaster();
            log.debug("切换到主数据库");
            return point.proceed();
        } finally {
            FailoverDataSource.clearDataSource();
        }
    }
}
