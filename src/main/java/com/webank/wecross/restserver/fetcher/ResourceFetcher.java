package com.webank.wecross.restserver.fetcher;

import com.webank.wecross.account.AccountAccessControlFilter;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.ResourceDetail;
import com.webank.wecross.restserver.response.ResourceResponse;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.StubConstant;
import com.webank.wecross.zone.ZoneManager;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceFetcher {
    private Logger logger = LoggerFactory.getLogger(ResourceFetcher.class);

    private ZoneManager zoneManager;

    public ResourceFetcher(ZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }

    public ResourceResponse fetchResourcesWithFilter(
            AccountAccessControlFilter filter, boolean ignoreRemote) {
        return fetchResources(filter, ignoreRemote, true);
    }

    public ResourceResponse fetchResources(boolean ignoreRemote) {
        return fetchResources(ignoreRemote, true);
    }

    public ResourceResponse fetchResources(boolean ignoreRemote, boolean ignoreProxy) {
        return fetchResources(null, ignoreRemote, ignoreProxy);
    }

    private ResourceResponse fetchResources(
            AccountAccessControlFilter filter, boolean ignoreRemote, boolean ignoreProxy) {
        Map<String, Resource> resources = null;
        if (filter == null) {
            resources = zoneManager.getAllResources(ignoreRemote);
        } else {
            resources = zoneManager.getAllResourcesWithFilter(filter, ignoreRemote);
        }

        LinkedList<ResourceDetail> details = new LinkedList<>();
        for (String path : resources.keySet()) {
            try {
                if (ignoreProxy
                        && Path.decode(path).getResource().equals(StubConstant.PROXY_NAME)) {
                    continue;
                }
            } catch (Exception e) {
                logger.warn("Could not decode path during fetchResources, path:{}", path);
            }

            ResourceDetail detail = new ResourceDetail();
            Resource resource = resources.get(path);
            details.add(detail.initResourceDetail(resource, path));
        }

        ResourceResponse resourceResponse = new ResourceResponse();
        resourceResponse.setTotal(details.size());
        resourceResponse.setResourceDetails(details.toArray(new ResourceDetail[] {}));
        return resourceResponse;
    }

    public ResourceResponse fetchResourcesWithFilter(
            AccountAccessControlFilter filter, Path chainPath, int offset, int size)
            throws WeCrossException {
        return fetchResources(filter, chainPath, offset, size);
    }

    public ResourceResponse fetchResources(Path chainPath, int offset, int size)
            throws WeCrossException {
        return fetchResources(null, chainPath, offset, size);
    }

    private ResourceResponse fetchResources(
            AccountAccessControlFilter filter, Path chainPath, int offset, int size)
            throws WeCrossException {
        LinkedHashMap<String, Resource> resources = null;
        if (filter == null) {
            resources = (LinkedHashMap<String, Resource>) zoneManager.getChainResources(chainPath);
        } else {
            resources =
                    (LinkedHashMap<String, Resource>)
                            zoneManager.getChainResourcesWithFilter(filter, chainPath);
        }

        LinkedList<ResourceDetail> details = new LinkedList<>();
        int index = 0;
        boolean start = false;

        for (Resource resource : resources.values()) {
            if (size == 0) {
                break;
            }

            try {
                if (resource.getPath().getResource().equals(StubConstant.PROXY_NAME)) {
                    continue;
                }
            } catch (Exception e) {
                logger.warn(
                        "Could not decode path during fetchResources, path:{}",
                        resource.getPath().toString());
            }

            if (index == offset) {
                start = true;
            }

            if (start) {
                ResourceDetail detail = new ResourceDetail();
                details.add(detail.initResourceDetail(resource, resource.getPath().toString()));
                size--;
            }

            index++;
        }

        ResourceResponse resourceResponse = new ResourceResponse();
        resourceResponse.setTotal(resources.size() - 1); // Exclude proxy contract
        resourceResponse.setResourceDetails(details.toArray(new ResourceDetail[] {}));

        return resourceResponse;
    }

    public ZoneManager getZoneManager() {
        return zoneManager;
    }

    public void setZoneManager(ZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }
}
