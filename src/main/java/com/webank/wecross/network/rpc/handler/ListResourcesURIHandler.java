package com.webank.wecross.network.rpc.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.common.NetworkQueryStatus;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.restserver.RestRequest;
import com.webank.wecross.restserver.RestResponse;
import com.webank.wecross.restserver.request.ResourceRequest;
import com.webank.wecross.restserver.response.ResourceResponse;
import com.webank.wecross.zone.ZoneManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** GET/POST /listResources */
public class ListResourcesURIHandler implements URIHandler {
    private static final Logger logger = LoggerFactory.getLogger(ListResourcesURIHandler.class);

    private WeCrossHost host;
    private ObjectMapper objectMapper = new ObjectMapper();

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
    public RestResponse handle(ChannelHandlerContext ctx, FullHttpRequest httpRequest) {
        RestResponse<ResourceResponse> restResponse = new RestResponse<>();
        String content = httpRequest.content().toString(StandardCharsets.UTF_8);

        if (logger.isDebugEnabled()) {
            logger.debug(" request string: {}", content);
        }

        try {
            RestRequest<ResourceRequest> restRequest =
                    objectMapper.readValue(
                            content, new TypeReference<RestRequest<ResourceRequest>>() {});
            restRequest.checkRestRequest("", "listResources");
            ResourceRequest resourceRequest = restRequest.getData();
            ZoneManager zoneManager = host.getZoneManager();
            ResourceResponse resourceResponse = new ResourceResponse();
            resourceResponse.setResourceInfos(zoneManager, resourceRequest.isIgnoreRemote());
            restResponse.setData(resourceResponse);
        } catch (WeCrossException e) {
            logger.warn("Process request error", e);
            restResponse.setErrorCode(NetworkQueryStatus.EXCEPTION_FLAG + e.getErrorCode());
            restResponse.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.warn("Process request error", e);
            restResponse.setErrorCode(NetworkQueryStatus.INTERNAL_ERROR);
            restResponse.setMessage(e.getMessage());
        }
        return restResponse;
    }
}
