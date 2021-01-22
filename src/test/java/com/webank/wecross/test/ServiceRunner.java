package com.webank.wecross.test;

import com.webank.wecross.Service;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class ServiceRunner {

    public static void main(String[] args) {
        Service.main(args);
    }
}
