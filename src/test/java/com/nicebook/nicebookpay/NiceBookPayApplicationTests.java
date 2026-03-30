package com.nicebook.nicebookpay;

import com.nicebook.nicebookpay.entity.XdBookFeedback;
import com.nicebook.nicebookpay.service.XdBookFeedbackService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.Date;

@SpringBootTest
class NiceBookPayApplicationTests {

    @Test
    void contextLoads() {
        recordFeedback();
    }

    @Autowired
    private XdBookFeedbackService bookFeedbackService;
    private void recordFeedback() {
        XdBookFeedback feedback = new XdBookFeedback();
        feedback.setCreateDatetime(new Date());
        long seconds = Instant.now().getEpochSecond();
        feedback.setCreateTime((int) seconds);
        feedback.setContent("测试");
        bookFeedbackService.insertFeedback(feedback);
    }

}
