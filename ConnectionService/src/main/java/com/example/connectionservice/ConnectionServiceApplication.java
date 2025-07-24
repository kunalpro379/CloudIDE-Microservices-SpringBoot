package com.example.connectionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
public class ConnectionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConnectionServiceApplication.class, args);
    }
}
