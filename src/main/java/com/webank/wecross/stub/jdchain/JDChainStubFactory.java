package com.webank.wecross.stub.jdchain;

import com.webank.wecross.common.WeCrossType;
import com.webank.wecross.exception.ErrorCode;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.resource.Resource;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDChainStubFactory {

    private static Logger logger = LoggerFactory.getLogger(JDChainStubFactory.class);

    public static JDChainStub getJDChainStub(
            String networkName, String stubName, String stubPath, Map<String, Object> stubConfig)
            throws WeCrossException {

        @SuppressWarnings("unchecked")
        List<Map<String, String>> jdChainServiceMaps =
                (List<Map<String, String>>) stubConfig.get("jdServices");
        if (jdChainServiceMaps == null) {
            String errorMessage =
                    "Something wrong in [[jdServices]] item, please check " + stubPath;
            throw new WeCrossException(ErrorCode.FIELD_MISSING, errorMessage);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, String>> resources =
                (List<Map<String, String>>) stubConfig.get("resources");

        List<JDChainService> jdChainService =
                JDChainConfigUtils.getJDChainService(stubPath, jdChainServiceMaps);

        if (resources == null) {
            String warnMessage = "\"resources\" of jdchain stub not found: " + stubName;
            logger.warn(warnMessage);
        }

        JDChainStubFactory jdChainStubFactory = new JDChainStubFactory();
        JDChainStub jdChainStub =
                jdChainStubFactory.initJdChainStub(
                        networkName, stubName, stubPath, jdChainService, resources);
        return jdChainStub;
    }

    public JDChainStub initJdChainStub(
            String networkName,
            String stubName,
            String stubPath,
            List<JDChainService> jdChainService,
            List<Map<String, String>> resources)
            throws WeCrossException {
        JDChainStub jdChainStub = new JDChainStub();

        JDChainSdkFactory jdChainSdkFactory = new JDChainSdkFactory(jdChainService);
        JDChainSdk jdChainSdk = jdChainSdkFactory.getJdChainSdk();

        try {
            // init jdchain stub
            jdChainStub.setAdminKey(jdChainSdk.getAdminKey());
            jdChainStub.setLedgerHash(jdChainSdk.getLedgerHash());
            jdChainStub.setBlockchainService(jdChainSdk.getBlockchainService());
        } catch (Exception e) {
            throw new WeCrossException(ErrorCode.INTERNAL_ERROR, e.getMessage());
        }

        // init jdchain resources
        String prefix = networkName + "." + stubName;
        Map<String, Resource> jdChainResources =
                JDChainConfigUtils.getJdChainResources(prefix, stubPath, resources);
        jdChainStub.setResources(jdChainResources);

        for (Resource resource : jdChainResources.values()) {
            if (resource.getType().equals(WeCrossType.RESOURCE_TYPE_JDCHAIN_CONTRACT)) {
                ((JDChainResource) resource)
                        .init(
                                jdChainSdk.getAdminKey(),
                                jdChainSdk.getLedgerHash(),
                                jdChainSdk.getBlockchainService());
            }
        }

        return jdChainStub;
    }
}
