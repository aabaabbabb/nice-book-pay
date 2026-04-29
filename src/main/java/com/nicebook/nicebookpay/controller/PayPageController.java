package com.nicebook.nicebookpay.controller;

import com.nicebook.nicebookpay.entity.XdBookFeedback;
import com.nicebook.nicebookpay.entity.XdBookOrder;
import com.nicebook.nicebookpay.service.XdBookFeedbackService;
import com.nicebook.nicebookpay.service.XdBookOrderService;
import com.nicebook.nicebookpay.utils.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Date;

@Controller
public class PayPageController {

    @Autowired
    private XdBookOrderService orderService;

    @Autowired
    private XdBookFeedbackService feedbackService;

    @GetMapping("/pay/{orderid}")
    public String payPage(@PathVariable("orderid") String orderId, Model model) {
        XdBookOrder order = orderService.getByOrderId(orderId);
        if (order == null) {
            return "index";
        }
        XdBookFeedback feedback = new XdBookFeedback();
        feedback.setCreateDatetime(new Date());
        long seconds = Instant.now().getEpochSecond();
        feedback.setCreateTime((int) seconds);
        feedback.setAid(3);
        feedback.setUName("客户");
        feedback.setContent("跳转到首页");
        feedback.setOrderId(order.getOrderid());
        feedbackService.insertFeedback(feedback);

        model.addAttribute("order", order);
        model.addAttribute("orderId", orderId);
        return "index";
    }
}
