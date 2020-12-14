package com.webank.wecross.config;

import com.webank.wecross.account.uaproof.UAProofGenerator;
import com.webank.wecross.stubmanager.StubManager;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UAProofGeneratorConfig {
    @Resource StubManager stubManager;

    @Bean
    public UAProofGenerator newUAProofGenerator() {
        UAProofGenerator uaProofGenerator = new UAProofGenerator();
        uaProofGenerator.setStubManager(stubManager);
        return uaProofGenerator;
    }
}
