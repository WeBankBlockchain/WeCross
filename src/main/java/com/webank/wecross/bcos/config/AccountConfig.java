package com.webank.wecross.bcos.config;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountConfig {

    private Logger logger = LoggerFactory.getLogger(AccountConfig.class);

    @Bean
    public Credentials getCredentials() {
        try {
            ECKeyPair keyPair = Keys.createEcKeyPair();
            Credentials credentials = Credentials.create(keyPair);
            return credentials;
        } catch (InvalidAlgorithmParameterException
                | NoSuchAlgorithmException
                | NoSuchProviderException e) {
            logger.error("create bcos keypair failed: {}", e.toString());
            return null;
        }
    }
}
