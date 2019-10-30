package com.webank.wecross.peer;

import com.webank.wecross.p2p.P2PMessageCallback;
import com.webank.wecross.p2p.engine.restful.P2PHttpResponse;
import org.springframework.core.ParameterizedTypeReference;

public class PeerDoNothingCallback extends P2PMessageCallback {

    public PeerDoNothingCallback() {
        super.setEngineCallbackMessageClassType(
                new ParameterizedTypeReference<P2PHttpResponse>() {});
    }
}
