package com.nicebook.nicebookpay.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nicebook.nicebookpay.entity.XdBookFeedback;
import com.nicebook.nicebookpay.entity.XdBookOrder;
import com.nicebook.nicebookpay.service.XdBookFeedbackService;
import com.nicebook.nicebookpay.service.XdBookOrderService;
import com.nicebook.nicebookpay.service.XdBookWeChatPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/wechatpay")
public class WeChatController {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int PAY_STATE_READY = 2;
    private static final int PAY_STATE_SUCCESS = 3;
    private static final int PAYMENT_METHOD_WECHAT = 1;
    private static final int FEEDBACK_USER_ID = 3;
    private static final String FEEDBACK_USER_NAME = "客户";
    private static final String FEEDBACK_CONTENT = "拉起微信支付";

    @Autowired
    private XdBookOrderService orderService;

    @Autowired
    private XdBookWeChatPayService bookWeChatPayService;

    @Autowired
    private XdBookFeedbackService bookFeedbackService;

    @GetMapping("/toPrepay/{id}")
    public ResponseEntity<String> toPrepay(@PathVariable("id") Integer orderId, HttpServletRequest request) {
        if (orderId == null) {
            return ResponseEntity.badRequest().body("订单号不能为空");
        }

        XdBookOrder order = orderService.getById(orderId);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("订单不存在");
        }

        if (!Integer.valueOf(PAY_STATE_READY).equals(order.getPayState())) {
            return redirect(buildFallbackRedirect(order));
        }

        order.setIp(resolveClientIp(request));
        recordFeedback(order, FEEDBACK_CONTENT);

        Map<String, String> result;
        try {
            result = bookWeChatPayService.createOrder(order);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("微信预下单失败");
        }

        String h5Url = result == null ? null : result.get("h5_url");
        if (isBlank(h5Url)) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("微信支付地址为空");
        }

        String redirectUrl = buildRedirectUrl(result == null ? null : result.get("url"), order);
        String target = h5Url + "&redirect_url=" + URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8);
        return redirect(target);
    }

    @PostMapping(value = "/notify", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> notify(
            @RequestBody(required = false) String body,
            @RequestHeader(value = "Wechatpay-Signature", required = false) String signature,
            @RequestHeader(value = "Wechatpay-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "Wechatpay-Nonce", required = false) String nonce,
            @RequestHeader(value = "Wechatpay-Serial", required = false) String serial) {
        if (isBlank(body)) {
            return weChatFail("通知内容为空");
        }

        if (!hasWeChatHeaders(signature, timestamp, nonce, serial)) {
            return weChatFail("缺少微信支付签名头");
        }

        try {
            String decryptedBody = bookWeChatPayService.decryptOrder(body);
            JsonNode node = OBJECT_MAPPER.readTree(decryptedBody);

            String outTradeNo = text(node, "out_trade_no");
            if (isBlank(outTradeNo)) {
                return weChatFail("缺少商户订单号");
            }

            XdBookOrder order = orderService.getByOrderId(outTradeNo);
            if (order == null && isDigits(outTradeNo)) {
                order = orderService.getById(Integer.parseInt(outTradeNo));
            }
            if (order == null) {
                return weChatFail("订单不存在");
            }

            if (Integer.valueOf(PAY_STATE_SUCCESS).equals(order.getPayState())) {
                return weChatSuccess();
            }

            String transactionId = text(node, "transaction_id");
            JsonNode amountNode = node.get("amount");
            Integer payerTotal = amountNode == null || amountNode.get("payer_total") == null || amountNode.get("payer_total").isNull()
                    ? null
                    : amountNode.get("payer_total").asInt();
            int outTotal = toFen(order.getPayprice());

            if (payerTotal == null || payerTotal != outTotal) {
                recordFeedback(order, "微信支付结果：支付金额错误,支付金额:" + (payerTotal == null ? "未知" : payerTotal / 100.0) + "元");
                return weChatFail("支付金额错误");
            }

            order.setTransactionid(transactionId);
            order.setPayState(PAY_STATE_SUCCESS);
            order.setPaymentMethod(PAYMENT_METHOD_WECHAT);
            boolean updated = orderService.updateById(order);
            if (!updated) {
                return weChatFail("订单更新失败");
            }

            recordFeedback(order, "微信支付结果：成功;收款商户号" + safe(order.getMchid()) + ",支付金额:" + payerTotal / 100.0 + "元");
            return weChatSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return weChatFail("处理失败");
        }
    }

    private boolean hasWeChatHeaders(String signature, String timestamp, String nonce, String serial) {
        return !isBlank(signature) && !isBlank(timestamp) && !isBlank(nonce) && !isBlank(serial);
    }

    private ResponseEntity<String> redirect(String url) {
        if (isBlank(url)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("重定向地址为空");
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, url)
                .build();
    }

    private void recordFeedback(XdBookOrder order, String content) {
        String orderId = resolveOrderId(order);
        if (isBlank(orderId)) {
            return;
        }
        XdBookFeedback feedback = new XdBookFeedback();
        feedback.setCreateDatetime(new Date());
        feedback.setAid(FEEDBACK_USER_ID);
        feedback.setUName(FEEDBACK_USER_NAME);
        feedback.setContent(content);
        feedback.setOrderId(orderId);
        bookFeedbackService.insertFeedback(feedback);
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "127.0.0.1";
        }
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP"
        };
        for (String header : headers) {
            String value = request.getHeader(header);
            if (!isBlank(value) && !"unknown".equalsIgnoreCase(value)) {
                int commaIndex = value.indexOf(',');
                return commaIndex > 0 ? value.substring(0, commaIndex).trim() : value.trim();
            }
        }
        String remoteAddr = request.getRemoteAddr();
        return isBlank(remoteAddr) ? "127.0.0.1" : remoteAddr;
    }

    private String buildFallbackRedirect(XdBookOrder order) {
        String orderId = resolveOrderId(order);
        if (isBlank(orderId)) {
            return "/";
        }
        return "/pay/" + orderId;
    }

    private String buildRedirectUrl(String baseUrl, XdBookOrder order) {
        String suffix = order == null ? null : order.getGuid();
        if (isBlank(suffix)) {
            suffix = resolveOrderId(order);
        }
        if (isBlank(baseUrl)) {
            return isBlank(suffix) ? "/" : "/" + suffix;
        }
        if (isBlank(suffix)) {
            return baseUrl;
        }
        return baseUrl.endsWith("/") ? baseUrl + suffix : baseUrl + "/" + suffix;
    }

    private String resolveOrderId(XdBookOrder order) {
        if (order == null) {
            return null;
        }
        String orderId = order.getOrderid();
        if (!isBlank(orderId)) {
            return orderId;
        }
        Integer id = order.getId();
        return id == null ? null : id.toString();
    }

    private ResponseEntity<String> weChatSuccess() {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"SUCCESS\",\"message\":\"成功\"}");
    }

    private ResponseEntity<String> weChatFail(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"FAIL\",\"message\":\"" + escapeJson(message) + "\"}");
    }

    private int toFen(Double amount) {
        if (amount == null) {
            throw new IllegalArgumentException("订单支付价格为空");
        }
        return BigDecimal.valueOf(amount)
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();
    }

    private String text(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode child = node.get(field);
        return child == null || child.isNull() ? null : child.asText();
    }

    private boolean isDigits(String value) {
        if (isBlank(value)) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String escapeJson(String value) {
        return safe(value).replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
