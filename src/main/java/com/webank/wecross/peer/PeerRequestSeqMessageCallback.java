package com.webank.wecross.peer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageCallback;
import com.webank.wecross.p2p.engine.P2PResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;

public class PeerRequestSeqMessageCallback extends P2PMessageCallback<PeerSeqMessageData> {
    private Logger logger = LoggerFactory.getLogger(PeerRequestSeqMessageCallback.class);

    public PeerRequestSeqMessageCallback() {
        super.setTypeReference(new TypeReference<P2PResponse<PeerSeqMessageData>>() {});
        super.setEngineCallbackMessageClassType(
                new ParameterizedTypeReference<P2PResponse<PeerSeqMessageData>>() {});
    }

    @Override
    public void onResponse(int status, String message, P2PMessage msg) {
        PeerSeqMessageData data = (PeerSeqMessageData) msg.getData();
        logger.debug(
                "Receive peer seq. status: {} message: {} seq: {}",
                status,
                message,
                data == null ? "null" : data.getSeq());
    }
}
