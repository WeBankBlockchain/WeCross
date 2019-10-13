package com.webank.wecross.bcos.config;

import org.fisco.bcos.web3j.crypto.Credentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountConfig {

    @Bean
    public Credentials getCredentials() {

        return Credentials.create("00000000000000000000000000000000000000000000000000000000000000");
    }
}
