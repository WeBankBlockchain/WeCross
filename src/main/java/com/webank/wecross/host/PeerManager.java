package com.webank.wecross.host;

import com.webank.wecross.core.SeqUtil;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageCallback;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.peer.PeerSeqMessageData;
import java.util.Map;
import javax.annotation.Resource;

public class PeerManager {
    @Resource(name = "newRestfulP2PMessageEngine")
    private P2PMessageEngine p2pEngine;

    private int seq; // Seq of the host

    public void start() {
        newSeq();
        // broadcastSeq();
    }

    @Resource(name = "initPeers")
    private Map<String, Peer> peers; // url -> peer

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

    public void newSeq() {
        seq = this.seq = SeqUtil.newSeq();
    }

    public int getSeq() {
        return seq;
    }

    public int peerSize() {
        return peers.size();
    }
}
