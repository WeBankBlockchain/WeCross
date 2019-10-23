package com.webank.wecross.network;

import com.webank.wecross.core.PathUtils;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.remote.RemoteStub;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Network {

    private Logger logger = LoggerFactory.getLogger(Network.class);

    private Map<String, Stub> stubs = new HashMap<>();

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
            logger.error("Error while get stub:", e.getMessage());
            return null;
        }
        return stub;
    }

    public void addResource(Resource resource) throws Exception {
        String stubName = resource.getPath().getChain();
        stubs.putIfAbsent(stubName, new RemoteStub());
        stubs.get(stubName).addResource(resource);
    }

    public void removeResource(Path path, boolean ignoreLocal) throws Exception {
        Stub stub = getStub(path);
        if (stub != null) {
            stub.removeResource(path, ignoreLocal);

            if (stub.getPattern() == "remote") {
                // delete empty remote stub
                Map<String, Resource> resources = stub.getResources();
                if (resources == null || resources.size() == 0) {
                    logger.info("Delete remote stub " + stub);
                    stubs.remove(path.getChain());
                }
            }
        }
    }

    public void removeResource(Path path) throws Exception {
        removeResource(path, false);
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

    public Set<String> getAllStubResourceName(boolean ignoreRemote) {
        Set<String> ret = new HashSet<>();

        for (Map.Entry<String, Stub> entry : stubs.entrySet()) {
            String stubName = PathUtils.toPureName(entry.getKey());
            Set<String> allResourceName = entry.getValue().getAllResourceName(ignoreRemote);

            for (String resourceName : allResourceName) {
                ret.add(stubName + "." + PathUtils.toPureName(resourceName));
            }
        }
        return ret;
    }
}
