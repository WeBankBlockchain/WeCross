package com.webank.wecross.network;

import com.webank.wecross.network.p2p.netty.common.Node;

public interface NetworkProcessor {
    interface Callback {
        void onResponse(String responseContent);
    }

    void process(Node node, String content, NetworkProcessor.Callback callback);
}
