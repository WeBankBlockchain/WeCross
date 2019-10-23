package com.webank.wecross.p2p;

import com.webank.wecross.p2p.engine.restful.RestfulP2PMessageEngine;
import com.webank.wecross.p2p.engine.restful.RestfulP2PMessageEngineFactory;

public class P2PMessageEngineFactory {

    private static RestfulP2PMessageEngine restfulEngineInstance;

    // private static NettyP2PMessageEngine nettyEngine = facXXX.getXXX();

    private static RestfulP2PMessageEngine getRestfulEngineInstance() {
        if (restfulEngineInstance == null) {
            restfulEngineInstance = RestfulP2PMessageEngineFactory.getEngineInstance();
        }
        return restfulEngineInstance;
    }

    public static P2PMessageEngine getEngineInstance() {

        return getRestfulEngineInstance();
    }
}
