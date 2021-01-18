package com.webank.wecross.test.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.config.BlockVerifierTomlConfig;
import org.junit.Assert;
import org.junit.Test;

public class BlockVerifierTomlConfigTest {

    @Test
    public void allTest() throws Exception {

        Toml toml = new Toml();
        toml.read(
                "[verifiers]\n"
                        + "    [verifiers.payment.bcos-group1]\n"
                        + "        chainType = 'BCOS2.0'\n"
                        + "        pubKey = [\n"
                        + "            '61dd7293c95af375e0cb0465b785115856cdab54a3641fbbd9f5d1b59fb750949ad',\n"
                        + "            '363c07944cd0e05c291658a55c6fa5fc19c0faa3b313976b4f478c3ad73c510f4ee',\n"
                        + "            '65015f05c55b23283b21053166959ef66e777c5e467a8a244356f28b014a097bf5a',\n"
                        + "            'b2b525c3303959365c60b2d9134afe4a86f865f7ea87d1204a0f761afed466b6f5c'\n"
                        + "        ]\n"
                        + "    [verifiers.payment.bcos-group2]\n"
                        + "        chainType = 'BCOS2.0'\n"
                        + "        pubKey = [\n"
                        + "            '61dd7293c95af375e0cb0465b785115856cdab54a3641fbbd9f5d1b59fb750949ad'\n"
                        + "        ]\n"
                        + "    [verifiers.payment.bcos-gm]\n"
                        + "        chainType = 'GM_BCOS2.0'\n"
                        + "        pubKey = [\n"
                        + "            'f9f3fd009bf954a2cb566ec50f2bc55a76298b179ca3105513c4'\n"
                        + "        ]\n");
        BlockVerifierTomlConfig.Verifiers verifiers = new BlockVerifierTomlConfig.Verifiers();
        verifiers.addVerifiers(toml);
        Assert.assertTrue(verifiers.getVerifiers().containsKey("payment.bcos-group1"));
        Assert.assertTrue(verifiers.getVerifiers().containsKey("payment.bcos-group2"));
        Assert.assertTrue(verifiers.getVerifiers().containsKey("payment.bcos-gm"));

        Assert.assertEquals(
                "BCOS2.0", verifiers.getVerifiers().get("payment.bcos-group1").getChainType());
        Assert.assertEquals(
                "BCOS2.0", verifiers.getVerifiers().get("payment.bcos-group2").getChainType());
        Assert.assertEquals(
                "GM_BCOS2.0", verifiers.getVerifiers().get("payment.bcos-gm").getChainType());
    }
}
