package com.webank.wecross.jdchain.config;

import com.webank.wecross.jdchain.JDChainContractResource;
import com.webank.wecross.jdchain.JDChainResource;
import com.webank.wecross.jdchain.JDChainStub;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDChainStubConfig {
    private Logger logger = LoggerFactory.getLogger(JDChainStubConfig.class);
    private final Boolean isInit = false;
    private final String pattern = "jdchain";

    public JDChainStub initJdChainStub(
            Map<String, JDChainSdk> jdChainSdkMap,
            Map<String, Map<String, String>> resources,
            String service) {
        JDChainStub jdChainStub = new JDChainStub();
        if (!jdChainSdkMap.containsKey(service)) {
            logger.error(
                    "Error in application.yml: services should contain a jdService named {}",
                    service);
            return null;
        }
        JDChainSdk sdk = jdChainSdkMap.get(service);
        jdChainStub.setIsInit(isInit);
        jdChainStub.setPattern(pattern);
        jdChainStub.setAdminKey(sdk.getAdminKey());
        jdChainStub.setLedgerHash(sdk.getLedgerHash());
        jdChainStub.setBlockchainService(sdk.getBlockchainService());

        Map<String, JDChainResource> jdChainResources = new HashMap<>();

        for (String resourceName : resources.keySet()) {
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
                JDChainContractResource jcChainContractResource = new JDChainContractResource();
                String address = metaResource.get("contractAddress");
                jcChainContractResource.setContractAddress(address);
                jdChainResources.put(resourceName, jcChainContractResource);

            } else if (type.equals("assets")) {
                // To be defined
                continue;
            } else {
                logger.info("Undefined type \"{}\" in {}", type, resourceName);
            }
        }
        jdChainStub.setResources(jdChainResources);

        return jdChainStub;
    }
}
