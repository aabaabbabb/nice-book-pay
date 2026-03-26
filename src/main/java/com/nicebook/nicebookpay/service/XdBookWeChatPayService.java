package com.nicebook.nicebookpay.service;

import com.nicebook.nicebookpay.entity.XdBookOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nicebook.nicebookpay.entity.XdBookPaymentMethods;

import java.util.Map;

/**
* @author Administrator
* @description 针对表【xd_book_we_chat_pay】的数据库操作Service
* @createDate 2026-03-11 20:06:08
*/
public interface XdBookWeChatPayService extends IService<XdBookPaymentMethods> {

    Map<String, String> createOrder(XdBookOrder order);

    String decryptOrder(String body);
}
