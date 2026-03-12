package com.nicebook.nicebookpay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nicebook.nicebookpay.entity.XdBookOrder;
import com.nicebook.nicebookpay.mapper.XdBookOrderMapper;
import com.nicebook.nicebookpay.service.XdBookOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【xd_book_order】的数据库操作Service实现
* @createDate 2026-03-10 22:04:13
*/
@Service
public class XdBookOrderServiceImpl extends ServiceImpl<XdBookOrderMapper, XdBookOrder>
    implements XdBookOrderService {
    @Autowired
    private XdBookOrderMapper bookOrderMapper;

    @Override
    public XdBookOrder getByOrderId(String orderid) {
        return bookOrderMapper.selectByOrderId(orderid);
    }

    @Override
    public XdBookOrder getById(Integer id) {
        return bookOrderMapper.selectById(id);
    }

    @Override
    public boolean updateById(XdBookOrder order) {
        if (order == null || order.getId() == null) {
            return false;
        }
        return bookOrderMapper.updateById(order) > 0;
    }
}
