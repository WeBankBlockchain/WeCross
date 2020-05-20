package com.webank.wecross.network.rpc.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.common.NetworkQueryStatus;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.restserver.RestRequest;
import com.webank.wecross.restserver.RestResponse;
import com.webank.wecross.restserver.response.StubResponse;
import com.webank.wecross.stub.StubManager;
import com.webank.wecross.zone.ZoneManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** GET/POST /supportedStubs */
public class ListStubsURIHandler implements URIHandler {
    private static final Logger logger = LoggerFactory.getLogger(ListStubsURIHandler.class);

    private WeCrossHost host;
    private ObjectMapper objectMapper = new ObjectMapper();

    public ListStubsURIHandler(WeCrossHost host) {
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
        RestResponse<StubResponse> restResponse = new RestResponse<>();
        String content = httpRequest.content().toString(StandardCharsets.UTF_8);

        if (logger.isDebugEnabled()) {
            logger.debug(" request string: {}", content);
        }

        try {
            RestRequest restRequest =
                    objectMapper.readValue(content, new TypeReference<RestRequest>() {});
            restRequest.checkRestRequest("", "supportedStubs");
            StubResponse stubResponse = new StubResponse();
            ZoneManager zoneManager = host.getZoneManager();
            StubManager stubManager = zoneManager.getStubManager();
            stubResponse.setStubTypes(stubManager);
            restResponse.setData(stubResponse);
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
