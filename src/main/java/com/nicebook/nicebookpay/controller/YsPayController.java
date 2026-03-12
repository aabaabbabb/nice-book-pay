package com.nicebook.nicebookpay.controller;

import com.nicebook.nicebookpay.entity.XdBookOrder;
import com.nicebook.nicebookpay.service.XdBookOrderService;
import com.nicebook.nicebookpay.service.XdBookYspayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/yspay")
public class YsPayController {

    @Autowired
    public XdBookYspayService srv ;
    @Autowired
    private XdBookOrderService orderService;

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
        String html = srv.createOrder(order);
        return ResponseEntity.ok()
            .contentType(MediaType.valueOf("text/html;charset=UTF-8"))
            .body(html);
    }
}
