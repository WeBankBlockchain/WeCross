package com.webank.wecross.zone;

import com.webank.wecross.stub.Path;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Zone {

    private Logger logger = LoggerFactory.getLogger(Zone.class);

    private Map<String, Chain> chains = new HashMap<>();

    // Access control
    private Boolean visible;

    public Chain getStub(Path path) {
        return getStub(path.getChain());
    }

    public Chain getStub(String name) {
        logger.trace("get stub: {}", name);
        Chain stub = chains.get(name);
        return stub;
    }

    public boolean isEmpty() {
        return getChains() == null || getChains().isEmpty();
    }

    public Map<String, Chain> getChains() {
        return chains;
    }

    public void setChains(Map<String, Chain> stubs) {
        this.chains = stubs;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
}
