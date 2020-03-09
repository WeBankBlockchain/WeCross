package com.webank.wecross.stub.fabric;

import com.webank.wecross.common.WeCrossDefault;
import com.webank.wecross.common.WeCrossType;
import com.webank.wecross.exception.ErrorCode;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.routine.htlc.AssetHTLCResource;
import com.webank.wecross.utils.ConfigUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricStubConfig {

    private static Logger logger = LoggerFactory.getLogger(FabricStubFactory.class);

    public FabricStub initFabricStub(
            String networkName,
            String stubName,
            FabricConfig fabricConfig,
            Map<String, FabricPeerConfig> fabricPeerMap,
            List<Map<String, Object>> resources)
            throws WeCrossException {

        if (resources == null) {
            return null;
        }
        FabricStub fabricStub = new FabricStub();
        Map<String, FabricConn> fabricConns = new HashMap<String, FabricConn>();
        for (Map<String, Object> resource : resources) {
            FabricConn fabricConn =
                    getFabricConfig(stubName, fabricConfig, fabricPeerMap, resource);
            fabricConns.put(fabricConn.getName(), fabricConn);
        }
        fabricStub.setFabricConns(fabricConns);
        String prefix = networkName + "." + stubName;
        Map<String, Resource> fabricResources = initFabricResources(stubName, prefix, resources);
        fabricStub.setResources(fabricResources);

        for (String name : fabricResources.keySet()) {
            Resource resource = fabricResources.get(name);
            String type = resource.getType();
            if (type.equalsIgnoreCase(WeCrossType.RESOURCE_TYPE_FABRIC_CONTRACT)) {
            	
                // String pathStr = resource.getPathAsString();
            	String pathStr = "TODO path";
                
                FabricConn fabricConn = fabricConns.get(name);
                if (fabricConn == null) {
                    logger.error("path:{} name:{} not exist in fabricConns", pathStr, name);
                    return null;
                }

                ((FabricContractResource) resource).init(fabricConn);
            } else if (type.equalsIgnoreCase(WeCrossType.RESOURCE_TYPE_ASSET_HTLC_CONTRACT)) {
               // String pathStr = resource.getPathAsString();
            	String pathStr = "TODO Path";
            	
                FabricConn fabricConn = fabricConns.get(name);
                if (fabricConn == null) {
                    logger.error("path:{} name:{} not exist in fabricConns", pathStr, name);
                    return null;
                }

                ((FabricContractResource) resource).init(fabricConn);
                AssetHTLCResource assetHtlcResource = new AssetHTLCResource(resource);
                fabricResources.put(name, assetHtlcResource);
            }
        }

        return fabricStub;
    }

    private FabricConn getFabricConfig(
            String stubName,
            FabricConfig fabricConfig,
            Map<String, FabricPeerConfig> fabricPeerMap,
            Map<String, Object> resource)
            throws WeCrossException {
        FabricConfig fabricConfigRet = fabricConfig;
        String name = (String) resource.get("name");
        if (name == null) {
            String errorMessage =
                    "\"name\" in [[resource]] item  not found, please check " + stubName;
            throw new WeCrossException(ErrorCode.FIELD_MISSING, errorMessage);
        }

        String type = (String) resource.get("type");
        if (type == null) {
            String errorMessage =
                    "\"type\" in [[resource]] item  not found, please check " + stubName;
            throw new WeCrossException(ErrorCode.FIELD_MISSING, errorMessage);
        }

        String chainCodeName = (String) resource.get("chainCodeName");
        if (chainCodeName == null) {
            String errorMessage =
                    "\"chainCodeName\" in [[resource]] item  not found, please check " + stubName;
            throw new WeCrossException(ErrorCode.FIELD_MISSING, errorMessage);
        }

        String chainLanguage = (String) resource.get("chainLanguage");
        if (chainLanguage == null) {
            String errorMessage =
                    "\"chainLanguage\" in [[resource]] item  not found, please check " + stubName;
            throw new WeCrossException(ErrorCode.FIELD_MISSING, errorMessage);
        }

        Long proposalWaitTimeTmp = (Long) resource.get("proposalWaitTime");
        long proposalWaitTime = WeCrossDefault.DEFAULT_PROPOSAL_WAIT_TIME;
        if (proposalWaitTimeTmp != null) {
            proposalWaitTime = proposalWaitTimeTmp.longValue();
        }

        @SuppressWarnings("unchecked")
        List<String> peerList = (List<String>) resource.get("peers");
        List<FabricPeerConfig> fabricPeerConfigs = new ArrayList<FabricPeerConfig>();
        for (int i = 0; i < peerList.size(); i++) {
            fabricPeerConfigs.add(fabricPeerMap.get(peerList.get(i)));
        }
        fabricConfigRet.setPeerConfigs(fabricPeerConfigs);

        FabricSdkConfig fabricSdkConfig = new FabricSdkConfig();
        fabricSdkConfig.setFabricConfig(fabricConfigRet);
        FabricSdk fabricSdk = fabricSdkConfig.initFabricStub();
        FabricConn fabricConn = new FabricConn();
        fabricConn.setChannel(fabricSdk.getChannel());
        fabricConn.setHfClient(fabricSdk.getHfClient());

        fabricConn.setName(name);
        fabricConn.setType(type);
        fabricConn.setChainCodeName(chainCodeName);
        fabricConn.setChainLanguage(chainLanguage);
        ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(chainCodeName).build();
        fabricConn.setChaincodeID(chaincodeID);
        fabricConn.setProposalWaitTime(proposalWaitTime);

        if (fabricConn.getChainLanguage().toLowerCase().equals("go")) {
            fabricConn.setChainCodeType(org.hyperledger.fabric.sdk.TransactionRequest.Type.GO_LANG);
        } else if (fabricConn.getChainLanguage().toLowerCase().equals("java")) {
            fabricConn.setChainCodeType(org.hyperledger.fabric.sdk.TransactionRequest.Type.JAVA);
        } else if (fabricConn.getChainLanguage().toLowerCase().equals("node")) {
            fabricConn.setChainCodeType(org.hyperledger.fabric.sdk.TransactionRequest.Type.NODE);
        } else {
            String errorMessage =
                    "\"chainLanguage\" in [[resource]] not support chaincode language "
                            + fabricConn.getChainLanguage();
            throw new WeCrossException(ErrorCode.FIELD_MISSING, errorMessage);
        }
        return fabricConn;
    }

    public Map<String, Resource> initFabricResources(
            String stubPath, String prefix, List<Map<String, Object>> resources)
            throws WeCrossException {
        if (resources == null) {
            return null;
        }
        Map<String, Resource> fabricResources = new HashMap<String, Resource>();
        for (Map<String, Object> resource : resources) {
            String resourceName = (String) resource.get("name");
            String type = (String) resource.get("type");
            FabricContractResource fabricContractResource = new FabricContractResource();
            fabricContractResource.setType(type);
            if (fabricResources.keySet().contains(resourceName)) {
                String errorMessage =
                        resourceName
                                + " in [[resources]] item  is repeated, please check "
                                + stubPath;
                throw new WeCrossException(ErrorCode.REPEATED_KEY, errorMessage);
            }
            // set path
            String stringPath = prefix + "." + resourceName;
            try {
                ConfigUtils.checkPath(stringPath);
                //fabricContractResource.setPath(Path.decode(stringPath)); TODO: set path
            } catch (WeCrossException e1) {
                throw e1;
            } catch (Exception e2) {
                throw new WeCrossException(ErrorCode.INTERNAL_ERROR, e2.getMessage());
            }
            fabricResources.put(resourceName, fabricContractResource);
        }
        return fabricResources;
    }
}
