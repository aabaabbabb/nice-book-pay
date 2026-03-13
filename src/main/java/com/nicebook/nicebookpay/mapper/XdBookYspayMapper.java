package com.nicebook.nicebookpay.mapper;

import com.nicebook.nicebookpay.entity.XdBookYspay;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author Administrator
* @description 针对表【xd_book_yspay】的数据库操作Mapper
* @createDate 2026-03-11 14:55:16
* @Entity com.nicebook.nicebookpay.gender.domain.XdBookYspay
*/
public interface XdBookYspayMapper extends BaseMapper<XdBookYspay> {

    XdBookYspay selectById(Integer id);

    XdBookYspay selectByIsDefaultAndParentId(Integer isDefault,String partnerid);

    XdBookYspay selectDefaultOne(Integer isDefault);

}




