package com.nicebook.nicebookpay.config;

import java.util.HashMap;
import java.util.Map;

public class PayConstant {

    private PayConstant() {}
    public static Integer YES=1;
    public static Integer NO=-1;

    public static final Integer WEIXIN_PAY=0;
    public static final Integer ALI_PAY=1;
    public static final Integer YS_PAY=2;

    public static Map<Integer, String> PAYMENT_METHOD_MAP = new HashMap<Integer, String>();
    static {
        PAYMENT_METHOD_MAP.put(WEIXIN_PAY, "微信");
        PAYMENT_METHOD_MAP.put(ALI_PAY,"支付宝");
        PAYMENT_METHOD_MAP.put(YS_PAY,"银盛支付");
    }

    public static final Integer SEND_YES = 1;
    public static final Integer SEND_NO = 0;
    public static final Integer PAY_NO=2;//未支付
    public static final Integer PAY_YES=3;//已支付
    public static final Integer PAY_RETURN=4;//已退款
    public static final Integer PAY_RETURN_REBATE=5;//部分退款





    public static Map<Integer, String> PAY_MAP=new HashMap<>();
    static{
        PAY_MAP.put(SEND_NO, "未发送");
        PAY_MAP.put(PAY_NO, "已发送|未支付");
        PAY_MAP.put(PAY_YES, "已支付");
        PAY_MAP.put(PAY_RETURN, "已退款");
    }

    public static final String ALIPAY_TRADE_SUCCESS = "TRADE_SUCCESS";

    public static final String ALIPAY_TRADE_FINISHED = "TRADE_FINISHED";

    public static final String ALIPAY_TRADE_CLOSED = "TRADE_CLOSED";
}
