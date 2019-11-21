package com.webank.wecross.stub.fabric.config;

import com.webank.wecross.exception.Status;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.fabric.FabricStub;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricStubFactory {
    private static Logger logger = LoggerFactory.getLogger(FabricStubFactory.class);

    public static FabricStub getFabricStub(
            String networkName, String stubName, String stubPath, Map<String, Object> stubConfig)
            throws WeCrossException {

        @SuppressWarnings("unchecked")
        Map<String, String> fabricServiceMaps =
                (Map<String, String>) stubConfig.get("fabricServices");
        if (fabricServiceMaps == null) {
            String errorMessage =
                    "Something wrong in [[fabricServices]] item, please check " + stubPath;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }
        FabricConfig fabricConfig = FabricConfigUtil.getFabricService(stubPath, fabricServiceMaps);

        @SuppressWarnings("unchecked")
        Map<String, Map<String, String>> peersMaps =
                (Map<String, Map<String, String>>) stubConfig.get("peers");
        if (peersMaps == null) {
            String errorMessage = "Something wrong in [[peers]] item, please check " + stubPath;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }

        Map<String, FabricPeerConfig> fabricPeerMap =
                FabricConfigUtil.getPeerConfig(stubPath, peersMaps);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> resources =
                (List<Map<String, Object>>) stubConfig.get("resources");
        if (resources == null) {
            String warnMessage = "\"resources\" of fabric stub not found: " + stubName;
            logger.warn(warnMessage);
        }
        FabricStubFactory fabricStubFactory = new FabricStubFactory();
        FabricStub fabricStub =
                fabricStubFactory.initFabricStub(
                        networkName, stubName, stubPath, fabricConfig, fabricPeerMap, resources);
        return fabricStub;
    }

    public FabricStub initFabricStub(
            String networkName,
            String stubName,
            String stubPath,
            FabricConfig fabricConfig,
            Map<String, FabricPeerConfig> fabricPeerMap,
            List<Map<String, Object>> resources)
            throws WeCrossException {
        FabricStubConfig fabricStubConfig = new FabricStubConfig();
        FabricStub fabricStub =
                fabricStubConfig.initFabricStub(
                        networkName, stubName, fabricConfig, fabricPeerMap, resources);
        return fabricStub;
    }
}
