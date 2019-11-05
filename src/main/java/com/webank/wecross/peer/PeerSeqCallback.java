package com.webank.wecross.peer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageCallback;
import com.webank.wecross.p2p.engine.P2PResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;

@Configuration
public class PeerSeqCallback extends P2PMessageCallback<PeerSeqMessageData> {
    private SyncPeerMessageHandler handler;
    private Logger logger = LoggerFactory.getLogger(PeerSeqCallback.class);

    public PeerSeqCallback() {
        super.setTypeReference(new TypeReference<P2PResponse<PeerSeqMessageData>>() {});
        super.setEngineCallbackMessageClassType(
                new ParameterizedTypeReference<P2PResponse<PeerSeqMessageData>>() {});
    }

    @Override
    public void onResponse(int status, String message, P2PMessage msg) {
        if (status != 0) {
            logger.debug("Response status: " + status);
            return;
        }

        if (msg == null) {
            logger.debug("Response msg is null");
            return;
        }

        PeerSeqMessageData data = (PeerSeqMessageData) msg.getData();
        logger.debug(
                "Receive peer seq. status: {} message: {} seq: {}",
                status,
                message,
                data == null ? "null" : data.getSeq());

        msg.setMethod("seq");
        handler.onPeerMessage(this.getPeer(), msg.getMethod(), msg);
    }

    public void setHandler(SyncPeerMessageHandler handler) {
        this.handler = handler;
    }
}
