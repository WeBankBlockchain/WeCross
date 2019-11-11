package com.webank.wecross.test.resource;

import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.TestResource;
import org.junit.Assert;
import org.junit.Test;

public class TestResourceTest {
    @Test
    public void hashTest() {
        try {
            Resource resource = new TestResource();
            resource.setPath(Path.decode("network.stub.resource"));
            String checksum = resource.getChecksum();

            Assert.assertEquals(
                    "0xb11a0c463a578d530926baff61f4d9132675a65aa2fe6f73c4779118ce964871", checksum);
            Assert.assertEquals(
                    "0xb11a0c463a578d530926baff61f4d9132675a65aa2fe6f73c4779118ce964871",
                    resource.getChecksum());

        } catch (Exception e) {
            Assert.assertTrue("Test exception: " + e, false);
        }
    }
}
