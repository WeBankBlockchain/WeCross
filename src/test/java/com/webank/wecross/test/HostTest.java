package com.webank.wecross.test;

import com.webank.wecross.p2p.Host;
import com.webank.wecross.p2p.PeerStatus;
import java.util.HashMap;
import java.util.Map;

public class HostTest {
    public static String hello(String msg) {
        return "Hello world: " + msg;
    }

    public static void main(String args[]) {
        Host host = new Host();
        Map<String, PeerStatus> peers = new HashMap<String, PeerStatus>();

        peers.put("http://127.0.0.1:8080/", new PeerStatus());
        host.setPeers(peers);

        host.syncAllState();

        System.out.println("state: " + peers.get("http://127.0.0.1:8080/").getSeq());
    }
}
