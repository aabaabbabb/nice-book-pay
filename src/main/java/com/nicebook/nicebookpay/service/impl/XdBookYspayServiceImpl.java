package com.nicebook.nicebookpay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eptok.yspay.opensdkjava.util.DateUtil;
import com.eptok.yspay.opensdkjava.util.YsOnlineSignUtils;
import com.nicebook.nicebookpay.entity.XdBookOrder;
import com.nicebook.nicebookpay.entity.XdBookYspay;
import com.nicebook.nicebookpay.mapper.XdBookYspayMapper;
import com.nicebook.nicebookpay.service.XdBookYspayService;
import com.nicebook.nicebookpay.utils.ResourcePathUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class XdBookYspayServiceImpl extends ServiceImpl<XdBookYspayMapper, XdBookYspay>
        implements XdBookYspayService {

    public static final String URL = "https://openapi.ysepay.com/gateway.do";

    @Autowired
    private XdBookYspayMapper bookYspayMapper;

    @Override
    public XdBookYspay selectById(Integer id) {
        return bookYspayMapper.selectById(id);
    }

    @Override
    public XdBookYspay selectByIsDefaultAndParentId(Integer isDefault, String partnerid) {
        return bookYspayMapper.selectByIsDefaultAndParentId(isDefault, partnerid);
    }

    @Override
    public String createOrder(XdBookOrder xdBookOrder) {
        if (xdBookOrder == null) {
            throw new IllegalArgumentException("订单为空");
        }

        XdBookYspay xdBookYspay = resolveYsPayConfig(xdBookOrder);
        Map<String, String> params = new LinkedHashMap<>();
        params.put("method", "ysepay.online.wap.directpay.createbyuser");
        params.put("partner_id", xdBookYspay.getPartnerid());
        params.put("timestamp", DateUtil.getDateNow());
        params.put("charset", xdBookYspay.getCharset());
        params.put("sign_type", xdBookYspay.getSigntype());
        params.put("notify_url", xdBookYspay.getNotifyurl() + "/yspay/showNotify");
        params.put("return_url", xdBookYspay.getNotifyurl() + "/yspay/showReturn");
        params.put("version", xdBookYspay.getVersion());
        params.put("tran_type", String.valueOf(xdBookYspay.getTrantype()));
        params.put("out_trade_no", xdBookOrder.getOrderid());
        params.put("shopdate", DateUtil.getDateFormat(new Date(), "yyyyMMdd"));
        params.put("subject", xdBookOrder.getHotelName());
        params.put("total_amount", String.valueOf(xdBookOrder.getPayprice()));
        params.put("seller_id", xdBookYspay.getSellerid());
        params.put("seller_name", xdBookYspay.getSellername());
        params.put("timeout_express", xdBookYspay.getTimeoutexpress());
        params.put("business_code", xdBookYspay.getBusinesscode());
        params.put("pay_mode", "native");
        params.put("bank_type", "1903000");

        try {
            String rel = xdBookYspay.getPrivatekeyfilepath();
            log.info("YSPay key path from db={}", rel);
            if (rel == null || rel.isBlank()) {
                throw new IllegalArgumentException("银盛私钥路径为空");
            }
            if (rel.startsWith("/")) {
                rel = rel.substring(1);
            }
            String keyPath = ResourcePathUtil.getResourcePath(rel);
            log.info("YSPay resolved key path={}", keyPath);

            String sign = YsOnlineSignUtils.sign(params, xdBookYspay.getPrivatekeypassword(), keyPath);
            params.put("sign", sign);

            log.info("YSPay request params={}", sanitizeParams(params));
            return buildAutoSubmitHtml(params);
        } catch (Exception e) {
            log.error("YSPay create order failed, orderId={}, params={}",
                    xdBookOrder.getOrderid(),
                    sanitizeParams(params),
                    e);
            throw new RuntimeException("银盛下单失败：" + e.getMessage(), e);
        }
    }

    private XdBookYspay resolveYsPayConfig(XdBookOrder order) {
        XdBookYspay ysPay = null;
        if (order != null && order.getParentid() != null) {
            ysPay = selectByIsDefaultAndParentId(1, String.valueOf(order.getParentid()));
        }
        if (ysPay == null) {
            ysPay = bookYspayMapper.selectDefaultOne(1);
        }
        if (ysPay == null) {
            throw new RuntimeException("未找到银盛支付配置");
        }
        return ysPay;
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
}
