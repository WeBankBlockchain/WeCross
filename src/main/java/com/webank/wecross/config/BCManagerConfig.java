package com.webank.wecross.config;

import com.webank.wecross.common.BCManager;
import java.security.Provider;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BCManagerConfig {

    @Bean
    public BCManager newBCManager() {
        BCManager bcManager = new BCManager();
        Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        System.out.println("Pre-Initializing bouncycastle provider");
        if (provider == null) {
            System.out.println("Initializing bouncycastle provider");
            bcManager.setBouncyCastleProvider(new BouncyCastleProvider());
            Security.addProvider(bcManager.getBouncyCastleProvider());
        } else {
            bcManager.setBouncyCastleProvider(provider);
        }
        return bcManager;
    }
}
