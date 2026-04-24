package com.nivedha.orderflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class OrderFlowEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderFlowEngineApplication.class, args);
    }
}
