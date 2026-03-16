package com.nicebook.nicebookpay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nicebook.nicebookpay.entity.XdBookOrderyfnotify;

/**
* @author Administrator
* @description 针对表【xd_book_orderyfnotify】的数据库操作Mapper
* @createDate 2026-03-16 21:26:10
* @Entity com.nicebook.nicebookpay.gender.domain.XdBookOrderyfnotify
*/
public interface XdBookOrderyfnotifyMapper extends BaseMapper<XdBookOrderyfnotify> {
    int insertOrderyfnotify(XdBookOrderyfnotify orderyfnotify);
}




