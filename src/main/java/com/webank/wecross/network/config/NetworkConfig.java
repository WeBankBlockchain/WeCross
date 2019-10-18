package com.webank.wecross.network.config;

import com.webank.wecross.bcos.BCOSStub;
import com.webank.wecross.bcos.config.BCOSStubConfig;
import com.webank.wecross.bcos.config.Web3Sdk;
import com.webank.wecross.jdchain.JDChainStub;
import com.webank.wecross.jdchain.config.JDChainSdk;
import com.webank.wecross.jdchain.config.JDChainStubConfig;
import com.webank.wecross.network.Network;
import com.webank.wecross.stub.Stub;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@DependsOn(value = {"channelServiceConfig"})
@Configuration
@ConfigurationProperties(prefix = "network-manager")
public class NetworkConfig {

    private Logger logger = LoggerFactory.getLogger(NetworkConfig.class);

    private Map<String, NetworkUnit> networks;

    @Resource Map<String, Web3Sdk> web3SdkMap;

    @Resource Map<String, JDChainSdk> jdChainSdkMap;

    @Bean
    public Map<String, Network> initNetworks() {

        Map<String, Network> result = new HashMap<>();

        for (String networkName : networks.keySet()) {

            Network networkBean = new Network();

            NetworkUnit networkUnit = networks.get(networkName);

            Map<String, Map<String, Object>> stubs = networkUnit.getStubs();

            // get stubs bean
            Map<String, Stub> stubsBean = initStub(stubs);

            // init network bean
            networkBean.setStubs(stubsBean);
            networkBean.setVisible(networkUnit.getVisible());

            result.put(networkName, networkBean);
        }

        return result;
    }

    public Map<String, Stub> initStub(Map<String, Map<String, Object>> stubs) {
        Map<String, Stub> stubsBean = new HashMap<>();

        for (String stubName : stubs.keySet()) {

            Map<String, Object> stubConfig = stubs.get(stubName);

            if (!stubConfig.containsKey("pattern")) {
                logger.error(
                        "Error in application.yml: {} should contain a key named \"pattern\"",
                        stubName);
                return null;
            }

            String Stubtype = (String) stubConfig.get("pattern");

            if (Stubtype.equals("bcos")) {
                // init bcos hannel service
                if (!stubConfig.containsKey("bcosService")) {
                    logger.error(
                            "Error in application.yml: {} should contain a key named \"bcosService\"",
                            stubName);
                    return null;
                }
                String bcosService = (String) stubConfig.get("bcosService");

                if (!stubConfig.containsKey("resources")) {
                    logger.error(
                            "Error in application.yml: {} should contain a key named \"resources\"",
                            stubName);
                    return null;
                }

                // parse bcos resources
                @SuppressWarnings("unchecked")
                Map<String, Map<String, String>> bcosResources =
                        (Map<String, Map<String, String>>) stubConfig.get("resources");
                BCOSStubConfig bcosStubConfig = new BCOSStubConfig();
                BCOSStub bcosStub =
                        bcosStubConfig.initBCOSStub(web3SdkMap, bcosResources, bcosService);
                stubsBean.put(stubName, bcosStub);

            } else if (Stubtype.equals("jdchain")) {
                // To be defined
                if (!stubConfig.containsKey("jdService")) {
                    logger.error(
                            "Error in application.yml: {} should contain a key named \"jdService\"",
                            stubName);
                    return null;
                }
                String jdChainService = (String) stubConfig.get("jdService");
                if (!stubConfig.containsKey("resources")) {
                    logger.error(
                            "Error in application.yml: {} should contain a key named \"resources\"",
                            stubName);
                    return null;
                }
                @SuppressWarnings("unchecked")
                Map<String, Map<String, String>> jdChainResources =
                        (Map<String, Map<String, String>>) stubConfig.get("resources");
                JDChainStubConfig jdChainStubConfig = new JDChainStubConfig();
                JDChainStub jdChainStub =
                        jdChainStubConfig.initJdChainStub(
                                jdChainSdkMap, jdChainResources, jdChainService);
                stubsBean.put(stubName, jdChainStub);

            } else if (Stubtype.equals("BaiDu")) {
                // To be defined
                continue;

            } else {
                logger.info("Undefined stub type \"{}\" in {}", Stubtype, stubName);
            }
        }

        return stubsBean;
    }

    public Map<String, NetworkUnit> getNetworks() {
        return networks;
    }

    public void setNetworks(Map<String, NetworkUnit> networks) {
        this.networks = networks;
    }

    public Map<String, Web3Sdk> getWeb3SdkMap() {
        return web3SdkMap;
    }

    public void setWeb3SdkMap(Map<String, Web3Sdk> web3SdkMap) {
        this.web3SdkMap = web3SdkMap;
    }
}
