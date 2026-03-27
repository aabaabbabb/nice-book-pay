package com.nicebook.nicebookpay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eptok.yspay.opensdkjava.util.DateUtil;
import com.eptok.yspay.opensdkjava.util.YsOnlineSignUtils;
import com.nicebook.nicebookpay.entity.XdBookOrder;
import com.nicebook.nicebookpay.entity.XdBookPaymentMethods;
import com.nicebook.nicebookpay.mapper.XdBookPaymentMethodsMapper;
import com.nicebook.nicebookpay.service.XdBookYspayService;
import com.nicebook.nicebookpay.utils.ResourcePathUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class XdBookYspayServiceImpl extends ServiceImpl<XdBookPaymentMethodsMapper, XdBookPaymentMethods>
        implements XdBookYspayService {

    public static final String URL = "https://openapi.ysepay.com/gateway.do";

    @Autowired
    private XdBookPaymentMethodsMapper bookPaymentMethodsMapper;

    @Override
    public String createOrder(XdBookOrder xdBookOrder) {
        if (xdBookOrder == null) {
            throw new IllegalArgumentException("订单为空");
        }

        XdBookPaymentMethods paymentMethods = resolveYsPayConfig(xdBookOrder);
        validateYsPayConfig(paymentMethods);

        Map<String, String> params = new LinkedHashMap<>();
        params.put("method", "ysepay.online.wap.directpay.createbyuser");
        params.put("partner_id", paymentMethods.getPartnerId());
        params.put("timestamp", DateUtil.getDateNow());
        params.put("charset", paymentMethods.getCharSet());
        params.put("sign_type", paymentMethods.getSignType());
        params.put("notify_url", paymentMethods.getPaymentCallbackAddress() + "/yspay/showNotify");
        params.put("return_url", paymentMethods.getPaymentCallbackAddress() + "/yspay/showReturn");
        params.put("version", paymentMethods.getYsVersion());
        params.put("tran_type", paymentMethods.getTranType());
        params.put("out_trade_no", xdBookOrder.getOrderid());
        params.put("shopdate", DateUtil.getDateFormat(new Date(), "yyyyMMdd"));
        params.put("subject", xdBookOrder.getHotelName());
        params.put("total_amount", String.valueOf(xdBookOrder.getTotalPrice()));
        params.put("seller_id", paymentMethods.getSellerId());
        params.put("seller_name", paymentMethods.getSellerName());
        params.put("timeout_express", paymentMethods.getTimeoutExpress());
        params.put("business_code", paymentMethods.getBusinessCode());
        params.put("pay_mode", "native");
        params.put("bank_type", "1903000");

        try {
            String rel = paymentMethods.getPrivateKeyFilePath();
            log.info("YSPay key path from db={}", rel);
            if (rel == null || rel.isBlank()) {
                throw new IllegalArgumentException("银盛私钥路径为空");
            }
            if (rel.startsWith("/")) {
                rel = rel.substring(1);
            }
            String keyPath = ResourcePathUtil.getResourcePath(rel);
            log.info("YSPay resolved key path={}", keyPath);

            String sign = YsOnlineSignUtils.sign(params, paymentMethods.getPrivateKeyPassword(), keyPath);
            params.put("sign", sign);

            log.info("YSPay request params={}", sanitizeParams(params));
            return buildAutoSubmitHtml(params);
        } catch (Exception e) {
            log.error("YSPay create order failed, orderId={}, params={}",
                    xdBookOrder.getOrderid(),
                    sanitizeParams(params),
                    e);
            throw new RuntimeException("银盛下单失败: " + e.getMessage(), e);
        }
    }

    private XdBookPaymentMethods resolveYsPayConfig(XdBookOrder order) {
        XdBookPaymentMethods paymentMethods = null;
        if (order != null && order.getParentid() != null) {
            paymentMethods = bookPaymentMethodsMapper.selectOneByPaymentChannelsAndStatus("2", "1002");
        }

        if (paymentMethods == null) {
            throw new RuntimeException("未找到银盛支付配置");
        }
        return paymentMethods;
    }

    private void validateYsPayConfig(XdBookPaymentMethods paymentMethods) {
        requireNonBlank(paymentMethods.getCharSet(), "char_set");
        requireNonBlank(paymentMethods.getSignType(), "sign_type");
        requireNonBlank(paymentMethods.getPaymentCallbackAddress(), "payment_callback_address");
        requireNonBlank(paymentMethods.getYsVersion(), "ys_version");
        requireNonBlank(paymentMethods.getTranType(), "tran_type");
        requireNonBlank(paymentMethods.getSellerId(), "seller_id");
        requireNonBlank(paymentMethods.getSellerName(), "seller_name");
        requireNonBlank(paymentMethods.getTimeoutExpress(), "timeout_express");
        requireNonBlank(paymentMethods.getBusinessCode(), "business_code");
        requireNonBlank(paymentMethods.getPrivateKeyPassword(), "private_key_password");
        requireNonBlank(paymentMethods.getPrivateKeyFilePath(), "private_key_file_path");
    }

    private String buildAutoSubmitHtml(Map<String, String> params) {
        StringBuilder sbHtml = new StringBuilder();
        sbHtml.append("<!doctype html>");
        sbHtml.append("<html><head>");
        sbHtml.append("<meta charset='utf-8' />");
        sbHtml.append("<meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'/>");
        sbHtml.append("<meta name='viewport' content='width=device-width,initial-scale=1' />");
        sbHtml.append("<title>银盛支付跳转</title>");
        sbHtml.append("</head><body>");
        sbHtml.append("<form style='display:none;' id='topay' method='post' action='").append(URL).append("'>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sbHtml.append("<input type='hidden' name='")
                    .append(escapeHtml(entry.getKey()))
                    .append("' value='")
                    .append(escapeHtml(entry.getValue()))
                    .append("'/>");
        }
        sbHtml.append("</form>");
        sbHtml.append("<p>正在跳转到银盛支付...</p>");
        sbHtml.append("<script>document.getElementById('topay').submit();</script>");
        sbHtml.append("</body></html>");
        return sbHtml.toString();
    }

    private Map<String, String> sanitizeParams(Map<String, String> params) {
        Map<String, String> copy = new LinkedHashMap<>(params);
        if (copy.containsKey("sign")) {
            copy.put("sign", "***");
        }
        return copy;
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "字段值是空白");
        }
    }
}
