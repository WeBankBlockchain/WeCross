package com.webank.wecross.restserver.response;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.ResourceDetail;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.StubConstant;
import com.webank.wecross.zone.ZoneManager;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceResponse {
    private static Logger logger = LoggerFactory.getLogger(ResourceResponse.class);

    private int total;
    private ResourceDetail[] resourceDetails;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

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
        this.total = details.size();
        this.resourceDetails = details.toArray(new ResourceDetail[] {});
    }

    public void setResourceInfos(ZoneManager zoneManager, String chainPath, int offset, int size) {
        Path chain;
        try {
            chain = Path.decode(chainPath);
        } catch (Exception e) {
            logger.warn("Decode chain path error: {}", chainPath);
            return;
        }

        Map<String, Resource> resources = zoneManager.getChainResources(chain);
        LinkedList<ResourceDetail> details = new LinkedList<>();
        int index = 0;
        boolean start = false;
        for (String path : resources.keySet()) {
            if (size == 0) {
                break;
            }

            try {
                if (Path.decode(path).getResource().equals(StubConstant.PROXY_NAME)) {
                    continue;
                }
            } catch (Exception e) {
                logger.warn("Could not decode path during setResourceInfos, path:{}", path);
            }

            if (index == offset) {
                start = true;
            }

            if (start) {
                ResourceDetail detail = new ResourceDetail();
                Resource resource = resources.get(path);
                details.add(detail.initResourceDetail(resource, path));
                size--;
            }

            index++;
        }
        this.resourceDetails = details.toArray(new ResourceDetail[] {});
        this.total = resources.size() - 1;
    }

    public void setResourceDetails(ResourceDetail[] resourceDetails) {
        this.resourceDetails = resourceDetails;
    }

    public ResourceDetail[] getResourceDetails() {
        return resourceDetails;
    }

    @Override
    public String toString() {
        return "ResourceResponse{"
                + "total="
                + total
                + ", resourceDetails="
                + Arrays.toString(resourceDetails)
                + '}';
    }
}
