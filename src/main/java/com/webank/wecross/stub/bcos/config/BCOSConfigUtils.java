package com.webank.wecross.stub.bcos.config;

import static com.webank.wecross.utils.ConfigUtils.fileIsExists;

import com.webank.wecross.common.WeCrossDefault;
import com.webank.wecross.common.WeCrossType;
import com.webank.wecross.exception.ErrorCode;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.bcos.BCOSContractResource;
import com.webank.wecross.utils.ConfigUtils;
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
            throw new WeCrossException(ErrorCode.FIELD_MISSING, errorMessage);
        }

        if (accountFile.contains(".pem")) {
            return new Account(accountFile, "");
        } else if (accountFile.contains(".p12")) {
            String password = accountConfig.get("password");
            if (password == null) {
                String errorMessage =
                        "\"password\" in [account] item  not found, please check " + stubPath;
                throw new WeCrossException(ErrorCode.FIELD_MISSING, errorMessage);
            }
            return new Account(accountFile, password);
        } else {
            String errorMessage = "Unsupported account file";
            throw new WeCrossException(ErrorCode.UNEXPECTED_CONFIG, errorMessage);
        }
    }

    public static ChannelService getBcosChannelService(
            String stubPath, Map<String, Object> channelServiceConfig) throws WeCrossException {

        Long timeout_temp = (Long) channelServiceConfig.get("timeout");
        Integer timeout;
        if (timeout_temp != null) {
            timeout = timeout_temp.intValue();
        } else {
            timeout = WeCrossDefault.DEFAULT_TIME_OUT;
        }

        Long groupId_temp = (Long) channelServiceConfig.get("groupId");
        Integer groupId;
        if (groupId_temp != null) {
            groupId = groupId_temp.intValue();
        } else {
            String errorMessage =
                    "\"groupId\" in [channelService] item  not found, please check " + stubPath;
            throw new WeCrossException(ErrorCode.FIELD_MISSING, errorMessage);
        }

        String caCertPath = (String) channelServiceConfig.get("caCert");
        if (caCertPath == null) {
            String errorMessage =
                    "\"caCert\" in [channelService] item  not found, please check " + stubPath;
            throw new WeCrossException(ErrorCode.FIELD_MISSING, errorMessage);
        }
        if (!fileIsExists(caCertPath)) {
            String errorMessage = "File: " + caCertPath + " is not exists";
            throw new WeCrossException(ErrorCode.DIR_NOT_EXISTS, errorMessage);
        }

        String sslCertPath = (String) channelServiceConfig.get("sslCert");
        if (sslCertPath == null) {
            String errorMessage =
                    "\"sslCert\" in [channelService] item  not found, please check " + stubPath;
            throw new WeCrossException(ErrorCode.FIELD_MISSING, errorMessage);
        }
        if (!fileIsExists(sslCertPath)) {
            String errorMessage = "File: " + sslCertPath + " is not exists";
            throw new WeCrossException(ErrorCode.DIR_NOT_EXISTS, errorMessage);
        }

        String sslKeyPath = (String) channelServiceConfig.get("sslKey");
        if (sslKeyPath == null) {
            String errorMessage =
                    "\"sslKey\" in [channelService] item  not found, please check " + stubPath;
            throw new WeCrossException(ErrorCode.FIELD_MISSING, errorMessage);
        }
        if (!fileIsExists(sslKeyPath)) {
            String errorMessage = "File: " + sslKeyPath + " is not exists";
            throw new WeCrossException(ErrorCode.DIR_NOT_EXISTS, errorMessage);
        }

        @SuppressWarnings("unchecked")
        List<String> connectionsStr = (List<String>) channelServiceConfig.get("connectionsStr");
        if (connectionsStr == null) {
            String errorMessage =
                    "\"connectionsStr\" in [channelService] item  not found, please check "
                            + stubPath;
            throw new WeCrossException(ErrorCode.FIELD_MISSING, errorMessage);
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
                throw new WeCrossException(ErrorCode.FIELD_MISSING, errorMessage);
            }

            //  handle contract resource
            if (type.equalsIgnoreCase(WeCrossType.RESOURCE_TYPE_BCOS_CONTRACT)) {
                String name = resource.get("name");
                if (name == null) {
                    String errorMessage =
                            "\"name\" in [[resources]] item  not found, please check " + stubPath;
                    throw new WeCrossException(ErrorCode.FIELD_MISSING, errorMessage);
                }

                if (bcosResources.keySet().contains(name)) {
                    String errorMessage =
                            name + " in [[resources]] item  is repeated, please check " + stubPath;
                    throw new WeCrossException(ErrorCode.REPEATED_KEY, errorMessage);
                }

                String contractAddress = resource.get("contractAddress");
                if (contractAddress == null) {
                    String errorMessage =
                            "\"contractAddress\" in [[resources]] item  not found, please check "
                                    + stubPath;
                    throw new WeCrossException(ErrorCode.FIELD_MISSING, errorMessage);
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
                    throw new WeCrossException(ErrorCode.INTERNAL_ERROR, e.getMessage());
                }

                bcosResources.put(name, bcosContractResource);

            } else if (type.equals("another")) {
                // To be defined
                continue;
            } else {
                String errorMessage = "Undefined bcos resource type: " + type;
                throw new WeCrossException(ErrorCode.UNEXPECTED_CONFIG, errorMessage);
            }
        }
        return bcosResources;
    }
}
