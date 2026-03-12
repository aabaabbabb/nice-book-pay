package com.nicebook.nicebookpay.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nicebook.nicebookpay.entity.XdBookOrder;

/**
* @author Administrator
* @description 针对表【xd_book_order】的数据库操作Mapper
* @createDate 2026-03-10 22:04:13
* @Entity com.nicebook.nicebookpay.gender.domain.XdBookOrder
*/
public interface XdBookOrderMapper extends BaseMapper<XdBookOrder> {
    XdBookOrder selectByOrderId(String orderid);
    XdBookOrder selectById (Integer id);
    int updateById(XdBookOrder order);
}




