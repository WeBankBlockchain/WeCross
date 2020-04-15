package com.webank.wecross;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class Service {

    public static void main(String[] args) {
        System.setProperty("jdk.tls.namedGroups", "secp256k1");
        SpringApplication.run(Service.class, args);
    }
}
