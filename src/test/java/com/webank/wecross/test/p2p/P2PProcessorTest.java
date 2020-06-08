package com.webank.wecross.test.p2p;

import org.junit.Test;

import com.webank.wecross.network.NetworkResponse;
import com.webank.wecross.network.p2p.P2PProcessor;
import com.webank.wecross.network.p2p.netty.common.Node;
import com.webank.wecross.peer.Peer;

public class P2PProcessorTest {
	@Test
	public void onStatusMessageTest() {
		Node node = new Node();
		node.setHost("127.0.0.1");
		node.setNodeID("123456");
		node.setPort(25500);
		Peer peer = new Peer(node);
		
		P2PProcessor p2pProcessor = new P2PProcessor();
		// NetworkResponse<Object> response = p2pProcessor.onStatusMessage(peer, method, p2pRequestString);
	}
}
