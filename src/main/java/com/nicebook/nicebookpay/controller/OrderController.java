package com.nicebook.nicebookpay.controller;

import com.nicebook.nicebookpay.entity.XdBookOrder;
import com.nicebook.nicebookpay.service.XdBookOrderService;
import com.nicebook.nicebookpay.utils.Response;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


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
    public Response<XdBookOrder> getOrderByOderId(@PathVariable String orderid, HttpServletResponse response) throws Exception {

        XdBookOrder order = orderService.getByOrderId(orderid);

        if (order == null) {
            response.sendRedirect("https://www.tetuijiudian.com/home");
            return null;
//            return Response.fail(404, "订单不存在");
        }

        return Response.success(order);
    }


}
