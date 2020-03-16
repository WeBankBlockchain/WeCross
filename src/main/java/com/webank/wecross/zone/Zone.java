package com.webank.wecross.zone;

import com.webank.wecross.resource.Path;
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
        return getStubs() == null || getStubs().isEmpty();
    }

    public Map<String, Chain> getStubs() {
        return chains;
    }

    public void setStubs(Map<String, Chain> stubs) {
        this.chains = stubs;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
}
