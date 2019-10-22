package com.webank.wecross.network.config;

import com.webank.wecross.stub.bcos.config.Account;
import com.webank.wecross.stub.bcos.config.ChannelService;
import com.webank.wecross.stub.bcos.config.GroupChannelConnections;
import com.webank.wecross.stub.jdchain.config.JDChainService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.fisco.bcos.channel.handler.ChannelConnections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class ConfigUtils {

    private static Logger logger = LoggerFactory.getLogger(ConfigUtils.class);

    public static Account getBcoseAccount(Map<String, String> accountConfig) {
        if (!accountConfig.containsKey("pemFile")) {
            logger.error(
                    "Error in application.yml: bcos accounts should contain a key named \"pemFile\"");
            return null;
        }
        if (!accountConfig.containsKey("p12File")) {
            logger.error(
                    "Error in application.yml: bcos accounts should contain a key named \"p12File\"");
            return null;
        }
        if (!accountConfig.containsKey("password")) {
            logger.error(
                    "Error in application.yml: bcos accounts should contain a key named \"password\"");
            return null;
        }
        return new Account(
                accountConfig.get("pemFile"),
                accountConfig.get("p12File"),
                accountConfig.get("password"));
    }

    public static ChannelService getBcosChannelService(Map<String, Object> channelServiceConfig) {
        if (!channelServiceConfig.containsKey("groupId")) {
            logger.error(
                    "Error in application.yml: bcos channelService should contain a key named \"groupId\"");
            return null;
        }
        if (!channelServiceConfig.containsKey("agencyName")) {
            logger.error(
                    "Error in application.yml: bcos channelService should contain a key named \"agencyName\"");
            return null;
        }
        if (!channelServiceConfig.containsKey("groupChannelConnections")) {
            logger.error(
                    "Error in application.yml: bcos channelService should contain a key named \"groupChannelConnections\"");
            return null;
        }

        int groupId = (int) channelServiceConfig.get("groupId");
        String agencyName = (String) channelServiceConfig.get("agencyName");

        @SuppressWarnings("unchecked")
        Map<String, Object> groupChannelConnectionsConfig =
                (Map<String, Object>) channelServiceConfig.get("groupChannelConnections");
        GroupChannelConnections groupChannelConnections =
                getBcosGroupChannelConnections(groupChannelConnectionsConfig);

        return new ChannelService(groupId, agencyName, groupChannelConnections);
    }

    public static GroupChannelConnections getBcosGroupChannelConnections(
            Map<String, Object> groupChannelConnectionsConfig) {
        GroupChannelConnections groupChannelConnections = new GroupChannelConnections();
        if (!groupChannelConnectionsConfig.containsKey("caCert")) {
            logger.error(
                    "Error in application.yml: bcos groupChannelConnections should contain a key named \"caCert\"");
            return null;
        }
        if (!groupChannelConnectionsConfig.containsKey("sslCert")) {
            logger.error(
                    "Error in application.yml: bcos groupChannelConnections should contain a key named \"sslCert\"");
            return null;
        }
        if (!groupChannelConnectionsConfig.containsKey("sslKey")) {
            logger.error(
                    "Error in application.yml: bcos groupChannelConnections should contain a key named \"sslKey\"");
            return null;
        }
        if (!groupChannelConnectionsConfig.containsKey("allChannelConnections")) {
            logger.error(
                    "Error in application.yml: bcos groupChannelConnections should contain a key named \"allChannelConnections\"");
            return null;
        }

        String caCertPath = (String) groupChannelConnectionsConfig.get("caCert");
        String sslCertPath = (String) groupChannelConnectionsConfig.get("sslCert");
        String sslKeyPath = (String) groupChannelConnectionsConfig.get("sslKey");

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        groupChannelConnections.setCaCert(resolver.getResource(caCertPath));
        groupChannelConnections.setSslCert(resolver.getResource(sslCertPath));
        groupChannelConnections.setSslKey(resolver.getResource(sslKeyPath));

        @SuppressWarnings("unchecked")
        Map<String, Object> allChannelConnectionsConfigMap =
                (Map<String, Object>) groupChannelConnectionsConfig.get("allChannelConnections");

        List<Map<String, Object>> allChannelConnectionsConfig = new ArrayList<>();
        for (Entry<String, Object> entry : allChannelConnectionsConfigMap.entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> allChannelConnectionsUnit = (Map<String, Object>) entry.getValue();
            allChannelConnectionsConfig.add(allChannelConnectionsUnit);
        }

        List<ChannelConnections> allChannelConnections =
                getBcosAllChannelConnections(allChannelConnectionsConfig);

        groupChannelConnections.setAllChannelConnections(allChannelConnections);
        return groupChannelConnections;
    }

    public static List<ChannelConnections> getBcosAllChannelConnections(
            List<Map<String, Object>> allChannelConnectionsConfig) {
        List<ChannelConnections> allChannelConnections = new ArrayList<>();
        for (Map<String, Object> channelConnectionsConfig : allChannelConnectionsConfig) {
            if (!channelConnectionsConfig.containsKey("groupId")) {
                logger.error(
                        "Error in application.yml: bcos allChannelConnections should contain a key named \"groupId\"");
                return null;
            }
            if (!channelConnectionsConfig.containsKey("connectionsStr")) {
                logger.error(
                        "Error in application.yml: bcos allChannelConnections should contain a key named \"connectionsStr\"");
                return null;
            }

            ChannelConnections channelConnections = new ChannelConnections();
            channelConnections.setGroupId((int) channelConnectionsConfig.get("groupId"));

            @SuppressWarnings("unchecked")
            Map<String, String> connectionsStrMap =
                    (Map<String, String>) channelConnectionsConfig.get("connectionsStr");

            List<String> connectionsStr = new ArrayList<>();
            for (Entry<String, String> entry : connectionsStrMap.entrySet()) {
                String connectionsStrUnit = entry.getValue();
                connectionsStr.add(connectionsStrUnit);
            }

            channelConnections.setConnectionsStr(connectionsStr);

            allChannelConnections.add(channelConnections);
        }
        return allChannelConnections;
    }

    public static JDChainService getJDChainService(Map<String, Object> jdChainServiceConfig) {
        if (!jdChainServiceConfig.containsKey("privateKey")) {
            logger.error(
                    "Error in application.yml: jdchain service should contain a key named \"privateKey\"");
            return null;
        }
        if (!jdChainServiceConfig.containsKey("publicKey")) {
            logger.error(
                    "Error in application.yml: jdchain service should contain a key named \"publicKey\"");
            return null;
        }
        if (!jdChainServiceConfig.containsKey("password")) {
            logger.error(
                    "Error in application.yml: jdchain service should contain a key named \"password\"");
            return null;
        }
        if (!jdChainServiceConfig.containsKey("connectionsStr")) {
            logger.error(
                    "Error in application.yml: jdchain service should contain a key named \"connectionsStr\"");
            return null;
        }

        String privateKey = (String) jdChainServiceConfig.get("privateKey");
        String publicKey = (String) jdChainServiceConfig.get("publicKey");
        String password = (String) jdChainServiceConfig.get("password");

        @SuppressWarnings("unchecked")
        Map<String, String> connectionsStrMap =
                (Map<String, String>) jdChainServiceConfig.get("connectionsStr");

        List<String> connectionsStr = new ArrayList<>();
        for (Entry<String, String> entry : connectionsStrMap.entrySet()) {
            String connectionsStrUnit = entry.getValue();
            connectionsStr.add(connectionsStrUnit);
        }

        return new JDChainService(privateKey, publicKey, password, connectionsStr);
    }
}
