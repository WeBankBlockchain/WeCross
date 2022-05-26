package com.webank.wecross.test.account;

import  com.webank.wecross.account.UniversalAccount;
import org.junit.Assert;
import org.junit.Test;

public class CheckChainNameTest {
    @Test
    public void isCheckRight() throws Exception {
        Assert.assertTrue(UniversalAccount.checkChainName("") == false);
        Assert.assertTrue(UniversalAccount.checkChainName("chainName") == false);
        Assert.assertTrue(UniversalAccount.checkChainName("payment.fabric") == true);
        Assert.assertTrue(UniversalAccount.checkChainName("payment.fabric-mychannel") == true);
        Assert.assertTrue(UniversalAccount.checkChainName("payment.bcos-group1") == true);
        Assert.assertTrue(UniversalAccount.checkChainName("payment.bcos-group2.helloworld") == false);
        Assert.assertTrue(UniversalAccount.checkChainName("testbcos-group2") == false);
    }
}
