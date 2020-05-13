package com.webank.wecross.network;

import com.webank.wecross.network.p2p.netty.common.Node;

public interface NetworkProcessor {
    String process(Node node, String content);
}
