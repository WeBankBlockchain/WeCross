package com.webank.wecross.test.restserver;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.webank.wecross.network.Network;

@Configuration
public class RestfulServiceTestConfig {
	@Bean(name= "produceNetworks")
    public Map<String, Network> getNetworks() {
    	Map<String, Network> networks = new HashMap<String, Network>();
    	
    	return networks;
    }
}
