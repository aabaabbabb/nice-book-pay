package com.nicebook.nicebookpay.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class TestErrorController {

    @GetMapping("/test/404")
    public String test404() {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "测试404页面");
    }

    @GetMapping("/test/500")
    public String test500() {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "测试500页面");
    }
}
