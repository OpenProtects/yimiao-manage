package com.yimiao.appointment.service.impl;

import com.yimiao.api.vo.VaccineeVO;
import com.yimiao.api.vo.VaccineVO;
import com.yimiao.appointment.client.UserClient;
import com.yimiao.appointment.client.VaccineClient;
import com.yimiao.appointment.service.BlacklistService;
import com.yimiao.appointment.service.RiskControlService;
import com.yimiao.common.constant.RedisKeyConstant;
import com.yimiao.common.exception.BusinessException;
import com.yimiao.common.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RiskControlServiceImpl implements RiskControlService {

    private final RedisService redisService;
    private final BlacklistService blacklistService;
    private final UserClient userClient;
    private final VaccineClient vaccineClient;

    public RiskControlServiceImpl(RedisService redisService, 
                                  BlacklistService blacklistService,
                                  UserClient userClient,
                                  VaccineClient vaccineClient) {
        this.redisService = redisService;
        this.blacklistService = blacklistService;
        this.userClient = userClient;
        this.vaccineClient = vaccineClient;
    }

    @Override
    public boolean checkAppointmentQualification(Long userId, Long vaccineeId, Long vaccineId, Integer doseNo) {
        if (blacklistService.isInBlacklist(userId)) {
            log.warn("用户在黑名单中: userId={}", userId);
            throw new BusinessException("您的账号已被限制预约，请联系客服");
        }

        try {
            var vaccineeResult = userClient.getVaccinee(vaccineeId);
            if (vaccineeResult == null || vaccineeResult.getData() == null) {
                throw new BusinessException("接种人信息不存在");
            }
            VaccineeVO vaccinee = vaccineeResult.getData();

            var vaccineResult = vaccineClient.getVaccine(vaccineId);
            if (vaccineResult == null || vaccineResult.getData() == null) {
                throw new BusinessException("疫苗信息不存在");
            }
            VaccineVO vaccine = vaccineResult.getData();

            String ageError = checkAge(vaccinee, vaccine);
            if (ageError != null) {
                throw new BusinessException(ageError);
            }

            String intervalError = checkInterval(vaccineeId, vaccineId, doseNo);
            if (intervalError != null) {
                throw new BusinessException(intervalError);
            }

            log.info("预约资格校验通过: userId={}, vaccineeId={}, vaccineId={}", userId, vaccineeId, vaccineId);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("预约资格校验异常: userId={}, vaccineeId={}", userId, vaccineeId, e);
            return true;
        }
    }

    @Override
    public String getQueuePosition(Long userId) {
        String queueKey = RedisKeyConstant.APPOINTMENT_QUEUE;
        Long rank = redisService.rank(queueKey, String.valueOf(userId));
        if (rank == null) {
            return "不在队列中";
        }
        return "第" + (rank + 1) + "位";
    }

    @Override
    public boolean tryAcquireRateLimit(Long userId, String operation) {
        String key = RedisKeyConstant.RATE_LIMIT_USER + userId + ":" + operation;
        Long count = redisService.increment(key);
        if (count == 1) {
            redisService.expire(key, 1, TimeUnit.MINUTES);
        }
        
        int maxRequests = 10;
        if (count > maxRequests) {
            log.warn("用户请求过于频繁: userId={}, operation={}", userId, operation);
            return false;
        }
        
        return true;
    }

    @Override
    public void releaseRateLimit(Long userId, String operation) {
        String key = RedisKeyConstant.RATE_LIMIT_USER + userId + ":" + operation;
        redisService.delete(key);
    }

    @Override
    public String checkInterval(Long vaccineeId, Long vaccineId, Integer doseNo) {
        if (doseNo == null || doseNo <= 1) {
            return null;
        }

        try {
            var vaccineResult = vaccineClient.getVaccine(vaccineId);
            if (vaccineResult == null || vaccineResult.getData() == null) {
                return null;
            }
            VaccineVO vaccine = vaccineResult.getData();

            Integer doseInterval = vaccine.getDoseInterval();
            if (doseInterval == null || doseInterval <= 0) {
                return null;
            }

            String lastVaccinationKey = "vaccination:last:" + vaccineeId + ":" + vaccineId;
            Object lastDate = redisService.getObject(lastVaccinationKey);
            
            if (lastDate != null) {
                LocalDate lastVaccinationDate = (LocalDate) lastDate;
                long daysSinceLastVaccination = ChronoUnit.DAYS.between(lastVaccinationDate, LocalDate.now());
                
                if (daysSinceLastVaccination < doseInterval) {
                    long remainingDays = doseInterval - daysSinceLastVaccination;
                    return "距离上次接种不足" + doseInterval + "天，还需等待" + remainingDays + "天";
                }
            }

            return null;
        } catch (Exception e) {
            log.error("检查接种间隔异常: vaccineeId={}, vaccineId={}", vaccineeId, vaccineId, e);
            return null;
        }
    }

    private String checkAge(VaccineeVO vaccinee, VaccineVO vaccine) {
        if (vaccinee.getBirthDate() == null) {
            return null;
        }

        LocalDate birthDate = vaccinee.getBirthDate();
        int age = Period.between(birthDate, LocalDate.now()).getYears();

        Integer minAge = vaccine.getMinAge();
        Integer maxAge = vaccine.getMaxAge();

        if (minAge != null && age < minAge) {
            return "该疫苗最小接种年龄为" + minAge + "岁，当前年龄" + age + "岁";
        }

        if (maxAge != null && age > maxAge) {
            return "该疫苗最大接种年龄为" + maxAge + "岁，当前年龄" + age + "岁";
        }

        return null;
    }
}
