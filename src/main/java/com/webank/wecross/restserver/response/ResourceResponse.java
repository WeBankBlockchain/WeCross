package com.webank.wecross.restserver.response;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.restserver.ResourceDetail;
import com.webank.wecross.zone.ZoneManager;
import java.util.Map;

public class ResourceResponse {

    private ResourceDetail[] resourceDetails;

    public void setResourceInfos(ZoneManager zoneManager, boolean ignoreRemote) {
        Map<String, Resource> resources = zoneManager.getAllResources(ignoreRemote);
        ResourceDetail[] details = new ResourceDetail[resources.size()];
        int i = 0;
        for (String path : resources.keySet()) {
            ResourceDetail detail = new ResourceDetail();
            Resource resource = resources.get(path);
            details[i] = detail.initResourceDetail(resource, path);
        }
        this.resourceDetails = details;
    }

    public void setResourceDetails(ResourceDetail[] resourceDetails) {
        this.resourceDetails = resourceDetails;
    }

    public ResourceDetail[] getResourceDetails() {
        return resourceDetails;
    }
}
