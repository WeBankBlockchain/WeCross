package com.webank.wecross.network.rpc.handler;

import com.webank.wecross.account.UserContext;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.restserver.RestResponse;
import com.webank.wecross.restserver.response.StubResponse;
import com.webank.wecross.stubmanager.StubManager;
import com.webank.wecross.zone.ZoneManager;

/** GET /sys/supportedStubs */
public class ListStubsURIHandler implements URIHandler {

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
            UserContext userContext, String uri, String method, String content, Callback callback) {
        RestResponse<StubResponse> restResponse = new RestResponse<>();
        StubResponse stubResponse = new StubResponse();
        ZoneManager zoneManager = host.getZoneManager();
        StubManager stubManager = zoneManager.getStubManager();
        stubResponse.setStubTypes(stubManager);
        restResponse.setData(stubResponse);
        callback.onResponse(restResponse);
    }
}
