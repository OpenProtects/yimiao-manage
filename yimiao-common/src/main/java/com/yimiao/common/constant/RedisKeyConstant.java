package com.yimiao.common.constant;

public class RedisKeyConstant {

    private RedisKeyConstant() {}

    public static final String PREFIX = "yimiao:";

    public static final String USER_TOKEN = PREFIX + "user:token:";
    public static final String USER_INFO = PREFIX + "user:info:";
    public static final String USER_CAPTCHA = PREFIX + "user:captcha:";
    public static final String USER_SMS = PREFIX + "user:sms:";

    public static final String VACCINE_INFO = PREFIX + "vaccine:info:";
    public static final String VACCINE_STOCK = PREFIX + "vaccine:stock:";
    public static final String VACCINE_SLOT = PREFIX + "vaccine:slot:";
    public static final String VACCINE_SLOT_LOCK = PREFIX + "vaccine:slot:lock:";

    public static final String APPOINTMENT_ORDER = PREFIX + "appointment:order:";
    public static final String APPOINTMENT_QUEUE = PREFIX + "appointment:queue:";
    public static final String APPOINTMENT_USER = PREFIX + "appointment:user:";

    public static final String BLACKLIST = PREFIX + "blacklist:";
    public static final String RATE_LIMIT = PREFIX + "rate:";
    public static final String RATE_LIMIT_USER = PREFIX + "rate:user:";

    public static final String PAYMENT_ORDER = PREFIX + "payment:order:";
    public static final String PAYMENT_NOTIFY = PREFIX + "payment:notify:";

    public static final String LOCK_APPOINTMENT = PREFIX + "lock:appointment:";
    public static final String LOCK_STOCK = PREFIX + "lock:stock:";
    public static final String LOCK_SLOT = PREFIX + "lock:slot:";

    public static final String STATISTICS_DAILY = PREFIX + "statistics:daily:";
    public static final String STATISTICS_REALTIME = PREFIX + "statistics:realtime";
}
