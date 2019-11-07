package com.webank.wecross.peer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wecross.p2p.P2PMessageCallback;
import com.webank.wecross.p2p.engine.P2PResponse;

public class PeerDoNothingCallback extends P2PMessageCallback {

    public PeerDoNothingCallback() {
        super.setTypeReference(new TypeReference<P2PResponse>() {});
    }
}
