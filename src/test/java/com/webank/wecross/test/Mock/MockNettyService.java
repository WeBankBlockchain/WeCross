package com.webank.wecross.test.Mock;

import com.webank.wecross.network.p2p.netty.NettyService;
import com.webank.wecross.peer.Peer;
import java.util.HashSet;
import java.util.Set;

public class MockNettyService extends NettyService {
    private Set<Peer> peers = new HashSet<>();

    public void addPeer(Peer peer) {
        peers.add(peer);
    }
}
