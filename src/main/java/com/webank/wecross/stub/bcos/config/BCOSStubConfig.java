package com.webank.wecross.stub.bcos.config;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.config.ConfigType;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.bcos.BCOSContractResource;
import com.webank.wecross.stub.bcos.BCOSStub;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCOSStubConfig {

    private Logger logger = LoggerFactory.getLogger(BCOSStubConfig.class);

    public BCOSStub initBCOSStub(
            String networkName,
            String stubName,
            Account account,
            ChannelService channelService,
            Map<String, Map<String, String>> resources)
            throws WeCrossException {

        Credentials credentials = account.getCredentials();

        Web3SdkConfig web3SdkConfig = new Web3SdkConfig(credentials, channelService);
        Web3Sdk web3Sdk = web3SdkConfig.getWeb3Sdk(stubName);

        // init bcos stub
        BCOSStub bcosStub = new BCOSStub();
        bcosStub.setBcosService(web3Sdk.getBcosService());
        bcosStub.setWeb3(web3Sdk.getWeb3());
        bcosStub.setCredentials(web3Sdk.getCredentials());

        // init bcos resources
        String prefix = networkName + "." + stubName;
        Map<String, Resource> bcosResources = initBcosResources(prefix, resources);
        bcosStub.setResources(bcosResources);

        logger.debug("Init {}.{} finished", networkName, stubName);
        return bcosStub;
    }

    public Map<String, Resource> initBcosResources(
            String prefix, Map<String, Map<String, String>> resources) throws WeCrossException {
        if (resources == null) {
            return null;
        }

        Map<String, Resource> bcosResources = new HashMap<>();

        for (String resourceName : resources.keySet()) {
            // parse meta resource
            Map<String, String> metaResource = resources.get(resourceName);

            if (!metaResource.containsKey("type")
                    || ((String) metaResource.get("type")).equals("")) {
                String errorMessage = "\"type\" of bcos resource not found: " + resourceName;
                throw new WeCrossException(2, errorMessage);
            }

            String type = metaResource.get("type");

            //  handle contract resource
            if (type.equalsIgnoreCase(ConfigType.RESOURCE_TYPE_BCOS_CONTRACT)) {
                if (!metaResource.containsKey("contractAddress")
                        || ((String) metaResource.get("contractAddress")).equals("")) {
                    String errorMessage =
                            "\"contractAddress\" of bcos resource not found: " + resourceName;
                    throw new WeCrossException(2, errorMessage);
                }
                BCOSContractResource bcosContractResource = new BCOSContractResource();
                String address = metaResource.get("contractAddress");
                bcosContractResource.setContractAddress(address);

                // set path
                String stringPath = prefix + "." + resourceName;
                String templateUrl = "http://127.0.0.1:8080/" + stringPath.replace('.', '/');
                try {
                    new URL(templateUrl);
                } catch (Exception e) {
                    throw new WeCrossException(4, "Invalid path: " + stringPath);
                }
                try {
                    bcosContractResource.setPath(Path.decode(stringPath));
                } catch (Exception e) {
                    throw new WeCrossException(1, e.getMessage());
                }

                bcosResources.put(resourceName, bcosContractResource);

            } else if (type.equals("another")) {
                // To be defined
                continue;
            } else {
                String errorMessage = "Undefined bcos resource type: " + type;
                throw new WeCrossException(3, errorMessage);
            }
        }
        return bcosResources;
    }
}
