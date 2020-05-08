package com.webank.wecross.test.resource;

import static com.webank.wecross.stub.ResourceInfo.isEqualInfos;

import com.webank.wecross.stub.ResourceInfo;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class ResourceInfoTest {
    @Test
    public void equalTest() {
        Map<String, ResourceInfo> infos1 = newMockResourcesInfos(100, 0);
        Map<String, ResourceInfo> infos2 = newMockResourcesInfos(100, 0);
        Map<String, ResourceInfo> infos3 = newMockResourcesInfos(100, 1);
        Map<String, ResourceInfo> infos4 = newMockResourcesInfos(200, 0);

        Assert.assertTrue(isEqualInfos(infos1, infos2));
        Assert.assertFalse(isEqualInfos(infos1, infos3));
        Assert.assertFalse(isEqualInfos(infos1, infos4));
    }

    private Map<String, ResourceInfo> newMockResourcesInfos(int num, int startId) {

        Map<String, ResourceInfo> res = new HashMap<>();

        for (int i = 0; i < num; i++) {
            ResourceInfo info = new ResourceInfo();
            int id = i + startId;
            String path =
                    "test-network"
                            + ((id / 1000) % 10)
                            + ".test-stub"
                            + ((id / 100) % 100)
                            + ".test-resource"
                            + id % 100;
            info.setName(path);
            info.setChecksum("0xaabbccdd" + id);

            res.put(path, info);
        }

        return res;
    }
}
