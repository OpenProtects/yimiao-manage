package com.yimiao.appointment.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yimiao.api.dto.AppointmentDTO;
import com.yimiao.api.vo.AppointmentVO;
import com.yimiao.appointment.client.VaccineClient;
import com.yimiao.appointment.entity.Appointment;
import com.yimiao.appointment.mapper.AppointmentMapper;
import com.yimiao.appointment.service.AppointmentService;
import com.yimiao.appointment.service.BlacklistService;
import com.yimiao.appointment.service.RiskControlService;
import com.yimiao.common.constant.AppointmentConstant;
import com.yimiao.common.constant.RedisKeyConstant;
import com.yimiao.common.core.ResultCode;
import com.yimiao.common.exception.BusinessException;
import com.yimiao.common.redis.RedisLock;
import com.yimiao.common.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    private final RedisService redisService;
    private final RedisLock redisLock;
    private final BlacklistService blacklistService;
    private final RiskControlService riskControlService;
    private final RabbitTemplate rabbitTemplate;
    private final VaccineClient vaccineClient;

    public AppointmentServiceImpl(RedisService redisService, RedisLock redisLock,
                                  BlacklistService blacklistService, RiskControlService riskControlService,
                                  RabbitTemplate rabbitTemplate, VaccineClient vaccineClient) {
        this.redisService = redisService;
        this.redisLock = redisLock;
        this.blacklistService = blacklistService;
        this.riskControlService = riskControlService;
        this.rabbitTemplate = rabbitTemplate;
        this.vaccineClient = vaccineClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAppointment(AppointmentDTO dto) {
        String lockKey = RedisKeyConstant.LOCK_APPOINTMENT + dto.getUserId();
        String lockValue = IdUtil.fastSimpleUUID();
        
        if (!redisLock.tryLock(lockKey, lockValue, 30, TimeUnit.SECONDS)) {
            throw new BusinessException("操作过于频繁，请稍后再试");
        }
        
        try {
            if (!riskControlService.checkAppointmentQualification(dto.getUserId(), dto.getVaccineeId(), 
                    dto.getVaccineId(), dto.getDoseNo())) {
                throw new BusinessException("不符合预约条件");
            }

            boolean slotDecremented = false;
            boolean stockDecremented = false;
            
            try {
                var slotResult = vaccineClient.decrementSlot(dto.getSlotId());
                if (slotResult == null || slotResult.getCode() != 200 || !Boolean.TRUE.equals(slotResult.getData())) {
                    throw new BusinessException("号源不足，请选择其他时段");
                }
                slotDecremented = true;

                var stockResult = vaccineClient.decrementStock(dto.getVaccineId(), dto.getSiteId(), 1);
                if (stockResult == null || stockResult.getCode() != 200 || !Boolean.TRUE.equals(stockResult.getData())) {
                    throw new BusinessException("疫苗库存不足，请选择其他接种点");
                }
                stockDecremented = true;

                String orderNo = generateOrderNo();
                
                Appointment appointment = new Appointment();
                appointment.setOrderNo(orderNo);
                appointment.setUserId(dto.getUserId());
                appointment.setVaccineeId(dto.getVaccineeId());
                appointment.setVaccineId(dto.getVaccineId());
                appointment.setSiteId(dto.getSiteId());
                appointment.setSlotId(dto.getSlotId());
                appointment.setDoseNo(dto.getDoseNo());
                appointment.setStatus(AppointmentConstant.STATUS_PENDING);
                appointment.setPayStatus(AppointmentConstant.PAY_STATUS_UNPAID);
                appointment.setAmount(BigDecimal.ZERO);
                appointment.setVerifyStatus(AppointmentConstant.VERIFY_STATUS_UNVERIFIED);
                
                save(appointment);
                
                String cacheKey = RedisKeyConstant.APPOINTMENT_ORDER + appointment.getId();
                redisService.setObject(cacheKey, appointment, 30, TimeUnit.MINUTES);
                
                sendAppointmentMessage(appointment);
                
                log.info("创建预约订单成功: orderNo={}, userId={}", orderNo, dto.getUserId());
                return appointment.getId();
            } catch (BusinessException e) {
                if (slotDecremented) {
                    try {
                        vaccineClient.incrementSlot(dto.getSlotId());
                    } catch (Exception ex) {
                        log.error("回滚号源失败: slotId={}", dto.getSlotId(), ex);
                    }
                }
                if (stockDecremented) {
                    try {
                        vaccineClient.incrementStock(dto.getVaccineId(), dto.getSiteId(), 1);
                    } catch (Exception ex) {
                        log.error("回滚库存失败: vaccineId={}, siteId={}", dto.getVaccineId(), dto.getSiteId(), ex);
                    }
                }
                throw e;
            }
        } finally {
            redisLock.unlock(lockKey, lockValue);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelAppointment(Long id, Long userId, String reason) {
        Appointment appointment = getById(id);
        if (appointment == null || appointment.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "预约不存在");
        }
        
        if (!appointment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权限操作");
        }
        
        if (appointment.getStatus() != AppointmentConstant.STATUS_PENDING && 
            appointment.getStatus() != AppointmentConstant.STATUS_PAID) {
            throw new BusinessException("当前状态无法取消");
        }

        try {
            vaccineClient.incrementSlot(appointment.getSlotId());
            vaccineClient.incrementStock(appointment.getVaccineId(), appointment.getSiteId(), 1);
        } catch (Exception e) {
            log.error("释放库存失败: appointmentId={}", id, e);
        }
        
        appointment.setStatus(AppointmentConstant.STATUS_CANCELLED);
        appointment.setCancelReason(reason);
        appointment.setCancelTime(LocalDateTime.now());
        updateById(appointment);
        
        redisService.delete(RedisKeyConstant.APPOINTMENT_ORDER + id);
        
        log.info("取消预约成功: id={}, reason={}", id, reason);
    }

    @Override
    public AppointmentVO getDetail(Long id) {
        Appointment appointment = getById(id);
        if (appointment == null || appointment.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "预约不存在");
        }
        return convertToVO(appointment);
    }

    @Override
    public Page<AppointmentVO> pageList(int pageNum, int pageSize, Long userId, Integer status) {
        Page<Appointment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(Appointment::getUserId, userId);
        }
        if (status != null) {
            wrapper.eq(Appointment::getStatus, status);
        }
        wrapper.eq(Appointment::getDeleted, 0);
        wrapper.orderByDesc(Appointment::getCreateTime);
        
        Page<Appointment> result = page(page, wrapper);
        
        Page<AppointmentVO> voPage = new Page<>();
        voPage.setCurrent(result.getCurrent());
        voPage.setSize(result.getSize());
        voPage.setTotal(result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        
        return voPage;
    }

    @Override
    public List<AppointmentVO> listByUserId(Long userId) {
        List<Appointment> list = list(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getUserId, userId)
                .eq(Appointment::getDeleted, 0)
                .orderByDesc(Appointment::getCreateTime));
        
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyAppointment(Long id, Long operatorId) {
        Appointment appointment = getById(id);
        if (appointment == null || appointment.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "预约不存在");
        }
        
        if (appointment.getStatus() != AppointmentConstant.STATUS_PAID) {
            throw new BusinessException("订单未支付，无法核销");
        }
        
        appointment.setStatus(AppointmentConstant.STATUS_COMPLETED);
        appointment.setVerifyStatus(AppointmentConstant.VERIFY_STATUS_VERIFIED);
        appointment.setVerifyTime(LocalDateTime.now());
        appointment.setVerifyUserId(operatorId);
        updateById(appointment);

        String lastVaccinationKey = "vaccination:last:" + appointment.getVaccineeId() + ":" + appointment.getVaccineId();
        redisService.setObject(lastVaccinationKey, LocalDate.now(), 365, TimeUnit.DAYS);
        
        log.info("核销预约成功: id={}, operatorId={}", id, operatorId);
    }

    @Override
    @Scheduled(cron = "0 */5 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void expireAppointments() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(30);
        
        List<Appointment> expiredList = list(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getStatus, AppointmentConstant.STATUS_PENDING)
                .eq(Appointment::getPayStatus, AppointmentConstant.PAY_STATUS_UNPAID)
                .lt(Appointment::getCreateTime, expireTime)
                .eq(Appointment::getDeleted, 0));
        
        for (Appointment appointment : expiredList) {
            appointment.setStatus(AppointmentConstant.STATUS_EXPIRED);

            try {
                vaccineClient.incrementSlot(appointment.getSlotId());
                vaccineClient.incrementStock(appointment.getVaccineId(), appointment.getSiteId(), 1);
            } catch (Exception e) {
                log.error("释放过期订单库存失败: appointmentId={}", appointment.getId(), e);
            }
            
            updateById(appointment);
            
            log.info("预约订单超时过期: orderNo={}", appointment.getOrderNo());
        }
    }

    @Override
    public Appointment getByOrderNo(String orderNo) {
        return getOne(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getOrderNo, orderNo)
                .eq(Appointment::getDeleted, 0));
    }

    @Override
    public void updatePayStatus(Long id, Integer payStatus, String tradeNo) {
        Appointment appointment = getById(id);
        if (appointment == null) {
            return;
        }
        
        appointment.setPayStatus(payStatus);
        appointment.setPayTradeNo(tradeNo);
        appointment.setPayTime(LocalDateTime.now());
        
        if (payStatus == AppointmentConstant.PAY_STATUS_PAID) {
            appointment.setStatus(AppointmentConstant.STATUS_PAID);
        }
        
        updateById(appointment);
    }

    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();
        return "YM" + dateStr + randomStr;
    }

    private void sendAppointmentMessage(Appointment appointment) {
        try {
            rabbitTemplate.convertAndSend("appointment.exchange", "appointment.created", 
                    JSON.toJSONString(appointment));
        } catch (Exception e) {
            log.error("发送预约消息失败: orderNo={}", appointment.getOrderNo(), e);
        }
    }

    private AppointmentVO convertToVO(Appointment appointment) {
        AppointmentVO vo = new AppointmentVO();
        BeanUtil.copyProperties(appointment, vo);
        return vo;
    }
}
