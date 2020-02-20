package com.webank.wecross.zone;

import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.remote.RemoteStub;
import com.webank.wecross.utils.core.PathUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Zone {

    private Logger logger = LoggerFactory.getLogger(Zone.class);

    private Map<String, Stub> stubs = new HashMap<>();

    // Access control
    private Boolean visible;

    public Stub getStub(Path path) {
        return getStub(path.getChain());
    }

    public Stub getStub(String name) {
        logger.trace("get stub: {}", name);
        Stub stub = stubs.get(name);
        return stub;
    }

    public boolean isEmpty() {
        return getStubs() == null || getStubs().isEmpty();
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
