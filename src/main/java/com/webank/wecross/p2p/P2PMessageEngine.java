package com.webank.wecross.p2p;

import com.webank.wecross.p2p.engine.P2PResponse;
import com.webank.wecross.p2p.netty.P2PService;
import com.webank.wecross.p2p.netty.common.Peer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class P2PMessageEngine {
    public abstract <T> void asyncSendMessage(
            Peer peer, P2PMessage<T> msg, P2PMessageCallback callback);

    private P2PService p2PService;

    public P2PService getP2PService() {
        return p2PService;
    }

    public void setP2PService(P2PService p2PService) {
        this.p2PService = p2PService;
    }

    protected <T> void checkP2PMessage(P2PMessage<T> msg) throws Exception {
        if (msg.getVersion().isEmpty()) {
            throw new Exception("message version is empty");
        }
        if (msg.getMethod().isEmpty()) {
            throw new Exception("message method is empty");
        }
        if (msg.getSeq() == 0) {
            throw new Exception("message seq is 0");
        }
    }

    protected void checkCallback(P2PMessageCallback callback) throws Exception {
        if (callback.getPeer() == null) {
            throw new Exception("callback from com.webank.wecross.peer has not set");
        }
    }

    protected <T> void checkHttpResponse(ResponseEntity<P2PResponse<T>> response) throws Exception {
        if (response == null) {
            throw new Exception("Remote response null");
        }
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Method not exists: " + response.toString());
        }
    }

    protected void checkPeerResponse(P2PResponse responseMsg) throws Exception {
        if (responseMsg == null) {
            throw new Exception("Peer response null");
        }
    }

    protected void executeCallback(
            P2PMessageCallback callback, int status, String message, P2PMessage data) {
        callback.setStatus(status);
        callback.setMessage(message);
        callback.setData(data);
        callback.execute();
    }
}
