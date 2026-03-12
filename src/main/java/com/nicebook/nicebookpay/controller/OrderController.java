package com.nicebook.nicebookpay.controller;

import com.nicebook.nicebookpay.entity.XdBookOrder;
import com.nicebook.nicebookpay.service.XdBookOrderService;
import com.nicebook.nicebookpay.utils.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @GetMapping("/error")
    public Response<String> error() {
        // 模拟运行时异常
        throw new RuntimeException("模拟运行时异常");
    }

    @Autowired
    private XdBookOrderService orderService;

    /**
     * 根据订单ID查询订单
     */
    @GetMapping("/{orderid}")
    public Response<XdBookOrder> getOrderByOderId(@PathVariable String orderid) {

        XdBookOrder order = orderService.getByOrderId(orderid);

        if (order == null) {
            return Response.fail(404, "订单不存在");
        }

        return Response.success(order);
    }


}
