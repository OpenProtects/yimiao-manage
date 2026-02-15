package com.yimiao.common.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "方法不允许"),
    SYSTEM_ERROR(500, "系统错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_DISABLED(1003, "用户已被禁用"),
    USER_EXISTS(1004, "用户已存在"),
    PHONE_EXISTS(1005, "手机号已注册"),
    ID_CARD_EXISTS(1006, "身份证已绑定"),
    REAL_NAME_NOT_VERIFIED(1007, "未实名认证"),
    VERIFICATION_CODE_ERROR(1008, "验证码错误"),
    VERIFICATION_CODE_EXPIRED(1009, "验证码已过期"),

    VACCINE_NOT_FOUND(2001, "疫苗不存在"),
    VACCINE_STOCK_NOT_ENOUGH(2002, "疫苗库存不足"),
    SLOT_NOT_FOUND(2003, "号源不存在"),
    SLOT_NOT_AVAILABLE(2004, "号源不可用"),
    SLOT_FULL(2005, "号源已满"),

    APPOINTMENT_NOT_FOUND(3001, "预约不存在"),
    APPOINTMENT_EXISTS(3002, "已有预约"),
    APPOINTMENT_CANCELLED(3003, "预约已取消"),
    APPOINTMENT_COMPLETED(3004, "预约已完成"),
    APPOINTMENT_EXPIRED(3005, "预约已过期"),
    AGE_NOT_QUALIFIED(3006, "年龄不符合接种要求"),
    INTERVAL_NOT_QUALIFIED(3007, "接种间隔不符合要求"),
    NOT_IN_BLACKLIST(3008, "不在黑名单中"),
    IN_BLACKLIST(3009, "已被加入黑名单"),

    ORDER_NOT_FOUND(4001, "订单不存在"),
    ORDER_PAID(4002, "订单已支付"),
    ORDER_CANCELLED(4003, "订单已取消"),
    PAY_FAILED(4004, "支付失败"),
    REFUND_FAILED(4005, "退款失败"),

    DB_ERROR(5001, "数据库错误"),
    DB_UNAVAILABLE(5002, "数据库不可用"),
    CACHE_ERROR(5003, "缓存错误"),
    MQ_ERROR(5004, "消息队列错误"),

    RATE_LIMIT(6001, "请求过于频繁，请稍后再试"),
    IN_QUEUE(6002, "正在排队中，请稍候");

    private final Integer code;
    private final String message;
}
