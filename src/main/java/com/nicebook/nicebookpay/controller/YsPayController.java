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
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Objects;

@Controller
@RequestMapping("/api/yspay")
public class YsPayController {

    @Autowired
    public XdBookYspayService srv ;
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
        String html = srv.createOrder(order);
        html = html.replaceAll("// 加载懒加载的图片", "");
        return ResponseEntity.ok()
            .contentType(MediaType.valueOf("text/html;charset=UTF-8"))
            .body(html);
    }


    @PostMapping("/notify")
    @ResponseBody
    public ResponseEntity<String> showNotify(HttpServletRequest request) {
        try {
            String outTradeNo = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
            String tradeNo = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");
            String tradeStatus = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");

            XdBookOrder order = orderService.getByOrderId(outTradeNo);
            Double totalAmount = Double.valueOf(request.getParameter("total_amount"));

            if (order != null && Objects.equals(order.getPayState(), PayConstant.PAY_NO)) {

                if ("TRADE_SUCCESS".equals(tradeStatus)) {

                    order.setPaymentMethod(PayConstant.YS_PAY);
                    order.setTransactionid(tradeNo);
                    order.setPayState(PayConstant.PAY_YES);

                    orderService.updateById(order);

                    XdBookFeedback feedback = new XdBookFeedback();
                    feedback.setCreateDatetime(new Date());
                    feedback.setAid(3);
                    feedback.setUName("客户");
                    feedback.setContent("支付宝支付结果：成功;收款商户号,支付金额:" + totalAmount + "元");
                    feedback.setOrderId(order.getId().toString());

                    feedbackService.insertFeedback(feedback);

                    return ResponseEntity.ok()
                            .contentType(MediaType.valueOf("text/plain;charset=UTF-8"))
                            .body("success");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("text/plain;charset=UTF-8"))
                .body("fail");
    }

//    public void showReturn() {
//        try {
//            String out_trade_no = new String(getRequest().getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
//            String trade_no = new String(getRequest().getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");
//            String trade_status=new String(getRequest().getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");
//            Order order = osrv.getOrderInfoByOwn(out_trade_no);
//            Double total_amount= new Double(getRequest().getParameter("total_amount"));
//            if(order.getPaystate() == DragonConstant.PAY_NO) {
//                if(trade_status.equals("TRADE_SUCCESS")) {
//                    order.setPaymentMethod(DragonConstant.YS_PAY);
//                    order.setTransactionid(trade_no);
//                    order.setPaystate(DragonConstant.PAY_YES);
//                    order.update();
//                    fbsrv.doAddBackFeed("支付宝支付结果：成功;收款商户号,支付金额:" + total_amount + "元",
//                            order.getId());
//                    renderText("success");
//                    return;
//                }
//            }
//        } catch (UnsupportedEncodingException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        renderText("fail");
//    }


//    public void write(String res) {
//        try {
//            FileWriter writer = new FileWriter(PathKit.getRootClassPath()+"/yspay/info.log",true);
//            writer.write(res);
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

}
