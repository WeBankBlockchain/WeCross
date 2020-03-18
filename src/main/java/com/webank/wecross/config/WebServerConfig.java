package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.common.WeCrossDefault;
import com.webank.wecross.utils.KeyCertLoader;
import java.net.InetAddress;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import javax.annotation.Resource;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.Ssl.ClientAuth;
import org.springframework.boot.web.server.SslStoreProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebServerConfig {

    private static Logger logger = LoggerFactory.getLogger(WebServerConfig.class);

    @Resource Toml toml;
    @Resource P2PConfig p2pConfig;

    @Bean
    public TomcatServletWebServerFactory newTomcatServletWebServerFactory() {
        String address = getAddress();
        int port = getPort();
        TomcatServletWebServerFactory tomcatServletWebServerFactory =
                new TomcatServletWebServerFactory();

        Security.addProvider(new BouncyCastleProvider());
        try {
            tomcatServletWebServerFactory.setAddress(InetAddress.getByName(address));
            tomcatServletWebServerFactory.setPort(port);
            Ssl ssl = new Ssl();
            ssl.setClientAuth(ClientAuth.NEED);

            KeyCertLoader keyCertLoader = new KeyCertLoader();

            for (Provider provider : Security.getProviders()) {
                logger.debug("Provider: {}", provider.getName());
            }

            KeyStore keyStore = KeyStore.getInstance("jks");
            keyStore.load(null);

            PrivateKey privateKey =
                    keyCertLoader.toPrivateKey(p2pConfig.getSslKey().getInputStream(), null);
            X509Certificate[] certificates =
                    keyCertLoader.toX509Certificates(p2pConfig.getSslCert().getInputStream());
            keyStore.setKeyEntry("mykey", privateKey, "".toCharArray(), certificates);

            X509Certificate[] caCertificates =
                    keyCertLoader.toX509Certificates(p2pConfig.getCaCert().getInputStream());
            KeyStore trustStore = KeyStore.getInstance("jks");
            trustStore.load(null);
            trustStore.setCertificateEntry("mykey", caCertificates[0]);

            tomcatServletWebServerFactory.setSslStoreProvider(
                    new SslStoreProvider() {
                        @Override
                        public KeyStore getTrustStore() throws Exception {
                            return trustStore;
                        }

                        @Override
                        public KeyStore getKeyStore() throws Exception {
                            return keyStore;
                        }
                    });

            tomcatServletWebServerFactory.setSsl(ssl);
        } catch (Exception e) {
            logger.error("Error loading webserver config", e);
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
        return toml.getString("server.keyStoreType", "PKCS12");
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
