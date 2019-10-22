package com.webank.wecross.stub.bcos.config;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.bcos.BCOSContractResource;
import com.webank.wecross.stub.bcos.BCOSStub;
import java.util.HashMap;
import java.util.Map;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCOSStubConfig {

    private Logger logger = LoggerFactory.getLogger(BCOSStubConfig.class);

    private final Boolean isInit = false;

    private final String pattern = "bcos";

    public BCOSStub initBCOSStub(
            String stubName,
            Account account,
            ChannelService channelService,
            Map<String, Map<String, String>> resources) {
        if (account == null) {
            logger.error("Error in {}: bcos configure is wrong", stubName);
            return null;
        }

        if (channelService == null) {
            logger.error("Error in {}: channelService configure is wrong", stubName);
            return null;
        }

        Credentials credentials = account.getCredentials("pem");

        Web3SdkConfig web3SdkConfig = new Web3SdkConfig(credentials, channelService);
        Web3Sdk web3Sdk = web3SdkConfig.getWeb3Sdk(stubName);

        // init bcos stub
        BCOSStub bcosStub = new BCOSStub();
        bcosStub.setInit(isInit);
        bcosStub.setPattern(pattern);
        bcosStub.setBcosService(web3Sdk.getBcosService());
        bcosStub.setWeb3(web3Sdk.getWeb3());
        bcosStub.setCredentials(web3Sdk.getCredentials());

        // init bcos resources
        Map<String, Resource> bcosResources = initBcosResources(resources);
        bcosStub.setResources(bcosResources);

        return bcosStub;
    }

    public Map<String, Resource> initBcosResources(Map<String, Map<String, String>> resources) {
        Map<String, Resource> bcosResources = new HashMap<>();

        for (String resourceName : resources.keySet()) {

            // parse meta resource
            Map<String, String> metaResource = resources.get(resourceName);

            if (!metaResource.containsKey("type")) {
                logger.error(
                        "Error in application.yml: {} should contain a key named \"type\"",
                        metaResource);
                return null;
            }

            String type = metaResource.get("type");

            //  handle contract resource
            if (type.equals("contract")) {
                if (!metaResource.containsKey("contractAddress")) {
                    logger.error(
                            "Error in application.yml: {} should contain a key named \"contractAddress\"",
                            resourceName);
                }
                BCOSContractResource bcosContractResource = new BCOSContractResource();
                String address = metaResource.get("contractAddress");
                bcosContractResource.setContractAddress(address);
                bcosResources.put(resourceName, bcosContractResource);

            } else if (type.equals("assets")) {
                // To be defined
                continue;
            } else {
                logger.info("Undefined type \"{}\" in {}", type, resourceName);
            }
        }
        return bcosResources;
    }

    public Boolean getInit() {
        return isInit;
    }

    public String getPattern() {
        return pattern;
    }
}
