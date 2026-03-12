package com.nicebook.nicebookpay.controller;

import com.alibaba.fastjson.JSONObject;
import com.nicebook.nicebookpay.entity.XdBookFeedback;
import com.nicebook.nicebookpay.entity.XdBookOrder;
import com.nicebook.nicebookpay.service.XdBookFeedbackService;
import com.nicebook.nicebookpay.service.XdBookOrderService;
import com.nicebook.nicebookpay.service.XdBookWeChatPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/wechatpay")
public class WeChatController {

    private static final int PAY_STATE_READY = 2;
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
        recordFeedback(order);

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

    private ResponseEntity<String> redirect(String url) {
        if (isBlank(url)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("重定向地址为空");
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, url)
                .build();
    }

    private void recordFeedback(XdBookOrder order) {
        String orderId = resolveOrderId(order);
        if (isBlank(orderId)) {
            return;
        }
        XdBookFeedback feedback = new XdBookFeedback();
        feedback.setCreateDatetime(new Date());
        feedback.setAid(FEEDBACK_USER_ID);
        feedback.setUName(FEEDBACK_USER_NAME);
        feedback.setContent(FEEDBACK_CONTENT);
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

//    public void showNotify() {
//
//        Map<String, String> result = new HashMap<String, String>();
//        result.put("code", "FAIL");
//        try {
//            BufferedReader br = getRequest().getReader();
//            String str = null;
//            StringBuilder builder = new StringBuilder();
//            while ((str = br.readLine()) != null) {
//                builder.append(str);
//            }
//            String body = wcsrv.decryptOrder(builder.toString());
//            JSONObject node = JSONObject.parseObject(body);
//
//            String out_trade_no = node.getString("out_trade_no");
//            Order order = osrv.getOrderById(Integer.parseInt(out_trade_no));
//            if (order != null) {
//
//                String transaction_id = node.getString("transaction_id");
//
//                JSONObject amount = node.getJSONObject("amount");
//                int payer_total = amount.getInteger("payer_total");
//
//                int out_total = (int) (order.getPayprice() * 100);
//
//                if (out_total == payer_total) {
//                    order.setTransactionid(transaction_id);
//                    order.setPaystate(DragonConstant.PAY_YES);
//                    order.setPaymentMethod(DragonConstant.WEIXIN_PAY);
//                    order.update();
//                    result.put("message", "成功");
//                    result.put("code", "SUCCESS");
//                    fbsrv.doAddBackFeed("微信支付结果：成功;收款商户号" + order.getMchId() + ",支付金额:" + payer_total / 100 + "元",
//                            order.getId());
//                    redirect("/" + order.getGuid());
//                } else {
//                    fbsrv.doAddBackFeed("微信支付结果：支付金额错误,支付金额:" + payer_total / 100 + "元", order.getId());
//                    result.put("message", "支付金额错误");
//                }
//            } else {
//                fbsrv.doAddBackFeed("微信支付结果：订单号错误", order.getId());
//                result.put("message", "订单号错误");
//            }
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        renderJson(result);
//    }
//
//    /**
//     * 完成支付
//     */
//    public void finish() {
//        Integer id = getParaToInt();
//        Order order = osrv.getOrderById(id);
//        set("order", order);
//        if (order.getPaystate() == DragonConstant.PAY_YES) {
//            fbsrv.doAddBackFeed("微信支付完成", order.getId());
//            render("result.html");
//        } else {
//            render("index.html");
//        }
//    }

}
