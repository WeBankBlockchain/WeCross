package com.webank.wecross;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class Service {

    private static final Logger logger = LoggerFactory.getLogger(Service.class);

    public static void main(String[] args) {
        try {
            SpringApplication.run(Service.class, args);
        } catch (Exception e) {
            logger.error("main, e: ", e);
            System.exit(-1);
        }
    }
}
