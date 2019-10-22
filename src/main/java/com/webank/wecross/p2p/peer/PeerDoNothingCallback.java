package com.webank.wecross.p2p.peer;

import com.webank.wecross.p2p.P2PMessageCallback;
import com.webank.wecross.p2p.engine.p2p.P2PHttpResponse;
import org.springframework.core.ParameterizedTypeReference;

public class PeerDoNothingCallback extends P2PMessageCallback {

    public PeerDoNothingCallback() {
        super.setEngineCallbackMessageClassType(
                new ParameterizedTypeReference<P2PHttpResponse>() {});
    }
}
