package com.webank.wecross.p2p;

public abstract class P2PMessageEngineFactory {
    protected P2PMessageEngine engine;

    public P2PMessageEngine getEngine() {
        return engine;
    }
}
