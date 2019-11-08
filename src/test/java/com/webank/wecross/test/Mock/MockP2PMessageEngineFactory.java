package com.webank.wecross.test.Mock;

import com.webank.wecross.p2p.P2PMessageEngine;

public class MockP2PMessageEngineFactory {

    public static P2PMessageEngine newMockP2PMessageEngine(P2PEngineMessageFilter filter) {
        return new MockP2PMessageEngine(filter);
    }
}
