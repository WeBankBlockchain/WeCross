package com.webank.wecross.test.resource;

import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.TestResource;
import com.webank.wecross.utils.core.HashUtils;
import org.junit.Assert;
import org.junit.Test;

public class TestResourceTest {

    @Test
    public void hashTest() {
        try {
            Resource resource = new TestResource();
            // resource.setPath(Path.decode("network.stub.resource"));
            String checksum = resource.getChecksum();

            Assert.assertEquals(HashUtils.sha256String("network.stub.resource"), checksum);
            Assert.assertEquals(
                    HashUtils.sha256String("network.stub.resource"), resource.getChecksum());

        } catch (Exception e) {
            Assert.assertTrue("Test exception: " + e, false);
        }
    }
}
