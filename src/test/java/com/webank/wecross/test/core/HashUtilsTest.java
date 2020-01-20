package com.webank.wecross.test.core;

import com.webank.wecross.utils.core.HashUtils;
import org.junit.Assert;
import org.junit.Test;

public class HashUtilsTest {
    @Test
    public void hash256Test() {
        String str = "network.stub.resource";
        String res = HashUtils.sha256String(str);
        Assert.assertEquals(64 + 2, res.length());
    }
}
