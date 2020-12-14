package com.webank.wecross.test.rpc;

import com.webank.wecross.network.UriDecoder;
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

    @Test
    public void paraseUriTest() throws Exception {
        String uri1 = "/sys/listResource?path=payment.bcos&offset=10&size=100";
        UriDecoder uriDecoder = new UriDecoder(uri1);
        Assert.assertEquals("listResource", uriDecoder.getMethod());
        Assert.assertEquals("payment.bcos", uriDecoder.getQueryBykey("path"));
        Assert.assertEquals("10", uriDecoder.getQueryBykey("offset"));
        Assert.assertEquals("100", uriDecoder.getQueryBykey("size"));

        String uri2 = "/resource/network/stub/resource/method";
        uriDecoder = new UriDecoder(uri2);
        Assert.assertEquals("method", uriDecoder.getMethod());
    }
}
