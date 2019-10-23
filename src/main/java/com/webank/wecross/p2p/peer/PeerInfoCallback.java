package com.webank.wecross.p2p.peer;

import com.webank.wecross.host.SyncPeerMessageHandler;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageCallback;
import com.webank.wecross.p2p.engine.restful.P2PHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;

public class PeerInfoCallback extends P2PMessageCallback<PeerInfoMessageData> {
    private SyncPeerMessageHandler handler;

    private Logger logger = LoggerFactory.getLogger(PeerInfoCallback.class);

    public PeerInfoCallback() {
        super.setEngineCallbackMessageClassType(
                new ParameterizedTypeReference<P2PHttpResponse<PeerInfoMessageData>>() {});
    }

    @Override
    public void onResponse(int status, String message, P2PMessage msg) {
        if (status != 0) {
            logger.warn("Response status: " + status);
            return;
        }

        if (msg == null) {
            logger.warn("Response msg is null");
            return;
        }

        PeerInfoMessageData data = (PeerInfoMessageData) msg.getData();
        logger.info(
                "Receive peer seq. status: {} message: {} seq: {}",
                status,
                message,
                data == null ? "null" : data.getDataSeq());

        handler.onPeerMessage(getPeer(), data.getMethod(), msg);
    }

    public void setHandler(SyncPeerMessageHandler handler) {
        this.handler = handler;
    }
}
