package com.webank.wecross.network.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.exception.Status;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.Network;
import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.config.StubsFactory;
import com.webank.wecross.utils.ConfigUtils;
import com.webank.wecross.utils.WeCrossDefault;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NetworksFactory {

    private Logger logger = LoggerFactory.getLogger(NetworksFactory.class);

    @Resource(name = "produceToml")
    Toml toml;

    @Bean
    public Map<String, Network> produceNetworks() {
        Map<String, Network> result = new HashMap<>();

        try {
            String network = toml.getString("common.network");
            if (network == null) {
                String errorMessage =
                        "\"network\" in [common] item  not found, please check "
                                + WeCrossDefault.MAIN_CONFIG_FILE;
                logger.error(errorMessage);
                System.exit(1);
            }

            Boolean visible = toml.getBoolean("common.visible");
            if (visible == null) {
                String errorMessage =
                        "\"visible\" in [common] item  not found, please check "
                                + WeCrossDefault.MAIN_CONFIG_FILE;
                logger.error(errorMessage);
                System.exit(1);
            }

            String stubsPath = toml.getString("stubs.path");
            if (stubsPath == null) {
                String errorMessage =
                        "\"path\" in [stubs] item  not found, please check "
                                + WeCrossDefault.MAIN_CONFIG_FILE;
                throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
            }

            Map<String, String> stubsDir = ConfigUtils.getStubsDir(stubsPath);
            Map<String, Stub> stubsBean = StubsFactory.getStubs(network, stubsDir);
            Network networkBean = new Network();
            if (stubsBean != null) {
                // init network bean
                networkBean.setStubs(stubsBean);
                networkBean.setVisible(visible);
                result.put(network, networkBean);
            } else {
                logger.error("No stubs found in {}", network);
                System.exit(1);
            }

        } catch (WeCrossException e) {
            logger.error(e.getMessage());
            System.exit(1);
        }

        return result;
    }

    public Toml getToml() {
        return toml;
    }

    public void setToml(Toml toml) {
        this.toml = toml;
    }
}
