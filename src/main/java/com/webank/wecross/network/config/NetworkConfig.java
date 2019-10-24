package com.webank.wecross.network.config;

import com.webank.wecross.network.Network;
import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.bcos.BCOSStub;
import com.webank.wecross.stub.bcos.config.Account;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.ChannelService;
import com.webank.wecross.stub.jdchain.JDChainStub;
import com.webank.wecross.stub.jdchain.config.JDChainService;
import com.webank.wecross.stub.jdchain.config.JDChainStubConfig;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "network-manager")
public class NetworkConfig {

    private Logger logger = LoggerFactory.getLogger(NetworkConfig.class);

    private Map<String, NetworkUnit> networks;

    @Bean
    public Map<String, Network> initNetworks() {

        Map<String, Network> result = new HashMap<>();
        if (networks == null) {
            logger.info("no network configuration found");
            return result;
        }

        for (String networkName : networks.keySet()) {

            Network networkBean = new Network();

            NetworkUnit networkUnit = networks.get(networkName);

            Map<String, Object> stubs = networkUnit.getStubs();

            // get stubs bean
            Map<String, Stub> stubsBean = initStub(networkName, stubs);

            if (stubsBean != null) {
                // init network bean
                networkBean.setStubs(stubsBean);
                networkBean.setVisible(networkUnit.getVisible());

                result.put(networkName, networkBean);
            } else {
                logger.error("Init {} failed", networkName);
            }
        }

        return result;
    }

    public Map<String, Stub> initStub(String networkName, Map<String, Object> stubs) {
        Map<String, Stub> stubsBean = new HashMap<>();
        if (stubs == null) {
            logger.info("no stubs configuration found");
            return stubsBean;
        }

        for (String stubName : stubs.keySet()) {

            @SuppressWarnings("unchecked")
            Map<String, Object> stubConfig = (Map<String, Object>) stubs.get(stubName);

            if (!stubConfig.containsKey("pattern")) {
                logger.error(
                        "Error in application.yml: {} should contain a key named \"pattern\"",
                        stubName);
                return null;
            }

            String Stubtype = (String) stubConfig.get("pattern");

            if (Stubtype.equals("bcos")) {

                BCOSStub bcosStub = getBcosStub(networkName, stubName, stubConfig);
                if (bcosStub != null) {
                    stubsBean.put(stubName, bcosStub);
                } else {
                    logger.error(
                            "get bcos stub failed networkname:{} stubname:{},Stubtype:{}",
                            networkName,
                            stubName,
                            Stubtype);
                }

            } else if (Stubtype.equals("jdchain")) {

                JDChainStub jdChainStub = getJdStub(networkName, stubName, stubConfig);
                if (jdChainStub != null) {
                    stubsBean.put(stubName, jdChainStub);
                } else {
                    logger.error(
                            "get bcos stub failed networkname:{} stubname:{},Stubtype:{}",
                            networkName,
                            stubName,
                            Stubtype);
                }

            } else if (Stubtype.equals("BaiDu")) {
                // To be defined
                continue;

            } else {
                logger.info("Undefined stub type \"{}\" in {}", Stubtype, stubName);
            }
        }

        return stubsBean;
    }

    public BCOSStub getBcosStub(
            String networkName, String stubName, Map<String, Object> stubConfig) {
        if (!stubConfig.containsKey("accounts")) {
            logger.error(
                    "Error in application.yml: {} should contain a key named \"accounts\"",
                    stubName);
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, String> accountConfig = (Map<String, String>) stubConfig.get("accounts");
        Account account = ConfigUtils.getBcoseAccount(accountConfig);

        if (!stubConfig.containsKey("channelService")) {
            logger.error(
                    "Error in application.yml: {} should contain a key named \"channelService\"",
                    stubName);
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> channelServiceConfig =
                (Map<String, Object>) stubConfig.get("channelService");
        ChannelService channelService = ConfigUtils.getBcosChannelService(channelServiceConfig);

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
                bcosStubConfig.initBCOSStub(
                        networkName, stubName, account, channelService, bcosResources);
        return bcosStub;
    }

    public JDChainStub getJdStub(
            String networkName, String stubName, Map<String, Object> stubConfig) {
        if (!stubConfig.containsKey("jdService")) {
            logger.error(
                    "Error in application.yml: {} should contain a key named \"jdService\"",
                    stubName);
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> jdChainServiceConfig =
                (Map<String, Object>) stubConfig.get("jdService");
        JDChainService jdChainService = ConfigUtils.getJDChainService(jdChainServiceConfig);

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
                        networkName, stubName, jdChainService, jdChainResources);
        return jdChainStub;
    }

    public Map<String, NetworkUnit> getNetworks() {
        return networks;
    }

    public void setNetworks(Map<String, NetworkUnit> networks) {
        this.networks = networks;
    }
}
