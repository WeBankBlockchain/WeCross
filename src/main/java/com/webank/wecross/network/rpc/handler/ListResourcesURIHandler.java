package com.webank.wecross.network.rpc.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.UserContext;
import com.webank.wecross.common.NetworkQueryStatus;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.restserver.RestRequest;
import com.webank.wecross.restserver.RestResponse;
import com.webank.wecross.restserver.request.ResourceRequest;
import com.webank.wecross.restserver.response.ResourceResponse;
import com.webank.wecross.stub.ObjectMapperFactory;
import com.webank.wecross.zone.ZoneManager;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** GET/POST /sys/listResources */
public class ListResourcesURIHandler implements URIHandler {
    private static final Logger logger = LoggerFactory.getLogger(ListResourcesURIHandler.class);

    private WeCrossHost host;
    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    public ListResourcesURIHandler(WeCrossHost host) {
        this.host = host;
    }

    public WeCrossHost getHost() {
        return host;
    }

    public void setHost(WeCrossHost host) {
        this.host = host;
    }

    @Override
    public void handle(
            UserContext userContext, String uri, String method, String content, Callback callback) {
        RestResponse<ResourceResponse> restResponse = new RestResponse<>();

        if (logger.isDebugEnabled()) {
            logger.debug("uri: {}, request string: {}", uri, content);
        }

        try {
            /* /sys/listResource?path=payment.bcos&offset=10&size=10 */
            if (uri.contains("?path=") && uri.contains("&offset=") && uri.contains("&size=")) {
                URI thisUri = URI.create("http:/" + uri);
                String[] querys = thisUri.getQuery().split("&");
                String chainPath = querys[0].substring(5);
                int offset = Integer.parseInt(querys[1].substring(7));
                int size = Integer.parseInt(querys[2].substring(5));
                ZoneManager zoneManager = host.getZoneManager();
                ResourceResponse resourceResponse = new ResourceResponse();
                resourceResponse.setResourceInfos(zoneManager, chainPath, offset, size);
                restResponse.setData(resourceResponse);
            } else {
                RestRequest<ResourceRequest> restRequest =
                        objectMapper.readValue(
                                content, new TypeReference<RestRequest<ResourceRequest>>() {});
                restRequest.checkRestRequest();
                ResourceRequest resourceRequest = restRequest.getData();
                ZoneManager zoneManager = host.getZoneManager();
                ResourceResponse resourceResponse = new ResourceResponse();
                resourceResponse.setResourceInfos(zoneManager, resourceRequest.isIgnoreRemote());
                restResponse.setData(resourceResponse);
            }
        } catch (WeCrossException e) {
            logger.warn("Process request error", e);
            restResponse.setErrorCode(NetworkQueryStatus.NETWORK_PACKAGE_ERROR + e.getErrorCode());
            restResponse.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.warn("Process request error", e);
            restResponse.setErrorCode(NetworkQueryStatus.INTERNAL_ERROR);
            restResponse.setMessage(e.getMessage());
        }

        callback.onResponse(restResponse);
    }
}
