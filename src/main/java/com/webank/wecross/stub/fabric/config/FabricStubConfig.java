package com.webank.wecross.stub.fabric.config;

import com.webank.wecross.exception.Status;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.fabric.FabricContractResource;
import com.webank.wecross.stub.fabric.FabricStub;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class FabricStubConfig {

    public FabricStub initFabricStub(
            String networkName,
            String stubName,
            FabricConfig fabricConfig,
            Map<String, Map<String, String>> resources)
            throws WeCrossException {
        FabricSdkConfig fabricSdkConfig = new FabricSdkConfig();
        fabricSdkConfig.setFabricConfig(fabricConfig);
        FabricSdk fabricSdk = fabricSdkConfig.initFabricStub();
        FabricStub fabricStub = new FabricStub();
        fabricStub.setChannel(fabricSdk.getChannel());
        fabricStub.setHfClient(fabricSdk.getHfClient());

        String prefix = networkName + "." + stubName;
        Map<String, Resource> jdChainResources = initFabricResources(prefix, resources);
        fabricStub.setResources(jdChainResources);
        return fabricStub;
    }

    public Map<String, Resource> initFabricResources(
            String prefix, Map<String, Map<String, String>> resources) throws WeCrossException {
        if (resources == null) {
            return null;
        }
        Map<String, Resource> fabricResources = new HashMap<>();
        for (String resourceName : resources.keySet()) {
            FabricContractResource fabricContractResource = new FabricContractResource();
            fabricContractResource.setChainName("mycc");

            String stringPath = prefix + "." + resourceName;
            String templateUrl = "http://127.0.0.1:8080/" + stringPath.replace('.', '/');
            try {
                new URL(templateUrl);
            } catch (Exception e) {
                throw new WeCrossException(Status.ILLEGAL_SYMBOL, "Invalid path: " + stringPath);
            }
            try {
                fabricContractResource.setPath(Path.decode(stringPath));
            } catch (Exception e) {
                throw new WeCrossException(Status.INTERNAL_ERROR, e.getMessage());
            }

            fabricResources.put(resourceName, fabricContractResource);
        }
        return fabricResources;
    }
}
