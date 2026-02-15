package com.yimiao.common.constant;

public class AppointmentConstant {

    private AppointmentConstant() {}

    public static final Integer STATUS_PENDING = 0;
    public static final Integer STATUS_PAID = 1;
    public static final Integer STATUS_CANCELLED = 2;
    public static final Integer STATUS_COMPLETED = 3;
    public static final Integer STATUS_EXPIRED = 4;
    public static final Integer STATUS_REFUNDED = 5;

    public static final Integer PAY_STATUS_UNPAID = 0;
    public static final Integer PAY_STATUS_PAID = 1;
    public static final Integer PAY_STATUS_REFUNDED = 2;

    public static final Integer VERIFY_STATUS_UNVERIFIED = 0;
    public static final Integer VERIFY_STATUS_VERIFIED = 1;

    public static final Integer DOSE_FIRST = 1;
    public static final Integer DOSE_SECOND = 2;
    public static final Integer DOSE_THIRD = 3;
    public static final Integer DOSE_BOOSTER = 4;
}
