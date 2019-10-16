package com.webank.wecross.jdchain.config;

import com.webank.wecross.jdchain.JdChainContractResource;
import com.webank.wecross.jdchain.JdChainResource;
import com.webank.wecross.jdchain.JdChainStub;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDChainStubConfig {
    private Logger logger = LoggerFactory.getLogger(JDChainStubConfig.class);
    private final Boolean isInit = false;
    private final String pattern = "jdchain";

    public JdChainStub initJdChainStub(
            Map<String, jdChainSdk> jdChainSdkMap,
            Map<String, Map<String, String>> resources,
            String service) {
        JdChainStub jdChainStub = new JdChainStub();
        if (!jdChainSdkMap.containsKey(service)) {
            logger.error(
                    "Error in application.yml: services should contain a jdService named {}",
                    service);
            return null;
        }
        jdChainSdk sdk = jdChainSdkMap.get(service);
        jdChainStub.setIsInit(isInit);
        jdChainStub.setPattern(pattern);
        jdChainStub.setAdminKey(sdk.getAdminKey());
        jdChainStub.setLedgerHash(sdk.getLedgerHash());
        jdChainStub.setBlockchainService(sdk.getBlockchainService());

        Map<String, JdChainResource> jdChainResources = new HashMap<>();

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
                JdChainContractResource jcChainContractResource = new JdChainContractResource();
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
