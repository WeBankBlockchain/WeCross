package com.webank.wecross.test.core;

import com.webank.wecross.core.HashUtils;
import org.junit.Assert;
import org.junit.Test;

public class HashUtilsTest {
    @Test
    public void hash256Test() {
        String str = "network.stub.resource";
        String res = HashUtils.sha256String(str);
        Assert.assertEquals("0xb11a0c463a578d530926baff61f4d9132675a65aa2fe6f73c4779118ce964871", res);
    }
}
