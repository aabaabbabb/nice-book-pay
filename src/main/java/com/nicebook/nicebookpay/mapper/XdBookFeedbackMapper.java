package com.nicebook.nicebookpay.mapper;

import com.nicebook.nicebookpay.entity.XdBookFeedback;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author Administrator
* @description 针对表【xd_book_feedback】的数据库操作Mapper
* @createDate 2026-03-11 21:13:54
* @Entity com.nicebook.nicebookpay.gender.domain.XdBookFeedback
*/
public interface XdBookFeedbackMapper extends BaseMapper<XdBookFeedback> {

    int insertFeedback(XdBookFeedback feedback);

    List<XdBookFeedback> selectByOrderId(String orderid);

}




