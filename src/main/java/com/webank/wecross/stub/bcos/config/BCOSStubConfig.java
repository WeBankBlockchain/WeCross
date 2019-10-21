package com.webank.wecross.stub.bcos.config;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.bcos.BCOSContractResource;
import com.webank.wecross.stub.bcos.BCOSStub;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCOSStubConfig {

    private Logger logger = LoggerFactory.getLogger(BCOSStubConfig.class);

    private final Boolean isInit = false;

    private final String pattern = "bcos";

    public BCOSStub initBCOSStub(
            Map<String, Web3Sdk> web3SdkMap,
            Map<String, Map<String, String>> resources,
            String service) {
        if (!web3SdkMap.containsKey(service)) {
            logger.error(
                    "Error in application.yml: channelServices should contain a bcosService named {}",
                    service);
            return null;
        }
        Web3Sdk web3SDK = web3SdkMap.get(service);

        BCOSStub bcosStub = new BCOSStub();
        // init bcos stub
        bcosStub.setInit(isInit);
        bcosStub.setPattern(pattern);
        bcosStub.setBcosService(web3SDK.getBcosService());
        bcosStub.setWeb3(web3SDK.getWeb3());
        bcosStub.setCredentials(web3SDK.getCredentials());

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

        bcosStub.setResources(bcosResources);

        return bcosStub;
    }

    public Boolean getInit() {
        return isInit;
    }

    public String getPattern() {
        return pattern;
    }
}
