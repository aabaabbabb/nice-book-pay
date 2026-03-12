package com.nicebook.nicebookpay.mapper;

import com.nicebook.nicebookpay.entity.XdBookWeChatPay;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author Administrator
* @description 针对表【xd_book_we_chat_pay】的数据库操作Mapper
* @createDate 2026-03-11 20:06:08
* @Entity com.nicebook.nicebookpay.gender.domain.XdBookWeChatPay
*/
public interface XdBookWeChatPayMapper extends BaseMapper<XdBookWeChatPay> {

    XdBookWeChatPay selectByISDefaultAndParentId(Integer isDefault, String partnerid);

}




