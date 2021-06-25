package com.webank.wecross.test.resource;

import com.webank.wecross.stub.Path;
import org.junit.Assert;
import org.junit.Test;

public class PathTest {
    @Test
    public void allTest() {
        // legal
        try {
            // Path.decode("a..");
            Path.decode("a.b.");
            Path.decode("a.b.c");
        } catch (Exception e) {
            Assert.assertTrue("legal check failed", false);
        }

        // illegal
        try {
            Path.decode("..");
            Assert.assertTrue("illegal check failed", false);
        } catch (Exception e) {

        }

        try {
            Path.decode(".a.");
            Assert.assertTrue("illegal check failed", false);
        } catch (Exception e) {

        }

        try {
            Path.decode("..a");
            Assert.assertTrue("illegal check failed", false);
        } catch (Exception e) {

        }
    }
}
