package com.nicebook.nicebookpay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nicebook.nicebookpay.entity.XdBookFeedback;
import com.nicebook.nicebookpay.service.XdBookFeedbackService;
import com.nicebook.nicebookpay.mapper.XdBookFeedbackMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Administrator
* @description 针对表【xd_book_feedback】的数据库操作Service实现
* @createDate 2026-03-11 21:13:54
*/
@Service
public class XdBookFeedbackServiceImpl extends ServiceImpl<XdBookFeedbackMapper, XdBookFeedback>
    implements XdBookFeedbackService{

    @Override
    public void insertFeedback(XdBookFeedback feedback) {
        baseMapper.insertFeedback(feedback);
    }

    @Override
    public List<XdBookFeedback> selectByOrderId(String orderid) {
        return baseMapper.selectByOrderId(orderid);
    }

}




