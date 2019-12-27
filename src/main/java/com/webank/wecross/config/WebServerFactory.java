package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.utils.WeCrossDefault;
import java.net.InetAddress;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebServerFactory {

    private static Logger logger = LoggerFactory.getLogger(WebServerFactory.class);

    @Resource(name = "produceToml")
    Toml toml;

    @Bean
    public TomcatServletWebServerFactory servletWebServerFactory() {
        TomcatServletWebServerFactory tomcatServletWebServerFactory =
                new TomcatServletWebServerFactory();
        try {
            String address = toml.getString("server.address");
            Integer port = toml.getLong("server.port").intValue();
            if (address == null || port == null) {
                String errorMessage =
                        "Something wrong with [server] item, please check "
                                + WeCrossDefault.MAIN_CONFIG_FILE;
                logger.error(errorMessage);
            }

            tomcatServletWebServerFactory.setAddress(InetAddress.getByName(address));
            tomcatServletWebServerFactory.setPort(port);

        } catch (Exception e) {
            logger.error(e.getMessage());
            System.exit(1);
        }
        return tomcatServletWebServerFactory;
    }
}
