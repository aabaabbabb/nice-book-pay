package com.nicebook.nicebookpay;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.nicebook.nicebookpay.mapper")
public class NiceBookPayApplication {

    public static void main(String[] args) {
        SpringApplication.run(NiceBookPayApplication.class, args);
    }

}
