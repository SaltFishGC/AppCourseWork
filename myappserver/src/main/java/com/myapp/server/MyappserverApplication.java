package com.myapp.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.myapp.server.mapper")
public class MyappserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyappserverApplication.class, args);
    }

}
