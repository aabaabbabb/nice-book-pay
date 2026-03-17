package com.nicebook.nicebookpay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nicebook.nicebookpay.entity.XdBookOrder;

/**
* @author Administrator
* @description 针对表【xd_book_order】的数据库操作Service
* @createDate 2026-03-10 22:04:13
*/
public interface XdBookOrderService extends IService<XdBookOrder> {

    XdBookOrder getByOrderId(String orderid);

    XdBookOrder getById(Integer id);
    boolean updateById(XdBookOrder order);
    int updatePaySuccess(String orderId, String transactionId);
}
