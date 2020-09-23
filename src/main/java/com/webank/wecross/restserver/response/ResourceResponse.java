package com.webank.wecross.restserver.response;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.ResourceDetail;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.StubConstant;
import com.webank.wecross.zone.ZoneManager;
import java.util.LinkedList;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceResponse {
    private static Logger logger = LoggerFactory.getLogger(ResourceResponse.class);

    private ResourceDetail[] resourceDetails;

    public void setResourceInfos(ZoneManager zoneManager, boolean ignoreRemote) {
        setResourceInfos(zoneManager, ignoreRemote, true);
    }

    public void setResourceInfos(
            ZoneManager zoneManager, boolean ignoreRemote, boolean ignoreProxy) {
        Map<String, Resource> resources = zoneManager.getAllResources(ignoreRemote);
        LinkedList<ResourceDetail> details = new LinkedList<>();
        for (String path : resources.keySet()) {

            try {
                if (ignoreProxy
                        && Path.decode(path).getResource().equals(StubConstant.PROXY_NAME)) {
                    continue;
                }
            } catch (Exception e) {
                logger.warn("Could not decode path during setResourceInfos, path:{}", path);
            }

            ResourceDetail detail = new ResourceDetail();
            Resource resource = resources.get(path);
            details.add(detail.initResourceDetail(resource, path));
        }
        this.resourceDetails = details.toArray(new ResourceDetail[] {});
    }

    public void setResourceDetails(ResourceDetail[] resourceDetails) {
        this.resourceDetails = resourceDetails;
    }

    public ResourceDetail[] getResourceDetails() {
        return resourceDetails;
    }
}
