package com.nicebook.nicebookpay.service;

import com.nicebook.nicebookpay.entity.XdBookFeedback;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Administrator
* @description 针对表【xd_book_feedback】的数据库操作Service
* @createDate 2026-03-11 21:13:54
*/
public interface XdBookFeedbackService extends IService<XdBookFeedback> {

    int insertFeedback(XdBookFeedback feedback);

    List<XdBookFeedback> selectByOrderId(String orderid);

}
