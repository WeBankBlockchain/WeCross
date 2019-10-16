package com.webank.wecross.network;

import com.webank.wecross.resource.Path;
import com.webank.wecross.stub.Stub;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Network {

    private Logger logger = LoggerFactory.getLogger(Network.class);

    private Map<String, Stub> stubs;

    // Access control
    private Boolean visible;

    public Stub getStub(Path path) {
        return getStub(path.getChain());
    }

    public Stub getStub(String name) {
        logger.trace("get stub: {}", name);

        Stub stub = stubs.get(name);
        try {
            stub.init();
        } catch (Exception e) {
            logger.error("Error while get stub:", e);
            return null;
        }
        return stub;
    }

    public Map<String, Stub> getStubs() {
        return stubs;
    }

    public void setStubs(Map<String, Stub> stubs) {
        this.stubs = stubs;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
}
