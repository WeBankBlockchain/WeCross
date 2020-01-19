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

        String address = toml.getString("server.address");
        Long port_temp = toml.getLong("server.port");
        Integer port = null;
        if (address == null || port_temp == null) {
            String errorMessage =
                    "Something wrong with [server] item, please check [address] or [port] in"
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            logger.error(errorMessage);
            System.exit(1);
        } else {
            port = port_temp.intValue();
        }

        try {
            tomcatServletWebServerFactory.setAddress(InetAddress.getByName(address));
            tomcatServletWebServerFactory.setPort(port);

        } catch (Exception e) {
            logger.error(e.getMessage());
            System.exit(1);
        }
        return tomcatServletWebServerFactory;
    }
}
