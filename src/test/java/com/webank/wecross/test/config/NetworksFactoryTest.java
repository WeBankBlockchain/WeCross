package com.webank.wecross.test.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.config.ConfigInfo;
import com.webank.wecross.config.ConfigUtils;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.Network;
import com.webank.wecross.network.config.NetworksFactory;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class NetworksFactoryTest {

    @Test
    public void produceNetworksTest() {
        try {
            Toml toml = ConfigUtils.getToml(ConfigInfo.MAIN_CONFIG_FILE);
            String network = toml.getString("common.network");
            System.out.println(network);

            NetworksFactory mock = new NetworksFactory();
            mock.setToml(toml);

            Map<String, Network> networkMap = mock.produceNetworks();
            Assert.assertTrue(networkMap.containsKey(network));

        } catch (WeCrossException e) {
            System.out.println("Error in produceNetworksTest: " + e.getMessage());
        }
    }
}
