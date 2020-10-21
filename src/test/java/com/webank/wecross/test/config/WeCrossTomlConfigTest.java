package com.webank.wecross.test.config;

import com.moandjiezana.toml.Toml;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class WeCrossTomlConfigTest {

    @Test
    public void allTest() {

        Toml toml = new Toml();
        toml.read(
                "[common]\n"
                        + "    network = 'network'\n"
                        + "    visible = true\n"
                        + "\n"
                        + "[stubs]\n"
                        + "    path = 'classpath:stubs'\n"
                        + "\n"
                        + "[rpc] # rpc ip & port\n"
                        + "    address = '0.0.0.0'\n"
                        + "    port = 8250\n"
                        + "    caCert = 'classpath:ca.crt'\n"
                        + "    sslCert = 'classpath:ssl.crt'\n"
                        + "    sslKey = 'classpath:ssl.key'\n"
                        + "\n"
                        + "[p2p]\n"
                        + "    listenIP = '0.0.0.0'\n"
                        + "    listenPort = 25500\n"
                        + "    caCert = 'classpath:ca.crt'\n"
                        + "    sslCert = 'classpath:ssl.crt'\n"
                        + "    sslKey = 'classpath:ssl.key'\n"
                        + "    peers = [\"127.0.0.1:25501\"]");

        Map<String, Object> tomlMap = toml.toMap();
        Assert.assertTrue(tomlMap.containsKey("common"));
        Assert.assertTrue(tomlMap.containsKey("stubs"));
        Assert.assertTrue(tomlMap.containsKey("rpc"));
        Assert.assertTrue(tomlMap.containsKey("p2p"));

        Map<String, Object> common = (Map<String, Object>) tomlMap.get("common");
        Map<String, Object> stubs = (Map<String, Object>) tomlMap.get("stubs");
        Map<String, Object> rpc = (Map<String, Object>) tomlMap.get("rpc");
        Map<String, Object> p2p = (Map<String, Object>) tomlMap.get("p2p");

        Assert.assertTrue(common.containsKey("network"));
        Assert.assertTrue(common.containsKey("visible"));

        Assert.assertTrue(stubs.containsKey("path"));

        Assert.assertTrue(rpc.containsKey("address"));
        Assert.assertTrue(rpc.containsKey("port"));
        Assert.assertTrue(rpc.containsKey("caCert"));
        Assert.assertTrue(rpc.containsKey("sslCert"));
        Assert.assertTrue(rpc.containsKey("sslKey"));

        Assert.assertTrue(p2p.containsKey("listenIP"));
        Assert.assertTrue(p2p.containsKey("listenPort"));
        Assert.assertTrue(p2p.containsKey("caCert"));
        Assert.assertTrue(p2p.containsKey("sslCert"));
        Assert.assertTrue(p2p.containsKey("sslKey"));
        Assert.assertTrue(p2p.containsKey("sslKey"));
    }
}
