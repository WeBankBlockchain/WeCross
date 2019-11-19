package com.webank.wecross.stub.jdchain.config;

import com.webank.wecross.exception.Status;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.jdchain.JDChainStub;
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
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, String>> resources =
                (List<Map<String, String>>) stubConfig.get("resources");

        List<JDChainService> jdChainService =
                JDChainConfigUtils.getJDChainService(stubPath, jdChainServiceMaps);

        if (resources == null) {
            String warnMessage = "\"resources\" of jdchain stub not found: " + stubName;
            logger.warn(warnMessage);

            JDChainStubFactory jdChainStubFactory = new JDChainStubFactory();
            JDChainStub jdChainStub =
                    jdChainStubFactory.initJdChainStub(
                            networkName, stubName, stubPath, jdChainService, null);
            return jdChainStub;
        } else {
            JDChainStubFactory jdChainStubFactory = new JDChainStubFactory();
            JDChainStub jdChainStub =
                    jdChainStubFactory.initJdChainStub(
                            networkName, stubName, stubPath, jdChainService, resources);
            return jdChainStub;
        }
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
            throw new WeCrossException(Status.INTERNAL_ERROR, e.getMessage());
        }

        // init jdchain resources
        String prefix = networkName + "." + stubName;
        Map<String, Resource> jdChainResources =
                JDChainConfigUtils.getJdChainResources(prefix, stubPath, resources);
        jdChainStub.setResources(jdChainResources);

        return jdChainStub;
    }

    //    public JDChainStub initJdChainStubYmlVersion(
    //            String networkName,
    //            String stubName,
    //            List<JDChainService> jdChainService,
    //            Map<String, Map<String, String>> resources)
    //            throws WeCrossException {
    //        JDChainStub jdChainStub = new JDChainStub();
    //
    //        JDChainSdkFactory jdChainSdkFactory = new JDChainSdkFactory(jdChainService);
    //        JDChainSdk jdChainSdk = jdChainSdkFactory.getJdChainSdk();
    //
    //        try {
    //            // init jdchain stub
    //            jdChainStub.setAdminKey(jdChainSdk.getAdminKey());
    //            jdChainStub.setLedgerHash(jdChainSdk.getLedgerHash());
    //            jdChainStub.setBlockchainService(jdChainSdk.getBlockchainService());
    //        } catch (Exception e) {
    //            throw new WeCrossException(Status.INTERNAL_ERROR, e.getMessage());
    //        }
    //
    //        // init jdchain resources
    //        String prefix = networkName + "." + stubName;
    //        Map<String, Resource> jdChainResources = initJdChainResourcesYmlVersion(prefix,
    // resources);
    //        jdChainStub.setResources(jdChainResources);
    //
    //        return jdChainStub;
    //    }
    //
    //    public Map<String, Resource> initJdChainResourcesYmlVersion(
    //            String prefix, Map<String, Map<String, String>> resources) throws WeCrossException
    // {
    //        if (resources == null) {
    //            return null;
    //        }
    //
    //        Map<String, Resource> jdChainResources = new HashMap<>();
    //
    //        for (String resourceName : resources.keySet()) {
    //            Map<String, String> metaResource = resources.get(resourceName);
    //            if (!metaResource.containsKey("type")
    //                    || ((String) metaResource.get("type")).equals("")) {
    //                String errorMessage = "\"type\" of jdchain resource not found: " +
    // resourceName;
    //                throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
    //            }
    //            String type = metaResource.get("type");
    //            //  handle contract resource
    //            if (type.equalsIgnoreCase(ConfigInfo.RESOURCE_TYPE_JDCHAIN_CONTRACT)) {
    //                if (!metaResource.containsKey("contractAddress")
    //                        || ((String) metaResource.get("contractAddress")).equals("")) {
    //                    String errorMessage =
    //                            "\"contractAddress\" of jdchain contract resource not found: "
    //                                    + resourceName;
    //                    throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
    //                }
    //                JDChainContractResource jdChainContractResource = new
    // JDChainContractResource();
    //                String address = metaResource.get("contractAddress");
    //                jdChainContractResource.setContractAddress(address);
    //
    //                // set path
    //                String stringPath = prefix + "." + resourceName;
    //                String templateUrl = "http://127.0.0.1:8080/" + stringPath.replace('.', '/');
    //                try {
    //                    new URL(templateUrl);
    //                } catch (Exception e) {
    //                    throw new WeCrossException(
    //                            Status.ILLEGAL_SYMBOL, "Invalid path: " + stringPath);
    //                }
    //                try {
    //                    jdChainContractResource.setPath(Path.decode(stringPath));
    //                } catch (Exception e) {
    //                    throw new WeCrossException(Status.INTERNAL_ERROR, e.getMessage());
    //                }
    //
    //                jdChainResources.put(resourceName, jdChainContractResource);
    //
    //            } else if (type.equals("assets")) {
    //                // To be defined
    //                continue;
    //            } else {
    //                String errorMessage = "Undefined jdchain resource type: " + type;
    //                throw new WeCrossException(Status.UNEXPECTED_CONFIG, errorMessage);
    //            }
    //        }
    //        return jdChainResources;
    //    }
}
