package com.nicebook.nicebookpay.service;

import com.nicebook.nicebookpay.entity.XdBookOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nicebook.nicebookpay.entity.XdBookPaymentMethods;

/**
* @author Administrator
* @description 针对表【xd_book_yspay】的数据库操作Service
* @createDate 2026-03-11 14:55:16
*/
public interface XdBookYspayService extends IService<XdBookPaymentMethods>{

    String createOrder(XdBookOrder xdBookOrder);
}
