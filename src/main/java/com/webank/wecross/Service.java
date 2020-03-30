package com.webank.wecross;

import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class Service {

    public static void main(String[] args) {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);

        SpringApplication.run(Service.class, args);
    }
}
