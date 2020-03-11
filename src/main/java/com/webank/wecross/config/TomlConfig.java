package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.common.WeCrossDefault;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.utils.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomlConfig {

    private Logger logger = LoggerFactory.getLogger(TomlConfig.class);

    @Bean
    public Toml newToml() {
        Toml toml = new Toml();
        try {
            toml = ConfigUtils.getToml(WeCrossDefault.MAIN_CONFIG_FILE);
        } catch (WeCrossException e) {
            logger.error(e.getMessage());
            System.exit(1);
        }
        return toml;
    }
}
