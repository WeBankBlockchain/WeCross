package com.webank.wecross.stub.fabric.config;

import com.webank.wecross.exception.Status;
import com.webank.wecross.exception.WeCrossException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class FabricConfigUtils {

    private static Logger logger = LoggerFactory.getLogger(FabricConfigUtils.class);

    public static String getPath(String fileName) throws IOException {

        if (fileName.indexOf("classpath:") != 0) {
            return fileName;
        }
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Path path;
        path = Paths.get(resolver.getResource(fileName).getURI());
        logger.debug("relative path:{} absolute path:{}", fileName, path.toString());
        return path.toString();
    }

    public static FabricConfig getFabricService(
            String stubPath, Map<String, String> fabricServiceMaps) throws WeCrossException {

        FabricConfig fabricConfig = new FabricConfig();

        String channelName = fabricServiceMaps.get("channelName");
        if (channelName == null) {
            String errorMessage =
                    "\"channelName\" in [[fabricServices]] item  not found, please check "
                            + stubPath;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }
        fabricConfig.setChannelName(channelName);

        String orgName = fabricServiceMaps.get("orgName");
        if (orgName == null) {
            String errorMessage =
                    "\"orgName\" in [[fabricServices]] item  not found, please check " + stubPath;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }
        fabricConfig.setOrgName(orgName);

        String mspId = fabricServiceMaps.get("mspId");
        if (mspId == null) {
            String errorMessage =
                    "\"mspId\" in [[fabricServices]] item  not found, please check " + stubPath;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }
        fabricConfig.setMspId(mspId);

        String orgUserName = fabricServiceMaps.get("orgUserName");
        if (orgUserName == null) {
            String errorMessage =
                    "\"orgUserName\" in [[fabricServices]] item  not found, please check "
                            + stubPath;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }
        fabricConfig.setOrgUserName(orgUserName);

        String orgUserKeyFile = fabricServiceMaps.get("orgUserKeyFile");
        if (orgUserKeyFile == null) {
            String errorMessage =
                    "\"orgUserKeyFile\" in [[fabricServices]] item  not found, please check "
                            + stubPath;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }
        try {
            fabricConfig.setOrgUserKeyFile(getPath(orgUserKeyFile));
        } catch (IOException e) {
            String errorMessage =
                    "\"orgUserKeyFile\" in [[fabricServices]] can not find absolute path"
                            + orgUserKeyFile;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }

        String orgUserCertFile = fabricServiceMaps.get("orgUserCertFile");
        if (orgUserCertFile == null) {
            String errorMessage =
                    "\"orgUserCertFile\" in [[fabricServices]] item  not found, please check"
                            + stubPath;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }
        try {
            fabricConfig.setOrgUserCertFile(getPath(orgUserCertFile));
        } catch (IOException e) {
            String errorMessage =
                    "\"orgUserKeyFile\" in [[fabricServices]] can not find absolute path "
                            + orgUserCertFile;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }

        String ordererTlsCaFile = fabricServiceMaps.get("ordererTlsCaFile");
        if (ordererTlsCaFile == null) {
            String errorMessage =
                    "\"ordererTlsCaFile\" in [[fabricServices]] item  not found, please check "
                            + stubPath;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }
        try {
            fabricConfig.setOrdererTlsCaFile(getPath(ordererTlsCaFile));
        } catch (IOException e) {
            String errorMessage =
                    "\"orgUserKeyFile\" in [[fabricServices]] can not find absolute path "
                            + ordererTlsCaFile;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }

        String ordererAddress = fabricServiceMaps.get("ordererAddress");
        if (ordererAddress == null) {
            String errorMessage =
                    "\"ordererAddress\" in [[fabricServices]] item  not found, please check "
                            + stubPath;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }
        fabricConfig.setOrdererAddress(ordererAddress);

        return fabricConfig;
    }

    public static Map<String, FabricPeerConfig> getPeerConfig(
            String stubPath, Map<String, Map<String, String>> peersMaps) throws WeCrossException {
        Map<String, FabricPeerConfig> fabricMap = new HashMap<String, FabricPeerConfig>();
        for (Entry<String, Map<String, String>> entry : peersMaps.entrySet()) {
            FabricPeerConfig fabricConfig = new FabricPeerConfig();
            String key = entry.getKey();
            Map<String, String> value = entry.getValue();
            String peerTlsCaFile = value.get("peerTlsCaFile");
            if (peerTlsCaFile == null) {
                String errorMessage =
                        "\"peerTlsCaFile\" in [[peers]] item  not found, please check " + stubPath;
                throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
            }

            try {
                fabricConfig.setPeerTlsCaFile(getPath(peerTlsCaFile));
            } catch (IOException e) {
                String errorMessage =
                        "\"ordererAddress\" in [[fabricServices]] can not find absolute path "
                                + peerTlsCaFile;
                throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
            }

            String peerAddress = value.get("peerAddress");
            if (peerAddress == null) {
                String errorMessage =
                        "\"peerAddress\" in [[peers]] item  not found, please check " + stubPath;
                throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
            }
            fabricConfig.setPeerAddress(peerAddress);
            fabricMap.put(key, fabricConfig);
        }
        return fabricMap;
    }
}
