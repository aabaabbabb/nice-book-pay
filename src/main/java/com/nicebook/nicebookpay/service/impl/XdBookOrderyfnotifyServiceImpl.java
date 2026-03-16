package com.nicebook.nicebookpay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nicebook.nicebookpay.entity.XdBookOrderyfnotify;
import com.nicebook.nicebookpay.service.XdBookOrderyfnotifyService;
import com.nicebook.nicebookpay.mapper.XdBookOrderyfnotifyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【xd_book_orderyfnotify】的数据库操作Service实现
* @createDate 2026-03-16 21:26:10
*/
@Service
public class XdBookOrderyfnotifyServiceImpl extends ServiceImpl<XdBookOrderyfnotifyMapper, XdBookOrderyfnotify>
    implements XdBookOrderyfnotifyService{

    @Autowired
    XdBookOrderyfnotifyMapper orderyfnotifyMapper;

    @Override
    public void insertOrderyfnotify(XdBookOrderyfnotify orderyfnotify) {
        orderyfnotifyMapper.insertOrderyfnotify(orderyfnotify);
    }
}




