package com.webank.wecross.test.account;

import com.webank.wecross.account.UniversalAccountImpl;
import com.webank.wecross.utils.SM2;
import java.security.KeyPair;
import org.junit.Assert;
import org.junit.Test;

public class UniversalAccountTest {
    private UniversalAccountImpl universalAccount;

    public UniversalAccountTest() throws Exception {
        KeyPair keyPair = SM2.newKeyPair();
        String pubKey = SM2.toPubHexString(keyPair);
        String secKey = SM2.toPemContent(keyPair);

        universalAccount = UniversalAccountImpl.builder().pubKey(pubKey).secKey(secKey).build();
    }

    @Test
    public void signVerifyTest() {
        byte[] message = {1, 2, 3, 4, 5, 6, 7};

        byte[] signBytes = universalAccount.sign(message);

        boolean result = universalAccount.verify(signBytes, message);

        Assert.assertTrue(result);
    }
}
