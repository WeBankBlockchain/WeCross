package com.webank.wecross.host;

import com.webank.wecross.core.SeqUtils;
import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageCallback;
import com.webank.wecross.p2p.P2PMessageData;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.peer.PeerInfoMessageData;
import com.webank.wecross.p2p.peer.PeerRequestSeqMessageCallback;
import com.webank.wecross.p2p.peer.PeerRequestSeqMessageData;
import com.webank.wecross.p2p.peer.PeerSeqMessageData;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeerManager {
    Logger logger = LoggerFactory.getLogger(PeerManager.class);

    @Resource(name = "newRestfulP2PMessageEngine")
    private P2PMessageEngine p2pEngine;

    @Resource(name = "initPeers")
    private Map<String, Peer> peers; // url -> peer

    private int seq; // Seq of the host

    @Resource(name = "newSyncPeerMessageHandler")
    SyncPeerMessageHandler messageHandler;

    @Resource NetworkManager networkManager;

    public void start() {
        newSeq();
        //  broadcastSeq();
    }

    public void newSeq() {
        seq = this.seq = SeqUtils.newSeq();
    }

    public int getSeq() {
        return seq;
    }

    public int peerSize() {
        return peers.size();
    }

    public synchronized Peer getPeer(String url) {
        if (peers.containsKey(url)) {
            return peers.get(url);
        } else {
            return null;
        }
    }

    public synchronized void updatePeer(Peer peer) {
        peers.put(peer.getUrl(), peer);
    }

    public synchronized void removePeer(String url) {
        peers.remove(url);
    }

    public synchronized boolean hasPeerChanged(String url, int currentSeq) {
        if (!peers.containsKey(url)) {
            return true;
        }
        return peers.get(url).getSeq() != currentSeq;
    }

    public P2PMessageData onSyncPeerMessage(String url, String method, P2PMessage msg) {
        switch (method) {
            case "requestSeq":
                {
                    return handleRequestSeq();
                }
            case "requestPeerInfo":
                {
                    return handleRequestPeerInfo();
                }
            default:
                messageHandler.onSyncPeerMessage(url, method, msg);
        }
        return null;
    }

    public <T> void broadcastToPeers(P2PMessage<T> msg, P2PMessageCallback callback) {
        for (Peer peer : peers.values()) {
            p2pEngine.asyncSendMessage(peer, msg, callback);
        }
    }

    public void broadcastSeq() {
        PeerSeqMessageData data = new PeerSeqMessageData();
        data.setDataSeq(this.getSeq());
        P2PMessage<PeerSeqMessageData> msg = new P2PMessage<>();
        msg.newSeq();
        msg.setData(data);
        msg.setVersion("0.1");
        msg.setType("peer");
        broadcastToPeers(msg, new P2PMessageCallback());
    }

    public void broadcastSeqRequest() {
        PeerRequestSeqMessageData data = new PeerRequestSeqMessageData();
        P2PMessage<PeerRequestSeqMessageData> msg = new P2PMessage<>();
        msg.newSeq();
        msg.setData(data);
        msg.setVersion("0.1");
        msg.setType("peer");
        PeerRequestSeqMessageCallback callback;

        broadcastToPeers(msg, new P2PMessageCallback()); //  add callback
    }

    public PeerSeqMessageData handleRequestSeq() {
        PeerSeqMessageData data = new PeerSeqMessageData();
        data.setDataSeq(seq);
        return data;
    }

    public PeerInfoMessageData handleRequestPeerInfo() {
        PeerInfoMessageData data = new PeerInfoMessageData();
        data.setDataSeq(seq);

        Set<String> resources = networkManager.getAllNetworkStubResourceName();
        for (String resource : resources) {
            data.addResource(resource);
        }
        return data;
    }
}
