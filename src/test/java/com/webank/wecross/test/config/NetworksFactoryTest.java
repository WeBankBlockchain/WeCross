package com.webank.wecross.test.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.config.ZonesConfig;
import org.junit.Test;

public class NetworksFactoryTest {

    @Test
    public void produceNetworksTest() {
        Toml toml = new Toml();
        toml.read(
                "[common]\n"
                        + "    network = 'payment'\n"
                        + "    visible = true\n"
                        + "\n"
                        + "[stubs]\n"
                        + "    path = 'classpath:stubs'\n"
                        + "\n"
                        + "[server] # tomcat server\n"
                        + "    address = '127.0.0.1'\n"
                        + "    port = 8080\n"
                        + "\n"
                        + "[p2p]\n"
                        + "    listenIP = '0.0.0.0'\n"
                        + "    listenPort = 12346\n"
                        + "    caCert = ''\n"
                        + "    sslCert = ''\n"
                        + "    sslKey = ''\n"
                        + "    peers = []\n"
                        + "\n"
                        + "[test]\n"
                        + "    enableTestResource = true");
        String network = toml.getString("common.network");
        System.out.println(network);

        ZonesConfig zoneConfig = new ZonesConfig();
        zoneConfig.setToml(toml);

        /*
        Map<String, Zone> networkMap = zoneConfig.readNetworksConfig();
        Assert.assertTrue(networkMap.containsKey(network));
        */
    }
}
