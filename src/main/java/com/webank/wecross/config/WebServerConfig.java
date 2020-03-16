package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.common.WeCrossDefault;
import java.net.InetAddress;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.Ssl.ClientAuth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebServerConfig {

    private static Logger logger = LoggerFactory.getLogger(WebServerConfig.class);

    @Resource Toml toml;

    @Bean
    public TomcatServletWebServerFactory newTomcatServletWebServerFactory() {
        String address = getAddress();
        int port = getPort();
        TomcatServletWebServerFactory tomcatServletWebServerFactory =
                new TomcatServletWebServerFactory();
        try {
            tomcatServletWebServerFactory.setAddress(InetAddress.getByName(address));
            tomcatServletWebServerFactory.setPort(port);
            Ssl sll = new Ssl();
            sll.setKeyStore(getKeyStore());
            sll.setKeyStorePassword(getKeyStorePass());
            sll.setKeyStoreType(getKeyStoreType());
            sll.setTrustStore(getTrustStore());
            sll.setTrustStorePassword(getTrustStorePass());
            sll.setClientAuth(ClientAuth.NEED);
            tomcatServletWebServerFactory.setSsl(sll);
        } catch (Exception e) {
            logger.error(e.getMessage());
            System.exit(1);
        }
        return tomcatServletWebServerFactory;
    }

    private String getAddress() {
        String address = toml.getString("server.address");
        if (address == null) {
            String errorMessage =
                    "Something wrong with [server] item, please check [address] in"
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            logger.error(errorMessage);
            System.exit(1);
        }
        return address;
    }

    private int getPort() {
        Long port_temp = toml.getLong("server.port");
        if (port_temp == null) {
            String errorMessage =
                    "Something wrong with [server] item, please check [port] in"
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            logger.error(errorMessage);
            System.exit(1);
        }
        return port_temp.intValue();
    }

    private String getKeyStore() {
        String keyStore = toml.getString("server.keyStore");
        if (keyStore == null) {
            String errorMessage =
                    "Something wrong with [server] item, please check [keyStore] in"
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            logger.error(errorMessage);
            System.exit(1);
        }
        return keyStore;
    }

    private String getKeyStorePass() {
        String keyStorePass = toml.getString("server.keyStorePass");
        if (keyStorePass == null) {
            String errorMessage =
                    "Something wrong with [server] item, please check [keyStorePass] in"
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            logger.error(errorMessage);
            System.exit(1);
        }
        return keyStorePass;
    }

    private String getKeyStoreType() {
        String keyStoreType = toml.getString("server.keyStoreType");
        if (keyStoreType == null) {
            String errorMessage =
                    "Something wrong with [server] item, please check [keyStoreType] in"
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            logger.error(errorMessage);
            System.exit(1);
        }
        return keyStoreType;
    }

    private String getTrustStore() {
        String trustStore = toml.getString("server.trustStore");
        if (trustStore == null) {
            String errorMessage =
                    "Something wrong with [server] item, please check [trustStore] in"
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            logger.error(errorMessage);
            System.exit(1);
        }
        return trustStore;
    }

    private String getTrustStorePass() {
        String trustStorePass = toml.getString("server.trustStorePass");
        if (trustStorePass == null) {
            String errorMessage =
                    "Something wrong with [server] item, please check [trustStorePass] in"
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            logger.error(errorMessage);
            System.exit(1);
        }
        return trustStorePass;
    }
}
