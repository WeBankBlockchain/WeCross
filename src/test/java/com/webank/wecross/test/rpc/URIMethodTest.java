package com.webank.wecross.test.rpc;

import com.webank.wecross.network.rpc.netty.URIMethod;
import org.junit.Assert;
import org.junit.Test;

public class URIMethodTest {
    @Test
    public void URIMethodTest() throws Exception {
        URIMethod uriMethod = new URIMethod("GET", "/test");
        Assert.assertEquals(uriMethod.getMethod(), "GET");
        Assert.assertEquals(uriMethod.getUri(), "/test");
        Assert.assertTrue(!uriMethod.isResourceURI());

        URIMethod uriMethod0 = new URIMethod("POST", "/test");
        Assert.assertEquals(uriMethod0.getMethod(), "POST");
        Assert.assertEquals(uriMethod0.getUri(), "/test");
        Assert.assertTrue(!uriMethod.isResourceURI());

        URIMethod uriMethod1 = new URIMethod("POST", "/resource/a/b/c/d");
        Assert.assertEquals(uriMethod1.getMethod(), "POST");
        Assert.assertEquals(uriMethod1.getUri(), "/resource/a/b/c/d");
        Assert.assertTrue(uriMethod1.isResourceURI());
    }
}
