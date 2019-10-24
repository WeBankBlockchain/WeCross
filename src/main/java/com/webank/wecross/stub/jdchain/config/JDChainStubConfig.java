package com.webank.wecross.stub.jdchain.config;

import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.jdchain.JDChainContractResource;
import com.webank.wecross.stub.jdchain.JDChainStub;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDChainStubConfig {

    private Logger logger = LoggerFactory.getLogger(JDChainStubConfig.class);
    private final Boolean isInit = false;
    private final String pattern = "jdchain";

    public JDChainStub initJdChainStub(
            String networkName,
            String stubName,
            JDChainService jdChainService,
            Map<String, Map<String, String>> resources) {
        JDChainStub jdChainStub = new JDChainStub();

        if (jdChainService == null) {
            logger.error("Error in {}: jdChainService configure is wrong", stubName);
            return null;
        }

        JDChainSdkConfig jdChainSdkConfig = new JDChainSdkConfig(jdChainService);
        JDChainSdk jdChainSdk = jdChainSdkConfig.getJdChainSdk();

        // init jdchain stub
        jdChainStub.setIsInit(isInit);
        jdChainStub.setPattern(pattern);

        try {
            jdChainStub.setAdminKey(jdChainSdk.getAdminKey());
            jdChainStub.setLedgerHash(jdChainSdk.getLedgerHash());
            jdChainStub.setBlockchainService(jdChainSdk.getBlockchainService());
        } catch (Exception e) {
            return null;
        }

        // init bcos resources
        String prefix = networkName + "." + stubName;
        Map<String, Resource> jdChainResources = initJdChainResources(prefix, resources);
        jdChainStub.setResources(jdChainResources);

        return jdChainStub;
    }

    public Map<String, Resource> initJdChainResources(
            String prefix, Map<String, Map<String, String>> resources) {
        Map<String, Resource> jdChainResources = new HashMap<>();

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
            if (type.equals("JD_CONTRACT")) {
                if (!metaResource.containsKey("contractAddress")) {
                    logger.error(
                            "Error in application.yml: {} should contain a key named \"contractAddress\"",
                            resourceName);
                }
                JDChainContractResource jdChainContractResource = new JDChainContractResource();
                String address = metaResource.get("contractAddress");
                jdChainContractResource.setContractAddress(address);

                // set path
                String stringPath = prefix + "." + resourceName;
                try {
                    jdChainContractResource.setPath(Path.decode(stringPath));
                } catch (Exception e) {
                    logger.error(e.toString());
                    continue;
                }

                if (jdChainContractResource != null) {
                    jdChainResources.put(resourceName, jdChainContractResource);
                } else {
                    logger.error("Init JDChainContractResource failed");
                }

            } else if (type.equals("assets")) {
                // To be defined
                continue;
            } else {
                logger.info("Undefined type \"{}\" in {}", type, resourceName);
            }
        }
        return jdChainResources;
    }
}
