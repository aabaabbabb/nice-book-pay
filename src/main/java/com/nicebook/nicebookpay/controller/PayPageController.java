package com.nicebook.nicebookpay.controller;

import com.nicebook.nicebookpay.entity.XdBookOrder;
import com.nicebook.nicebookpay.service.XdBookOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class PayPageController {

    @Autowired
    private XdBookOrderService orderService;

    @GetMapping("/pay/{orderid}")
    public String payPage(@PathVariable("orderid") String orderId, Model model) {
        XdBookOrder order = orderService.getByOrderId(orderId);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        model.addAttribute("order", order);
        model.addAttribute("orderId", orderId);
        return "index";
    }
}
