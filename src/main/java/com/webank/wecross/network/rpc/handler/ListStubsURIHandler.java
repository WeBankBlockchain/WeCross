package com.webank.wecross.network.rpc.handler;

import com.webank.wecross.common.NetworkQueryStatus;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.restserver.RestResponse;
import com.webank.wecross.restserver.response.StubResponse;
import com.webank.wecross.stub.UniversalAccount;
import com.webank.wecross.stubmanager.StubManager;
import com.webank.wecross.zone.ZoneManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** GET/POST /supportedStubs */
public class ListStubsURIHandler implements URIHandler {
    private static final Logger logger = LoggerFactory.getLogger(ListStubsURIHandler.class);

    private WeCrossHost host;

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
    public void handle(
            UniversalAccount ua, String uri, String method, String content, Callback callback) {
        RestResponse<StubResponse> restResponse = new RestResponse<>();
        try {
            StubResponse stubResponse = new StubResponse();
            ZoneManager zoneManager = host.getZoneManager();
            StubManager stubManager = zoneManager.getStubManager();
            stubResponse.setStubTypes(stubManager);
            restResponse.setData(stubResponse);
        } catch (Exception e) {
            logger.warn("Process request error", e);
            restResponse.setErrorCode(NetworkQueryStatus.INTERNAL_ERROR);
            restResponse.setMessage(e.getMessage());
        }

        callback.onResponse(restResponse);
    }
}
