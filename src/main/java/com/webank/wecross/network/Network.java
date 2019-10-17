package com.webank.wecross.network;

import com.webank.wecross.core.PathUtils;
import com.webank.wecross.resource.Path;
import com.webank.wecross.stub.Stub;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

    public Set<String> getAllStubResourceName() {
        Set<String> ret = new HashSet<>();

        for (Map.Entry<String, Stub> entry : stubs.entrySet()) {
            String stubName = PathUtils.toPureName(entry.getKey());
            Set<String> allResourceName = entry.getValue().getAllResourceName();

            for (String resourceName : allResourceName) {
                ret.add(stubName + "/" + PathUtils.toPureName(resourceName));
            }
        }
        return ret;
    }
}
