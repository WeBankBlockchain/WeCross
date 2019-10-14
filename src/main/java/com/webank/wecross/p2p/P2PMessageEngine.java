package com.webank.wecross.p2p;

import java.util.Map;

public abstract class P2PMessageEngine {
    private Map<String, P2PMessageCallback> registeredHandler;

    public abstract <T> void asyncSendMessage(
            Peer peer, P2PMessage<T> msg, P2PMessageCallback callback);

    public synchronized void registerHandler(String type, P2PMessageCallback handler)
            throws Exception {
        if (registeredHandler.containsKey(type)) {
            throw new Exception("Duplicate register handler: " + type);
        }
        registeredHandler.put(type, handler);
    }
}
