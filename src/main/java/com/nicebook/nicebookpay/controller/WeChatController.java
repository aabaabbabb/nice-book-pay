package com.nicebook.nicebookpay.controller;

import com.nicebook.nicebookpay.entity.XdBookFeedback;
import com.nicebook.nicebookpay.entity.XdBookOrder;
import com.nicebook.nicebookpay.service.LockService;
import com.nicebook.nicebookpay.service.XdBookFeedbackService;
import com.nicebook.nicebookpay.service.XdBookOrderService;
import com.nicebook.nicebookpay.service.XdBookWeChatPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/api/wechatpay")
public class WeChatController {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int PAY_STATE_READY = 2;
    private static final int PAY_STATE_SUCCESS = 3;
    private static final int FEEDBACK_USER_ID = 3;
    private static final String FEEDBACK_USER_NAME = "customer";
    private static final String FEEDBACK_START_CONTENT = "wechat pay started";
    private static final String FEEDBACK_FINISH_CONTENT = "wechat pay finished";

    @Autowired
    private XdBookOrderService orderService;

    @Autowired
    private XdBookWeChatPayService bookWeChatPayService;

    @Autowired
    private XdBookFeedbackService bookFeedbackService;

    @Autowired
    private LockService lockService;

    @ResponseBody
    @GetMapping("/toPrepay/{id}")
    public ResponseEntity<String> toPrepay(@PathVariable("id") Integer orderId, HttpServletRequest request) {
        if (orderId == null) {
            return ResponseEntity.badRequest().body("订单号为必填");
        }

        XdBookOrder order = orderService.getById(orderId);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("未找到订单");
        }

        if (!Integer.valueOf(PAY_STATE_READY).equals(order.getPayState())) {
            return redirect(buildFallbackRedirect(order));
        }

        order.setIp(resolveClientIp(request));
        recordFeedback(order, FEEDBACK_START_CONTENT);

        Map<String, String> result;
        try {
            result = bookWeChatPayService.createOrder(order);
        } catch (Exception ex) {
            log.error("创建微信订单失败，订单ID={}", orderId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("创建微信订单失败");
        }

        String h5Url = result == null ? null : result.get("h5_url");
        if (isBlank(h5Url)) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("微信支付网址为空");
        }

        String redirectUrl = buildFinishUrl(order, request);
        String target = h5Url + "&redirect_url=" + URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8);
        log.info("微信重定向地址={}", target);
        return redirect(target);
    }

    @ResponseBody
    @PostMapping("/notify")
    public ResponseEntity<String> notify(@RequestBody String body) {
        String traceId = UUID.randomUUID().toString();

        if (isBlank(body)) {
            return weChatFail("notify body is empty");
        }

        try {
            String decryptedBody = bookWeChatPayService.decryptOrder(body);
            JsonNode node = OBJECT_MAPPER.readTree(decryptedBody);
            String orderId = node.path("out_trade_no").asText();
            String transactionId = node.path("transaction_id").asText();
            int total = node.path("amount").path("payer_total").asInt();
            log.info("[{}] wechat payer_total={}", traceId, total);

            int updated = orderService.updatePaySuccess(orderId, transactionId);
            if (updated == 0) {
                log.info("[{}] 回调已处理", traceId);
                return success();
            }
            log.info("[{}] 付款成功订单号={}", traceId, orderId);
            return success();
        } catch (Exception e) {
            log.error("[{}] 回调通知失败", traceId, e);
            return fail("系统错误");
        }
    }

    @GetMapping("/finish")
    public String finish(@RequestParam("id") Integer id, Model model) {
        XdBookOrder order = orderService.getById(id);
        model.addAttribute("order", order);
        model.addAttribute("orderId", order == null ? String.valueOf(id) : resolveOrderId(order));

        if (order != null && Integer.valueOf(PAY_STATE_SUCCESS).equals(order.getPayState())) {
            recordFeedback(order, FEEDBACK_FINISH_CONTENT);
        }

        return "result";
    }

    private ResponseEntity<String> success() {
        return ResponseEntity.ok("{\"code\":\"SUCCESS\",\"message\":\"success\"}");
    }

    private ResponseEntity<String> fail(String msg) {
        return ResponseEntity.ok("{\"code\":\"FAIL\",\"message\":\"" + msg + "\"}");
    }

    private ResponseEntity<String> redirect(String url) {
        if (isBlank(url)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("redirect url is empty");
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

    private String buildFinishUrl(XdBookOrder order, HttpServletRequest request) {
        Integer id = order == null ? null : order.getId();
        if (id == null) {
            return "/";
        }
        String path = "/api/wechatpay/finish?id=" + id;
        String baseUrl = resolveRequestBaseUrl(request);
        return isBlank(baseUrl) ? path : baseUrl + path;
    }

    private String resolveRequestBaseUrl(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int port = request.getServerPort();
        if (isBlank(scheme) || isBlank(serverName) || port <= 0) {
            return "";
        }
        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
        return defaultPort ? scheme + "://" + serverName : scheme + "://" + serverName + ":" + port;
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
                .body("{\"code\":\"SUCCESS\",\"message\":\"success\"}");
    }

    private ResponseEntity<String> weChatFail(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"FAIL\",\"message\":\"" + escapeJson(message) + "\"}");
    }

    private int toFen(Double amount) {
        if (amount == null) {
            throw new IllegalArgumentException("order pay price is empty");
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
