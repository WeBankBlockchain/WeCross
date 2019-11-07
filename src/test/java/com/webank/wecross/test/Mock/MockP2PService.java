package com.webank.wecross.test.Mock;

import com.webank.wecross.p2p.netty.P2PService;
import com.webank.wecross.p2p.netty.common.Peer;
import java.util.HashSet;
import java.util.Set;

public class MockP2PService extends P2PService {
    private Set<Peer> peers = new HashSet<>();

    public void addPeer(Peer peer) {
        peers.add(peer);
    }

    @Override
    public Set<Peer> getConnectedPeers() {
        return peers;
    }
}
