package com.nicebook.nicebookpay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nicebook.nicebookpay.entity.XdBookOrderyfnotify;

/**
* @author Administrator
* @description 针对表【xd_book_orderyfnotify】的数据库操作Service
* @createDate 2026-03-16 21:26:10
*/
public interface XdBookOrderyfnotifyService extends IService<XdBookOrderyfnotify> {
    void insertOrderyfnotify(XdBookOrderyfnotify orderyfnotify);
}
