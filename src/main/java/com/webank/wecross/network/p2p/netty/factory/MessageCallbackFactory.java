package com.webank.wecross.network.p2p.netty.factory;

import com.webank.wecross.account.AccountSyncManager;
import com.webank.wecross.network.p2p.netty.ConnectProcessor;
import com.webank.wecross.network.p2p.netty.DisconnectProcessor;
import com.webank.wecross.network.p2p.netty.HeartBeatProcessor;
import com.webank.wecross.network.p2p.netty.MessageType;
import com.webank.wecross.network.p2p.netty.RequestProcessor;
import com.webank.wecross.network.p2p.netty.ResponseProcessor;
import com.webank.wecross.network.p2p.netty.SeqMapper;
import com.webank.wecross.network.p2p.netty.message.MessageCallBack;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.zone.ZoneManager;

public class MessageCallbackFactory {

    private static HeartBeatProcessor newHeartBeatProcessor() {
        return new HeartBeatProcessor();
    }

    private static RequestProcessor newRequestProcessor() {
        return new RequestProcessor();
    }

    private static ResponseProcessor newResponseProcessor(SeqMapper seqMapper) {
        ResponseProcessor resourceResponseProcessor = new ResponseProcessor();
        resourceResponseProcessor.setSeqMapper(seqMapper);
        return resourceResponseProcessor;
    }

    private static ConnectProcessor newConnectProcessor(PeerManager peerManager) {
        ConnectProcessor connectProcessor = new ConnectProcessor();
        connectProcessor.setPeerManager(peerManager);

        return connectProcessor;
    }

    private static DisconnectProcessor newDisconnectProcessor(
            PeerManager peerManager,
            ZoneManager zoneManager,
            AccountSyncManager accountSyncManager) {
        DisconnectProcessor disconnectProcessor = new DisconnectProcessor();
        disconnectProcessor.setPeerManager(peerManager);
        disconnectProcessor.setZoneManager(zoneManager);
        disconnectProcessor.setAccountSyncManager(accountSyncManager);

        return disconnectProcessor;
    }

    public static MessageCallBack build(
            SeqMapper seqMapper,
            PeerManager peerManager,
            ZoneManager zoneManager,
            AccountSyncManager accountSyncManager) {
        System.out.println("Initializing MessageCallBack ...");

        MessageCallBack callback = new MessageCallBack();
        callback.setSeqMapper(seqMapper);

        callback.setProcessor(MessageType.HEARTBEAT, newHeartBeatProcessor());
        callback.setProcessor(MessageType.RESOURCE_REQUEST, newRequestProcessor());
        callback.setProcessor(MessageType.RESOURCE_RESPONSE, newResponseProcessor(seqMapper));
        callback.setProcessor(MessageCallBack.ON_CONNECT, newConnectProcessor(peerManager));
        callback.setProcessor(
                MessageCallBack.ON_DISCONNECT,
                newDisconnectProcessor(peerManager, zoneManager, accountSyncManager));

        return callback;
    }
}
