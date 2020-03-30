package com.webank.wecross;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class Service {

    public static void main(String[] args) {
        SpringApplication.run(Service.class, args);
    }
}
