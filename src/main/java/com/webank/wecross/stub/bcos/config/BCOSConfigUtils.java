package com.webank.wecross.stub.bcos.config;

import com.webank.wecross.config.ConfigInfo;
import com.webank.wecross.config.ConfigUtils;
import com.webank.wecross.exception.Status;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.bcos.BCOSContractResource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.fisco.bcos.channel.handler.ChannelConnections;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class BCOSConfigUtils {

    private static Logger logger = LoggerFactory.getLogger(BCOSConfigUtils.class);

    public static Account getBcosAccount(String stubPath, Map<String, String> accountConfig)
            throws WeCrossException {

        String accountFile = accountConfig.get("accountFile");
        if (accountFile == null) {
            String errorMessage =
                    "\"accountFile\" in [account] item  not found, please check " + stubPath;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }

        if (accountFile.contains(".pem")) {
            return new Account(accountFile, "");
        } else if (accountFile.contains(".p12")) {
            String password = accountConfig.get("password");
            if (password == null) {
                String errorMessage =
                        "\"password\" in [account] item  not found, please check " + stubPath;
                throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
            }
            return new Account(accountFile, password);
        } else {
            String errorMessage = "Unsupported account file";
            throw new WeCrossException(Status.UNEXPECTED_CONFIG, errorMessage);
        }
    }

    public static ChannelService getBcosChannelService(
            String stubPath, Map<String, Object> channelServiceConfig) throws WeCrossException {

        Integer timeout = ((Long) channelServiceConfig.get("timeout")).intValue();
        if (timeout == null) {
            timeout = 60 * 1000;
        }

        Integer groupId = ((Long) channelServiceConfig.get("groupId")).intValue();
        if (groupId == null) {
            String errorMessage =
                    "\"groupId\" in [channelService] item  not found, please check " + stubPath;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }

        String caCertPath = (String) channelServiceConfig.get("caCert");
        if (caCertPath == null) {
            String errorMessage =
                    "\"caCert\" in [channelService] item  not found, please check " + stubPath;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }

        String sslCertPath = (String) channelServiceConfig.get("sslCert");
        if (sslCertPath == null) {
            String errorMessage =
                    "\"sslCert\" in [channelService] item  not found, please check " + stubPath;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }

        String sslKeyPath = (String) channelServiceConfig.get("sslKey");
        if (sslKeyPath == null) {
            String errorMessage =
                    "\"sslKey\" in [channelService] item  not found, please check " + stubPath;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }

        @SuppressWarnings("unchecked")
        List<String> connectionsStr = (List<String>) channelServiceConfig.get("connectionsStr");
        if (sslKeyPath == null) {
            String errorMessage =
                    "\"connectionsStr\" in [channelService] item  not found, please check "
                            + stubPath;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }

        List<ChannelConnections> allChannelConnections = new ArrayList<>();
        ChannelConnections channelConnections = new ChannelConnections();
        channelConnections.setGroupId(groupId);
        channelConnections.setConnectionsStr(connectionsStr);
        allChannelConnections.add(channelConnections);
        logger.debug("Init GroupChannelConnections class finished");

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        GroupChannelConnections groupChannelConnections = new GroupChannelConnections();
        groupChannelConnections.setCaCert(resolver.getResource(caCertPath));
        groupChannelConnections.setSslCert(resolver.getResource(sslCertPath));
        groupChannelConnections.setSslKey(resolver.getResource(sslKeyPath));
        groupChannelConnections.setAllChannelConnections(allChannelConnections);
        logger.debug("Init ChannelService class finished");

        return new ChannelService(timeout, groupId, groupChannelConnections);
    }

    public static Map<String, Resource> getBcosResources(
            String prefix, String stubPath, List<Map<String, String>> resources, Web3j web3)
            throws WeCrossException {
        if (resources == null) {
            return null;
        }

        Map<String, Resource> bcosResources = new HashMap<>();

        for (Map<String, String> resource : resources) {

            String type = resource.get("type");
            if (type == null) {
                String errorMessage =
                        "\"type\" in [[resources]] item  not found, please check " + stubPath;
                throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
            }

            //  handle contract resource
            if (type.equalsIgnoreCase(ConfigInfo.RESOURCE_TYPE_BCOS_CONTRACT)) {
                String name = resource.get("name");
                if (name == null) {
                    String errorMessage =
                            "\"name\" in [[resources]] item  not found, please check " + stubPath;
                    throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
                }

                String contractAddress = resource.get("contractAddress");
                if (contractAddress == null) {
                    String errorMessage =
                            "\"contractAddress\" in [[resources]] item  not found, please check "
                                    + stubPath;
                    throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
                }

                BCOSContractResource bcosContractResource = new BCOSContractResource();
                bcosContractResource.setContractAddress(contractAddress);
                bcosContractResource.setWeb3(web3);

                // check and set
                String stringPath = prefix + "." + name;
                try {
                    ConfigUtils.checkPath(stringPath);
                    bcosContractResource.setPath(Path.decode(stringPath));
                } catch (WeCrossException we) {
                    throw we;
                } catch (Exception e) {
                    throw new WeCrossException(Status.INTERNAL_ERROR, e.getMessage());
                }

                bcosResources.put(name, bcosContractResource);

            } else if (type.equals("another")) {
                // To be defined
                continue;
            } else {
                String errorMessage = "Undefined bcos resource type: " + type;
                throw new WeCrossException(Status.UNEXPECTED_CONFIG, errorMessage);
            }
        }
        return bcosResources;
    }

    //    public static ChannelService getBcosChannelServiceYmlVersion(
    //        Map<String, Object> channelServiceConfig) throws WeCrossException {
    //        if (!channelServiceConfig.containsKey("groupId")) {
    //            String errorMessage = "\"groupId\" of channelService not found";
    //            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
    //        }
    //        if (!channelServiceConfig.containsKey("agencyName")
    //            || ((String) channelServiceConfig.get("agencyName")).equals("")) {
    //            String errorMessage = "\"agencyName\" of channelService not found";
    //            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
    //        }
    //        if (!channelServiceConfig.containsKey("groupChannelConnections")) {
    //            String errorMessage = "\"groupChannelConnections\" of channelService not found";
    //            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
    //        }
    //
    //        int groupId = (int) channelServiceConfig.get("groupId");
    //        String agencyName = (String) channelServiceConfig.get("agencyName");
    //
    //        @SuppressWarnings("unchecked")
    //        Map<String, Object> groupChannelConnectionsConfig =
    //            (Map<String, Object>) channelServiceConfig.get("groupChannelConnections");
    //        GroupChannelConnections groupChannelConnections =
    //            getBcosGroupChannelConnectionsYmlVersion(groupChannelConnectionsConfig);
    //
    //        logger.debug("Init ChannelService class finished");
    //        return new ChannelService(groupId, agencyName, groupChannelConnections);
    //    }
    //
    //    public static GroupChannelConnections getBcosGroupChannelConnectionsYmlVersion(
    //        Map<String, Object> groupChannelConnectionsConfig) throws WeCrossException {
    //        GroupChannelConnections groupChannelConnections = new GroupChannelConnections();
    //        if (!groupChannelConnectionsConfig.containsKey("caCert")
    //            || ((String) groupChannelConnectionsConfig.get("caCert")).equals("")) {
    //            String errorMessage = "\"caCert\" of GroupChannelConnections not found";
    //            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
    //        }
    //        if (!groupChannelConnectionsConfig.containsKey("sslCert")
    //            || ((String) groupChannelConnectionsConfig.get("sslCert")).equals("")) {
    //            String errorMessage = "\"sslCert\" of GroupChannelConnections not found";
    //            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
    //        }
    //        if (!groupChannelConnectionsConfig.containsKey("sslKey")
    //            || ((String) groupChannelConnectionsConfig.get("sslKey")).equals("")) {
    //            String errorMessage = "\"sslKey\" of GroupChannelConnections not found";
    //            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
    //        }
    //        if (!groupChannelConnectionsConfig.containsKey("allChannelConnections")) {
    //            String errorMessage = "\"allChannelConnections\" of GroupChannelConnections not
    // found";
    //            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
    //        }
    //
    //        String caCertPath = (String) groupChannelConnectionsConfig.get("caCert");
    //        String sslCertPath = (String) groupChannelConnectionsConfig.get("sslCert");
    //        String sslKeyPath = (String) groupChannelConnectionsConfig.get("sslKey");
    //
    //        PathMatchingResourcePatternResolver resolver = new
    // PathMatchingResourcePatternResolver();
    //
    //        groupChannelConnections.setCaCert(resolver.getResource(caCertPath));
    //        groupChannelConnections.setSslCert(resolver.getResource(sslCertPath));
    //        groupChannelConnections.setSslKey(resolver.getResource(sslKeyPath));
    //
    //        @SuppressWarnings("unchecked")
    //        Map<String, Object> allChannelConnectionsConfigMap =
    //            (Map<String, Object>) groupChannelConnectionsConfig.get("allChannelConnections");
    //
    //        List<Map<String, Object>> allChannelConnectionsConfig = new ArrayList<>();
    //        for (Entry<String, Object> entry : allChannelConnectionsConfigMap.entrySet()) {
    //            @SuppressWarnings("unchecked")
    //            Map<String, Object> allChannelConnectionsUnit = (Map<String, Object>)
    // entry.getValue();
    //            allChannelConnectionsConfig.add(allChannelConnectionsUnit);
    //        }
    //
    //        List<ChannelConnections> allChannelConnections =
    //            getBcosAllChannelConnectionsYmlVersion(allChannelConnectionsConfig);
    //
    //        groupChannelConnections.setAllChannelConnections(allChannelConnections);
    //        logger.debug("Init GroupChannelConnections class finished");
    //
    //        return groupChannelConnections;
    //    }
    //
    //    public static List<ChannelConnections> getBcosAllChannelConnectionsYmlVersion(
    //        List<Map<String, Object>> allChannelConnectionsConfig) throws WeCrossException {
    //        List<ChannelConnections> allChannelConnections = new ArrayList<>();
    //        for (Map<String, Object> channelConnectionsConfig : allChannelConnectionsConfig) {
    //            if (!channelConnectionsConfig.containsKey("groupId")) {
    //                String errorMessage = "\"groupId\" of ChannelConnections not found";
    //                throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
    //            }
    //            if (!channelConnectionsConfig.containsKey("connectionsStr")
    //                || channelConnectionsConfig.get("connectionsStr") == null) {
    //                String errorMessage = "\"connectionsStr\" of ChannelConnections not found";
    //                throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
    //            }
    //
    //            ChannelConnections channelConnections = new ChannelConnections();
    //            channelConnections.setGroupId((int) channelConnectionsConfig.get("groupId"));
    //
    //            @SuppressWarnings("unchecked")
    //            Map<String, String> connectionsStrMap =
    //                (Map<String, String>) channelConnectionsConfig.get("connectionsStr");
    //
    //            List<String> connectionsStr = new ArrayList<>();
    //            for (Entry<String, String> entry : connectionsStrMap.entrySet()) {
    //                String connectionsStrUnit = entry.getValue();
    //                connectionsStr.add(connectionsStrUnit);
    //            }
    //
    //            channelConnections.setConnectionsStr(connectionsStr);
    //
    //            allChannelConnections.add(channelConnections);
    //        }
    //
    //        logger.debug("Init allChannelConnections finished");
    //        return allChannelConnections;
    //    }
}
