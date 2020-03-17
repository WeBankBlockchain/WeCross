package com.webank.wecross.restserver.response;

import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.zone.ZoneManager;
import java.util.Map;

public class ResourceResponse {

    private ResourceInfo[] resourceInfos;

    public void setResourceInfos(ResourceInfo[] resourceInfos) {
        this.resourceInfos = resourceInfos;
    }

    public void setResourceInfos(ZoneManager zoneManager, boolean ignoreRemote) {
        Map<String, ResourceInfo> resources = zoneManager.getAllResourcesInfo(ignoreRemote);
        this.resourceInfos = resources.values().toArray(new ResourceInfo[resources.size()]);
    }

    public ResourceInfo[] getResourceInfos() {
        return resourceInfos;
    }
}
