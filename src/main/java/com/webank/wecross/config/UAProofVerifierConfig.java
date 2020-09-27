package com.webank.wecross.config;

import com.webank.wecross.account.uaproof.UAProofVerifier;
import com.webank.wecross.stubmanager.StubManager;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UAProofVerifierConfig {
    @Resource StubManager stubManager;

    @Bean
    public UAProofVerifier newUAProofVerifier() {
        UAProofVerifier uaProofVerifier = new UAProofVerifier();
        uaProofVerifier.setStubManager(stubManager);
        return uaProofVerifier;
    }
}
