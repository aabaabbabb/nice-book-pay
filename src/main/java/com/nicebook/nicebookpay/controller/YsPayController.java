package com.nicebook.nicebookpay.controller;

import com.nicebook.nicebookpay.config.PayConstant;
import com.nicebook.nicebookpay.entity.XdBookFeedback;
import com.nicebook.nicebookpay.entity.XdBookOrder;
import com.nicebook.nicebookpay.service.XdBookFeedbackService;
import com.nicebook.nicebookpay.service.XdBookOrderService;
import com.nicebook.nicebookpay.service.XdBookYspayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

@Controller
@RequestMapping({"/api/yspay", "/yspay"})
public class YsPayController {

    @Autowired
    public XdBookYspayService srv;

    @Autowired
    private XdBookOrderService orderService;

    @Autowired
    private XdBookFeedbackService feedbackService;

    @GetMapping("/toPrepay/{orderid}")
    public String toPrepay(@PathVariable("orderid") String orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "yspay-view";
    }

    @GetMapping(value = "/raw/{orderid}", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> raw(@PathVariable("orderid") String orderId) {
        XdBookOrder order = orderService.getByOrderId(orderId);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.valueOf("text/plain;charset=UTF-8"))
                    .body("订单不存在");
        }
        try {
            String html = srv.createOrder(order);
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("text/html;charset=UTF-8"))
                    .body(html);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.valueOf("text/plain;charset=UTF-8"))
                    .body("银盛下单失败：" + ex.getMessage());
        }
    }

    @PostMapping("/notify")
    @ResponseBody
    public ResponseEntity<String> notify(HttpServletRequest request) {
        return handleNotify(request);
    }

    @PostMapping("/showNotify")
    @ResponseBody
    public ResponseEntity<String> showNotify(HttpServletRequest request) {
        return handleNotify(request);
    }

    @GetMapping("/showReturn")
    public String showReturn(HttpServletRequest request, Model model) {
        return handleReturn(request, model);
    }

    @GetMapping("/return")
    public String returnPage(HttpServletRequest request, Model model) {
        return handleReturn(request, model);
    }

    private ResponseEntity<String> handleNotify(HttpServletRequest request) {
        try {
            XdBookOrder order = handleSuccessPayment(request);
            if (order != null) {
                return plainText("success");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plainText("fail");
    }

    private String handleReturn(HttpServletRequest request, Model model) {
        XdBookOrder order = null;
        try {
            order = handleSuccessPayment(request);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (order == null) {
            String outTradeNo = request.getParameter("out_trade_no");
            if (outTradeNo != null && !outTradeNo.isBlank()) {
                order = orderService.getByOrderId(decode(outTradeNo));
            }
        }

        model.addAttribute("order", order);
        model.addAttribute("orderId", order == null ? null : order.getOrderid());
        return "result";
    }

    private XdBookOrder handleSuccessPayment(HttpServletRequest request) {
        String outTradeNo = decode(request.getParameter("out_trade_no"));
        String tradeNo = decode(request.getParameter("trade_no"));
        String tradeStatus = decode(request.getParameter("trade_status"));
        Double totalAmount = parseAmount(request.getParameter("total_amount"));

        XdBookOrder order = orderService.getByOrderId(outTradeNo);
        if (order == null) {
            return null;
        }

        if (Objects.equals(order.getPayState(), PayConstant.PAY_NO) && "TRADE_SUCCESS".equals(tradeStatus)) {
            order.setPaymentMethod(PayConstant.YS_PAY);
            order.setPaymentId(PayConstant.YS_PAY);
            order.setTransactionid(tradeNo);
            order.setPayState(PayConstant.PAY_YES);
            orderService.updateById(order);

            XdBookFeedback feedback = new XdBookFeedback();
            feedback.setCreateDatetime(new Date());
            feedback.setAid(3);
            feedback.setUName("客户");
            feedback.setContent("支付宝支付结果：成功；支付金额：" + totalAmount + "元");
            feedback.setOrderId(order.getOrderid());
            feedbackService.insertFeedback(feedback);

            return orderService.getById(order.getId());
        }

        return order;
    }

    private ResponseEntity<String> plainText(String body) {
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("text/plain;charset=UTF-8"))
                .body(body);
    }

    private String decode(String value) {
        if (value == null) {
            return null;
        }
        return new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }

    private Double parseAmount(String value) {
        if (value == null || value.isBlank()) {
            return 0D;
        }
        return Double.valueOf(value);
    }
}
