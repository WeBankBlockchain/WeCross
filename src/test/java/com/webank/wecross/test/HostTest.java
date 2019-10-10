package com.webank.wecross.test;

import com.webank.wecross.p2p.Host;
import com.webank.wecross.p2p.PeerState;
import java.util.HashMap;
import java.util.Map;

public class HostTest {
    public static void main(String args[]) {
        Host host = new Host();
        Map<String, PeerState> peers = new HashMap<String, PeerState>();

        peers.put("http://127.0.0.1:8080/", new PeerState());
        host.setPeers(peers);

        host.syncAllState();

        System.out.println("state: " + peers.get("http://127.0.0.1:8080/").getSeq());
    }
}
