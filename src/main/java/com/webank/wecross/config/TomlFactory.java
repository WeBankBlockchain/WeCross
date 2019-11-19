package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.exception.WeCrossException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomlFactory {

    private Logger logger = LoggerFactory.getLogger(TomlFactory.class);

    @Bean
    public Toml produceToml() {
        Toml toml = new Toml();
        try {
            toml = ConfigUtils.getToml(ConfigInfo.MAIN_CONFIG_FILE);
        } catch (WeCrossException e) {
            logger.error(e.getMessage());
            System.exit(1);
        }
        return toml;
    }
}
