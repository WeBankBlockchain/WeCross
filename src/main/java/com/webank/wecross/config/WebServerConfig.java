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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.Ssl.ClientAuth;
import org.springframework.boot.web.server.SslStoreProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

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
            Ssl ssl = new Ssl();
            ssl.setClientAuth(ClientAuth.NEED);

            KeyCertLoader keyCertLoader = new KeyCertLoader();

            for (Provider provider : Security.getProviders()) {
                logger.debug("Provider: {}", provider.getName());
            }

            KeyStore keyStore = KeyStore.getInstance("pkcs12");
            keyStore.load(null);

            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();
            org.springframework.core.io.Resource sslKeyResource =
                    resolver.getResource(toml.getString("rpc.sslKey", "classpath:ssl.key"));
            PrivateKey privateKey =
                    keyCertLoader.toPrivateKey(sslKeyResource.getInputStream(), null);

            org.springframework.core.io.Resource sslCertResource =
                    resolver.getResource(toml.getString("rpc.sslCert", "classpath:ssl.crt"));
            X509Certificate[] certificates =
                    keyCertLoader.toX509Certificates(sslCertResource.getInputStream());
            keyStore.setKeyEntry("mykey", privateKey, "".toCharArray(), certificates);

            org.springframework.core.io.Resource caCertResource =
                    resolver.getResource(toml.getString("rpc.caCert", "classpath:ca.crt"));
            X509Certificate[] caCertificates =
                    keyCertLoader.toX509Certificates(caCertResource.getInputStream());

            KeyStore trustStore = KeyStore.getInstance("pkcs12");
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
        String address = toml.getString("rpc.address");
        if (address == null) {
            String errorMessage =
                    "Something wrong with [rpc] item, please check [address] in "
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            logger.error(errorMessage);
            System.exit(1);
        }
        return address;
    }

    private int getPort() {
        Long port_temp = toml.getLong("rpc.port");
        if (port_temp == null) {
            String errorMessage =
                    "Something wrong with [rpc] item, please check [port] in "
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            logger.error(errorMessage);
            System.exit(1);
        }
        return port_temp.intValue();
    }
}
