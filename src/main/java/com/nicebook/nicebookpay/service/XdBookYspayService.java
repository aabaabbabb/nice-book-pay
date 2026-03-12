package com.nicebook.nicebookpay.service;

import com.nicebook.nicebookpay.entity.XdBookOrder;
import com.nicebook.nicebookpay.entity.XdBookYspay;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Administrator
* @description 针对表【xd_book_yspay】的数据库操作Service
* @createDate 2026-03-11 14:55:16
*/
public interface XdBookYspayService extends IService<XdBookYspay> {

    XdBookYspay selectById(Integer id);

    XdBookYspay selectByIsDefaultAndParentId(Integer isDefault,String partnerid);

    String createOrder(XdBookOrder xdBookOrder);
}
