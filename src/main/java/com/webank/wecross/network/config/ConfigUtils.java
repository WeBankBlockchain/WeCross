package com.webank.wecross.network.config;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.bcos.config.Account;
import com.webank.wecross.stub.bcos.config.ChannelService;
import com.webank.wecross.stub.bcos.config.GroupChannelConnections;
import com.webank.wecross.stub.jdchain.config.JDChainService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.fisco.bcos.channel.handler.ChannelConnections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class ConfigUtils {

    private static Logger logger = LoggerFactory.getLogger(ConfigUtils.class);

    public static Account getBcosAccount(Map<String, String> accountConfig)
            throws WeCrossException {
        if (!accountConfig.containsKey("accountFile")
                || ((String) accountConfig.get("accountFile")).equals("")) {
            String errorMessage = "\"accountFile\" of account not found";
            throw new WeCrossException(2, errorMessage);
        }

        String accountFile = accountConfig.get("accountFile");

        if (accountFile.contains(".pem")) {
            return new Account(accountFile, "");
        } else if (accountFile.contains(".p12")) {
            if (!accountConfig.containsKey("password")
                    || ((String) accountConfig.get("password")).equals("")) {
                String errorMessage = "\"password\" of account not found";
                throw new WeCrossException(2, errorMessage);
            }
            return new Account(accountFile, accountConfig.get("password"));
        } else {
            String errorMessage = "Unsupported account file";
            throw new WeCrossException(3, errorMessage);
        }
    }

    public static ChannelService getBcosChannelService(Map<String, Object> channelServiceConfig)
            throws WeCrossException {
        if (!channelServiceConfig.containsKey("groupId")) {
            String errorMessage = "\"groupId\" of channelService not found";
            throw new WeCrossException(2, errorMessage);
        }
        if (!channelServiceConfig.containsKey("agencyName")
                || ((String) channelServiceConfig.get("agencyName")).equals("")) {
            String errorMessage = "\"agencyName\" of channelService not found";
            throw new WeCrossException(2, errorMessage);
        }
        if (!channelServiceConfig.containsKey("groupChannelConnections")
                || channelServiceConfig.get("groupChannelConnections") == null) {
            String errorMessage = "\"groupChannelConnections\" of channelService not found";
            throw new WeCrossException(2, errorMessage);
        }

        int groupId = (int) channelServiceConfig.get("groupId");
        String agencyName = (String) channelServiceConfig.get("agencyName");

        @SuppressWarnings("unchecked")
        Map<String, Object> groupChannelConnectionsConfig =
                (Map<String, Object>) channelServiceConfig.get("groupChannelConnections");
        GroupChannelConnections groupChannelConnections =
                getBcosGroupChannelConnections(groupChannelConnectionsConfig);

        logger.debug("Init ChannelService class finished");
        return new ChannelService(groupId, agencyName, groupChannelConnections);
    }

    public static GroupChannelConnections getBcosGroupChannelConnections(
            Map<String, Object> groupChannelConnectionsConfig) throws WeCrossException {
        GroupChannelConnections groupChannelConnections = new GroupChannelConnections();
        if (!groupChannelConnectionsConfig.containsKey("caCert")
                || ((String) groupChannelConnectionsConfig.get("caCert")).equals("")) {
            String errorMessage = "\"caCert\" of GroupChannelConnections not found";
            throw new WeCrossException(2, errorMessage);
        }
        if (!groupChannelConnectionsConfig.containsKey("sslCert")
                || ((String) groupChannelConnectionsConfig.get("sslCert")).equals("")) {
            String errorMessage = "\"sslCert\" of GroupChannelConnections not found";
            throw new WeCrossException(2, errorMessage);
        }
        if (!groupChannelConnectionsConfig.containsKey("sslKey")
                || ((String) groupChannelConnectionsConfig.get("sslKey")).equals("")) {
            String errorMessage = "\"sslKey\" of GroupChannelConnections not found";
            throw new WeCrossException(2, errorMessage);
        }
        if (!groupChannelConnectionsConfig.containsKey("allChannelConnections")) {
            String errorMessage = "\"allChannelConnections\" of GroupChannelConnections not found";
            throw new WeCrossException(2, errorMessage);
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
        logger.debug("Init GroupChannelConnections class finished");

        return groupChannelConnections;
    }

    public static List<ChannelConnections> getBcosAllChannelConnections(
            List<Map<String, Object>> allChannelConnectionsConfig) throws WeCrossException {
        List<ChannelConnections> allChannelConnections = new ArrayList<>();
        for (Map<String, Object> channelConnectionsConfig : allChannelConnectionsConfig) {
            if (!channelConnectionsConfig.containsKey("groupId")) {
                String errorMessage = "\"groupId\" of ChannelConnections not found";
                throw new WeCrossException(2, errorMessage);
            }
            if (!channelConnectionsConfig.containsKey("connectionsStr")
                    || channelConnectionsConfig.get("connectionsStr") == null) {
                String errorMessage = "\"connectionsStr\" of ChannelConnections not found";
                throw new WeCrossException(2, errorMessage);
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

        logger.debug("Init allChannelConnections finished");
        return allChannelConnections;
    }

    public static List<JDChainService> getJDChainService(Map<String, Object> jdChainServiceConfig)
            throws WeCrossException {

        List<JDChainService> jDChainServiceList = new ArrayList<JDChainService>();
        Iterator<String> iterator = jdChainServiceConfig.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            @SuppressWarnings("unchecked")
            Map<String, Object> jdChainConfig = (Map<String, Object>) jdChainServiceConfig.get(key);
            if (!jdChainConfig.containsKey("privateKey")
                    || ((String) jdChainConfig.get("privateKey")).equals("")) {
                String errorMessage = "\"privateKey\" of jdService not found";
                throw new WeCrossException(2, errorMessage);
            }
            if (!jdChainConfig.containsKey("publicKey")
                    || ((String) jdChainConfig.get("publicKey")).equals("")) {
                String errorMessage = "\"publicKey\" of jdService not found";
                throw new WeCrossException(2, errorMessage);
            }
            if (!jdChainConfig.containsKey("password")
                    || ((String) jdChainConfig.get("password")).equals("")) {
                String errorMessage = "\"password\" of jdService not found";
                throw new WeCrossException(2, errorMessage);
            }
            if (!jdChainConfig.containsKey("connectionsStr")
                    || ((String) jdChainConfig.get("connectionsStr")).equals("")) {
                String errorMessage = "\"connectionsStr\" of jdService not found";
                throw new WeCrossException(2, errorMessage);
            }
            String privateKey = (String) jdChainConfig.get("privateKey");
            String publicKey = (String) jdChainConfig.get("publicKey");
            String password = (String) jdChainConfig.get("password");
            String connectionsStr = (String) jdChainConfig.get("connectionsStr");
            jDChainServiceList.add(
                    new JDChainService(privateKey, publicKey, password, connectionsStr));
        }
        return jDChainServiceList;
    }
}
