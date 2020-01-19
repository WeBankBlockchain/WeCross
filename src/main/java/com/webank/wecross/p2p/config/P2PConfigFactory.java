package com.webank.wecross.p2p.config;

import static com.webank.wecross.utils.ConfigUtils.fileIsExists;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.exception.Status;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.utils.WeCrossDefault;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
public class P2PConfigFactory {

    private static Logger logger = LoggerFactory.getLogger(P2PConfigFactory.class);

    @Resource(name = "produceToml")
    Toml toml;

    @Bean
    public P2PConfig produceP2PConfig() throws WeCrossException {
        P2PConfig p2PConfig = null;
        try {
            Map<String, Object> wecrossMap = toml.toMap();

            @SuppressWarnings("unchecked")
            Map<String, Object> p2pMap = (Map<String, Object>) wecrossMap.get("p2p");
            if (p2pMap == null) {
                String errorMessage =
                        "Something wrong in [p2p] item, please check "
                                + WeCrossDefault.MAIN_CONFIG_FILE;
                throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
            }
            p2PConfig = getP2PConfig(p2pMap);

        } catch (WeCrossException e) {
            logger.error(e.getMessage());
            System.exit(1);
        }
        return p2PConfig;
    }

    public P2PConfig getP2PConfig(Map<String, Object> p2pMap) throws WeCrossException {
        P2PConfig p2PConfig = new P2PConfig();

        String listenIP = (String) p2pMap.get("listenIP");
        if (listenIP == null) {
            String errorMessage =
                    "\"listenIP\" in [p2p] item  not found, please check "
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }

        @SuppressWarnings("unchecked")
        List<String> peers = (List<String>) p2pMap.get("peers");
        if (peers == null) {
            String errorMessage =
                    "\"peers\" in [p2p] item  not found, please check "
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }

        Long listenPort_temp = (Long) p2pMap.get("listenPort");
        Integer listenPort;
        if (listenPort_temp != null) {
            listenPort = listenPort_temp.intValue();
        } else {
            String errorMessage =
                    "\"listenPort\" in [p2p] item  not found, please check "
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }

        String caCertPath = (String) p2pMap.get("caCert");
        if (caCertPath == null) {
            String errorMessage =
                    "\"caCert\" in [p2p] item  not found, please check "
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }
        if (!fileIsExists(caCertPath)) {
            String errorMessage = "File: " + caCertPath + " is not exists";
            throw new WeCrossException(Status.DIR_NOT_EXISTS, errorMessage);
        }

        String sslCertPath = (String) p2pMap.get("sslCert");
        if (sslCertPath == null) {
            String errorMessage =
                    "\"sslCert\" in [p2p] item  not found, please check "
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }
        if (!fileIsExists(sslCertPath)) {
            String errorMessage = "File: " + sslCertPath + " is not exists";
            throw new WeCrossException(Status.DIR_NOT_EXISTS, errorMessage);
        }

        String sslKeyPath = (String) p2pMap.get("sslKey");
        if (sslKeyPath == null) {
            String errorMessage =
                    "\"sslKey\" in [p2p] item  not found, please check "
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
        }
        if (!fileIsExists(sslKeyPath)) {
            String errorMessage = "File: " + sslKeyPath + " is not exists";
            throw new WeCrossException(Status.DIR_NOT_EXISTS, errorMessage);
        }

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        p2PConfig.setCaCert(resolver.getResource(caCertPath));
        p2PConfig.setSslCert(resolver.getResource(sslCertPath));
        p2PConfig.setSslKey(resolver.getResource(sslKeyPath));
        p2PConfig.setListenIP(listenIP);
        p2PConfig.setListenPort(listenPort);
        p2PConfig.setPeers(peers);

        return p2PConfig;
    }
}
