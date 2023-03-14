package com.comeon.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class ComeOnAppWebSocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComeOnAppWebSocketApplication.class, args);
    }

}
