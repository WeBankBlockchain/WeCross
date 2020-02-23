package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.common.WeCrossDefault;
import java.net.InetAddress;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebServerConfig {

    private static Logger logger = LoggerFactory.getLogger(WebServerConfig.class);

    @Resource(name = "produceToml")
    Toml toml;

    @Bean
    public TomcatServletWebServerFactory servletWebServerFactory() {
        String address = getAddress();
        int port = getPort();
        TomcatServletWebServerFactory tomcatServletWebServerFactory =
                new TomcatServletWebServerFactory();
        try {
            tomcatServletWebServerFactory.setAddress(InetAddress.getByName(address));
            tomcatServletWebServerFactory.setPort(port);

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
}
