package com.webank.wecross.config;

import static com.webank.wecross.utils.ConfigUtils.fileIsExists;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.common.WeCrossDefault;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.p2p.netty.factory.P2PConfig;
import com.webank.wecross.network.rpc.netty.RPCConfig;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
public class ConfigReaderConfig {
    private Logger logger = LoggerFactory.getLogger(ConfigReaderConfig.class);

    @Resource Toml toml;

    @Bean
    public RPCConfig newRPCConfig() throws WeCrossException {
        System.out.println("Initializing RPCConfig ...");

        logger.info("Initializing rpc config...");

        RPCConfig rpcConfig = null;
        try {
            Map<String, Object> wecrossMap = toml.toMap();

            @SuppressWarnings("unchecked")
            Map<String, Object> rpcMap = (Map<String, Object>) wecrossMap.get("rpc");
            if (rpcMap == null) {
                String errorMessage =
                        "Something wrong in [rpc] item, please check "
                                + WeCrossDefault.MAIN_CONFIG_FILE;
                throw new WeCrossException(WeCrossException.ErrorCode.FIELD_MISSING, errorMessage);
            }
            rpcConfig = parseRPCConfig(rpcMap);

        } catch (WeCrossException e) {
            logger.error(e.getMessage());
            System.exit(1);
        }
        return rpcConfig;
    }

    @Bean
    public P2PConfig newP2PConfig() throws WeCrossException {
        System.out.println("Initializing P2PConfig ...");

        logger.info("Initializing p2p config...");

        P2PConfig p2PConfig = null;
        try {
            Map<String, Object> wecrossMap = toml.toMap();

            @SuppressWarnings("unchecked")
            Map<String, Object> p2pMap = (Map<String, Object>) wecrossMap.get("p2p");
            if (p2pMap == null) {
                String errorMessage =
                        "Something wrong in [p2p] item, please check "
                                + WeCrossDefault.MAIN_CONFIG_FILE;
                throw new WeCrossException(WeCrossException.ErrorCode.FIELD_MISSING, errorMessage);
            }
            p2PConfig = parseP2PConfig(p2pMap);

        } catch (WeCrossException e) {
            logger.error(e.getMessage());
            System.exit(1);
        }
        return p2PConfig;
    }

    public P2PConfig parseP2PConfig(Map<String, Object> p2pMap) throws WeCrossException {
        P2PConfig p2PConfig = new P2PConfig();

        String listenIP = (String) p2pMap.get("listenIP");
        if (listenIP == null) {
            String errorMessage =
                    "\"listenIP\" in [p2p] item  not found, please check "
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            throw new WeCrossException(WeCrossException.ErrorCode.FIELD_MISSING, errorMessage);
        }

        @SuppressWarnings("unchecked")
        List<String> peers = (List<String>) p2pMap.get("peers");
        if (peers == null) {
            String errorMessage =
                    "\"peers\" in [p2p] item  not found, please check "
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            throw new WeCrossException(WeCrossException.ErrorCode.FIELD_MISSING, errorMessage);
        }

        Long listenPort_temp = (Long) p2pMap.get("listenPort");
        Integer listenPort;
        if (listenPort_temp != null) {
            listenPort = listenPort_temp.intValue();
        } else {
            String errorMessage =
                    "\"listenPort\" in [p2p] item  not found, please check "
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            throw new WeCrossException(WeCrossException.ErrorCode.FIELD_MISSING, errorMessage);
        }

        String caCertPath = (String) p2pMap.get("caCert");
        if (!fileIsExists(caCertPath)) {
            String errorMessage = "File: " + caCertPath + " is not exists";
            throw new WeCrossException(WeCrossException.ErrorCode.DIR_NOT_EXISTS, errorMessage);
        }

        String sslCertPath = (String) p2pMap.get("sslCert");
        if (sslCertPath == null) {
            String errorMessage =
                    "\"sslCert\" in [p2p] item  not found, please check "
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            throw new WeCrossException(WeCrossException.ErrorCode.FIELD_MISSING, errorMessage);
        }
        if (!fileIsExists(sslCertPath)) {
            String errorMessage = "File: " + sslCertPath + " is not exists";
            throw new WeCrossException(WeCrossException.ErrorCode.DIR_NOT_EXISTS, errorMessage);
        }

        String sslKeyPath = (String) p2pMap.get("sslKey");
        if (sslKeyPath == null) {
            String errorMessage =
                    "\"sslKey\" in [p2p] item  not found, please check "
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            throw new WeCrossException(WeCrossException.ErrorCode.FIELD_MISSING, errorMessage);
        }
        if (!fileIsExists(sslKeyPath)) {
            String errorMessage = "File: " + sslKeyPath + " is not exists";
            throw new WeCrossException(WeCrossException.ErrorCode.DIR_NOT_EXISTS, errorMessage);
        }

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        Long threadNum = (Long) p2pMap.get("threadNum");
        if (threadNum == null) {
            threadNum = new Long(8);
            logger.info("threadNum not set, use default: {}", threadNum);
        }

        Long threadQueueCapacity = (Long) p2pMap.get("threadQueueCapacity");
        if (threadQueueCapacity == null) {
            threadQueueCapacity = new Long(10000);
            logger.info("threadQueueCapacity not set, use default: {}", threadQueueCapacity);
        }

        p2PConfig.setCaCert(resolver.getResource(caCertPath));
        p2PConfig.setSslCert(resolver.getResource(sslCertPath));
        p2PConfig.setSslKey(resolver.getResource(sslKeyPath));
        p2PConfig.setListenIP(listenIP);
        p2PConfig.setListenPort(listenPort);
        p2PConfig.setPeers(peers);
        p2PConfig.setThreadNum(threadNum);
        p2PConfig.setThreadQueueCapacity(threadQueueCapacity);

        return p2PConfig;
    }

    public RPCConfig parseRPCConfig(Map<String, Object> rpcMap) throws WeCrossException {
        RPCConfig rpcConfig = new RPCConfig();

        String listenIP = (String) rpcMap.get("address");
        if (listenIP == null) {
            String errorMessage =
                    "\"address\" in [rpc] item  not found, please check "
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            throw new WeCrossException(WeCrossException.ErrorCode.FIELD_MISSING, errorMessage);
        }

        Long listenPort_temp = (Long) rpcMap.get("port");
        Integer listenPort;
        if (listenPort_temp != null) {
            listenPort = listenPort_temp.intValue();
        } else {
            String errorMessage =
                    "\"port\" in [rpc] item  not found, please check "
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            throw new WeCrossException(WeCrossException.ErrorCode.FIELD_MISSING, errorMessage);
        }

        Long threadNum = (Long) rpcMap.get("threadNum");
        if (threadNum == null) {
            threadNum = new Long(16);
            logger.info("rpc threadNum not set, use default: {}", threadNum);
        }
        rpcConfig.setThreadNum(threadNum);

        Long threadQueueCapacity = (Long) rpcMap.get("threadQueueCapacity");
        if (threadQueueCapacity == null) {
            threadQueueCapacity = new Long(10000);
            logger.info("rpc threadQueueCapacity not set, use default: {}", threadQueueCapacity);
        }
        rpcConfig.setThreadQueueCapacity(threadQueueCapacity);

        Long sslSwitch = (Long) rpcMap.get("sslSwitch");
        if (sslSwitch == null) {
            sslSwitch = Long.valueOf(RPCConfig.SSLSwitch.SSL_ON_CLIENT_AUTH.getSwh());
        }

        if (sslSwitch.intValue() == RPCConfig.SSLSwitch.SSL_OFF.getSwh()) {
            rpcConfig.setSslSwitch(sslSwitch.intValue());
            rpcConfig.setListenIP(listenIP);
            rpcConfig.setListenPort(listenPort);
            logger.info(" ssl switch close, RPC config: {}", rpcConfig);
            return rpcConfig;
        }

        String caCertPath = (String) rpcMap.get("caCert");
        if (!fileIsExists(caCertPath)) {
            String errorMessage = "File: " + caCertPath + " is not exists";
            throw new WeCrossException(WeCrossException.ErrorCode.DIR_NOT_EXISTS, errorMessage);
        }

        String sslCertPath = (String) rpcMap.get("sslCert");
        if (sslCertPath == null) {
            String errorMessage =
                    "\"sslCert\" in [p2p] item  not found, please check "
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            throw new WeCrossException(WeCrossException.ErrorCode.FIELD_MISSING, errorMessage);
        }
        if (!fileIsExists(sslCertPath)) {
            String errorMessage = "File: " + sslCertPath + " is not exists";
            throw new WeCrossException(WeCrossException.ErrorCode.DIR_NOT_EXISTS, errorMessage);
        }

        String sslKeyPath = (String) rpcMap.get("sslKey");
        if (sslKeyPath == null) {
            String errorMessage =
                    "\"sslKey\" in [rpc] item  not found, please check "
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            throw new WeCrossException(WeCrossException.ErrorCode.FIELD_MISSING, errorMessage);
        }
        if (!fileIsExists(sslKeyPath)) {
            String errorMessage = "File: " + sslKeyPath + " is not exists";
            throw new WeCrossException(WeCrossException.ErrorCode.DIR_NOT_EXISTS, errorMessage);
        }

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        rpcConfig.setCaCert(resolver.getResource(caCertPath));
        rpcConfig.setSslCert(resolver.getResource(sslCertPath));
        rpcConfig.setSslKey(resolver.getResource(sslKeyPath));
        rpcConfig.setListenIP(listenIP);
        rpcConfig.setSslSwitch(sslSwitch.intValue());
        rpcConfig.setListenPort(listenPort);

        logger.info(" RPC config: {}", rpcConfig);

        return rpcConfig;
    }
}
