package com.webank.wecross.p2p.peer;

import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageCallback;
import com.webank.wecross.p2p.engine.p2p.P2PHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;

public class PeerRequestSeqMessageCallback extends P2PMessageCallback<PeerSeqMessageData> {
    private Logger logger = LoggerFactory.getLogger(PeerRequestSeqMessageCallback.class);

    public PeerRequestSeqMessageCallback() {
        super.setEngineCallbackMessageClassType(
                new ParameterizedTypeReference<P2PHttpResponse<PeerSeqMessageData>>() {});
    }

    @Override
    public void onResponse(int status, String message, P2PMessage msg) {
        PeerSeqMessageData data = (PeerSeqMessageData) msg.getData();
        logger.info(
                "Receive peer seq. status: {} message: {} seq: {}",
                status,
                message,
                data == null ? "null" : data.getDataSeq());
    }
}
