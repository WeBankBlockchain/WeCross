package com.webank.wecross.network;

import com.webank.wecross.peer.Peer;

public interface NetworkService {
    <T> void asyncSendMessage(Peer peer, NetworkMessage<T> msg, NetworkCallback<?> callback);

    void setNetworkProcessor(NetworkProcessor networkProcessor);

    void start() throws Exception;
}
