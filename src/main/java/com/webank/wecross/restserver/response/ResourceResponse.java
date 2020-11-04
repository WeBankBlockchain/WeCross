package com.webank.wecross.restserver.response;

import com.webank.wecross.resource.ResourceDetail;
import java.util.Arrays;

public class ResourceResponse {
    private int total;
    private ResourceDetail[] resourceDetails;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
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
