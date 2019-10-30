package com.webank.wecross.stub.jdchain.config;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.config.ConfigType;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.jdchain.JDChainContractResource;
import com.webank.wecross.stub.jdchain.JDChainStub;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDChainStubConfig {

    private Logger logger = LoggerFactory.getLogger(JDChainStubConfig.class);

    public JDChainStub initJdChainStub(
            String networkName,
            String stubName,
            JDChainService jdChainService,
            Map<String, Map<String, String>> resources)
            throws WeCrossException {
        JDChainStub jdChainStub = new JDChainStub();

        JDChainSdkConfig jdChainSdkConfig = new JDChainSdkConfig(jdChainService);
        JDChainSdk jdChainSdk = jdChainSdkConfig.getJdChainSdk();

<<<<<<< HEAD
        // init jdchain stub
        jdChainStub.setIsInit(isInit);
        jdChainStub.setPattern(pattern);

        try {
=======
        try {
            // init jdchain stub
>>>>>>> ac9e9c8aa665725642244ce45a6c2bfe06ebfcf8
            jdChainStub.setAdminKey(jdChainSdk.getAdminKey());
            jdChainStub.setLedgerHash(jdChainSdk.getLedgerHash());
            jdChainStub.setBlockchainService(jdChainSdk.getBlockchainService());
        } catch (Exception e) {
<<<<<<< HEAD
            return null;
=======
            throw new WeCrossException(1, e.getMessage());
>>>>>>> ac9e9c8aa665725642244ce45a6c2bfe06ebfcf8
        }

        // init bcos resources
        String prefix = networkName + "." + stubName;
        Map<String, Resource> jdChainResources = initJdChainResources(prefix, resources);
        jdChainStub.setResources(jdChainResources);

        return jdChainStub;
    }

    public Map<String, Resource> initJdChainResources(
            String prefix, Map<String, Map<String, String>> resources) throws WeCrossException {
        if (resources == null) {
            return null;
        }

        Map<String, Resource> jdChainResources = new HashMap<>();

        for (String resourceName : resources.keySet()) {
            Map<String, String> metaResource = resources.get(resourceName);
            if (!metaResource.containsKey("type")
                    || ((String) metaResource.get("type")).equals("")) {
                String errorMessage = "\"type\" of jdchain resource not found: " + resourceName;
                throw new WeCrossException(2, errorMessage);
            }
            String type = metaResource.get("type");
            //  handle contract resource
            if (type.equalsIgnoreCase(ConfigType.RESOURCE_TYPE_JDCHAIN_CONTRACT)) {
                if (!metaResource.containsKey("contractAddress")
                        || ((String) metaResource.get("contractAddress")).equals("")) {
                    String errorMessage =
                            "\"contractAddress\" of jdchain contract resource not found: "
                                    + resourceName;
                    throw new WeCrossException(2, errorMessage);
                }
                JDChainContractResource jdChainContractResource = new JDChainContractResource();
                String address = metaResource.get("contractAddress");
                jdChainContractResource.setContractAddress(address);

                // set path
                String stringPath = prefix + "." + resourceName;
                String templateUrl = "http://127.0.0.1:8080/" + stringPath.replace('.', '/');
                try {
                    new URL(templateUrl);
                } catch (Exception e) {
                    throw new WeCrossException(4, "Invalid path: " + stringPath);
                }
                try {
                    jdChainContractResource.setPath(Path.decode(stringPath));
                } catch (Exception e) {
                    throw new WeCrossException(1, e.getMessage());
                }

                jdChainResources.put(resourceName, jdChainContractResource);

            } else if (type.equals("assets")) {
                // To be defined
                continue;
            } else {
                String errorMessage = "Undefined jdchain resource type: " + type;
                throw new WeCrossException(3, errorMessage);
            }
        }
        return jdChainResources;
    }
}
