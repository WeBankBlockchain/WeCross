package com.webank.wecross.restserver.p2p;

import com.webank.wecross.p2p.P2PMessageEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestfulP2PMessageEngineFactory extends P2PMessageEngineFactory {
    private Logger logger = LoggerFactory.getLogger(RestfulP2PMessageEngineFactory.class);

    public RestfulP2PMessageEngineFactory() {
        logger.info("New  RestfulP2PMessageEngine");
        super.engine = new RestfulP2PMessageEngine();
    }
}
