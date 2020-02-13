package com.webank.wecross.test.Mock;

import com.webank.wecross.p2p.netty.P2PService;
import com.webank.wecross.peer.PeerInfo;

import java.util.HashSet;
import java.util.Set;

public class MockP2PService extends P2PService {
    private Set<PeerInfo> peers = new HashSet<>();

    public void addPeer(PeerInfo peer) {
        peers.add(peer);
    }
}
