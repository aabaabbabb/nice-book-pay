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
import org.springframework.http.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
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

    @Autowired
    private LockService lockService;

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
        log.info("锁定地址------"+target);
        return redirect(target);
    }

    @PostMapping("/notify")
    public ResponseEntity<String> notify( @RequestBody String body) {

        String traceId = UUID.randomUUID().toString();

        if (isBlank(body)) {
            return weChatFail("通知内容为空");
        }

        try {
            String decryptedBody = bookWeChatPayService.decryptOrder(body);
            JsonNode node = OBJECT_MAPPER.readTree(decryptedBody);
            String orderId = node.path("out_trade_no").asText();
            String transactionId = node.path("transaction_id").asText();
            int total = node.path("amount").path("payer_total").asInt();

            String lockKey = "pay:lock:" + orderId;

//            if (!lockService.tryLock(lockKey, traceId, 10)) {
//                log.warn("[{}] 重复回调", traceId);
//                return success();
//            }
//
//            try {
//                int updated = orderService.updatePaySuccess(orderId, transactionId);
//
//                if (updated == 0) {
//                    log.info("[{}] 已处理（幂等）", traceId);
//                    return success();
//                }
//
//                log.info("[{}] 支付成功 orderId={}", traceId, orderId);
//
//                return success();
//
//            } finally {
//                lockService.unlock(lockKey, traceId);
//            }

        } catch (Exception e) {
            log.error("[{}] 回调异常", traceId, e);
            return fail("系统异常");
        }
        return null;
    }

    private ResponseEntity<String> success() {
        return ResponseEntity.ok("{\"code\":\"SUCCESS\",\"message\":\"成功\"}");
    }

    private ResponseEntity<String> fail(String msg) {
        return ResponseEntity.ok("{\"code\":\"FAIL\",\"message\":\"" + msg + "\"}");
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
    @GetMapping("/finish")
    public String finish(@RequestParam("id") Integer id, Model model) {

        var order = orderService.getById(id);
        model.addAttribute("order", order);

        if (order != null && order.getPayState() == 1) {
            XdBookFeedback feedback = new XdBookFeedback();
            feedback.setCreateDatetime(new Date());
            feedback.setAid(3);
            feedback.setUName("客户");
            feedback.setContent("微信支付完成");
            feedback.setOrderId(order.getOrderid());
            bookFeedbackService.insertFeedback(feedback);
            return "result"; // 对应 result.html
        } else {
            return "index"; // 对应 index.html
        }
    }


    private final RestTemplate restTemplate = new RestTemplate();
    // 你的微信回调接口地址
    private final String notifyUrl = "http://localhost:8080/api/wechatpay/notify";

    /**
     * 模拟微信支付回调
     * @param outTradeNo 商户订单号
     * @param totalFee 支付金额（分）
     * @return 模拟 XML 和回调结果
     */
    @GetMapping(value = "/callback", produces = MediaType.APPLICATION_XML_VALUE)
    public String mockCallback(@RequestParam(defaultValue = "20260317123456") String outTradeNo,
                               @RequestParam(defaultValue = "100") int totalFee) {

        // 生成模拟微信回调 XML
        String xmlData = generateWeChatPayXml(outTradeNo, totalFee);
        log.info("模拟微信回调 XML:\n{}", xmlData);

        // 自动 POST 给你的回调接口
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            HttpEntity<String> entity = new HttpEntity<>(xmlData, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(notifyUrl, entity, String.class);
            log.info("回调接口响应: {}", response.getBody());
        } catch (Exception e) {
            log.error("自动调用回调接口失败", e);
        }

        // 返回生成的 XML 供查看
        return xmlData;
    }

    private String generateWeChatPayXml(String outTradeNo, int totalFee) {
        String appid = "wx352c7bd636c836bf";
        String attach = "pay";
        String bankType = "OTHERS";
        String feeType = "CNY";
        String isSubscribe = "N";
        String mchId = "1560775651";
        String openid = "ob_rswc_7qgCpBH3W8ZIYH4a0LoM";
        String resultCode = "SUCCESS";
        String tradeType = "MWEB";
        String transactionId = "420000249120" + System.currentTimeMillis();
        String nonceStr = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String timeEnd = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String sign = "FAKE_SIGN_FOR_TEST"; // 测试固定签名

        return """
                <xml>
                  <appid><![CDATA[%s]]></appid>
                  <attach><![CDATA[%s]]></attach>
                  <bank_type><![CDATA[%s]]></bank_type>
                  <cash_fee><![CDATA[%d]]></cash_fee>
                  <fee_type><![CDATA[%s]]></fee_type>
                  <is_subscribe><![CDATA[%s]]></is_subscribe>
                  <mch_id><![CDATA[%s]]></mch_id>
                  <nonce_str><![CDATA[%s]]></nonce_str>
                  <openid><![CDATA[%s]]></openid>
                  <out_trade_no><![CDATA[%s]]></out_trade_no>
                  <result_code><![CDATA[%s]]></result_code>
                  <return_code><![CDATA[SUCCESS]]></return_code>
                  <sign><![CDATA[%s]]></sign>
                  <time_end><![CDATA[%s]]></time_end>
                  <total_fee>%d</total_fee>
                  <trade_type><![CDATA[%s]]></trade_type>
                  <transaction_id><![CDATA[%s]]></transaction_id>
                </xml>
                """.formatted(
                appid, attach, bankType, totalFee, feeType, isSubscribe,
                mchId, nonceStr, openid, outTradeNo, resultCode,
                sign, timeEnd, totalFee, tradeType, transactionId
        );
    }

}
