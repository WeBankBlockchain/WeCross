package com.webank.wecross.routine.htlc;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HTLCManager {
    Set<String> paths = new HashSet();
    List<HTLCTaskInfo> htlcTaskInfos;

    public Resource filterHTLCResource(Path path, Resource resource) {
        if (paths.contains(path.toString())) {
            return new HTLCResource(resource);
        }
        return resource;
    }

    public void addPath(String path) {
        paths.add(path);
    }

    public Set<String> getPaths() {
        return paths;
    }

    public void setPaths(Set<String> paths) {
        this.paths = paths;
    }

    public List<HTLCTaskInfo> getHtlcTaskInfos() {
        return htlcTaskInfos;
    }

    public void setHtlcTaskInfos(List<HTLCTaskInfo> htlcTaskInfos) {
        this.htlcTaskInfos = htlcTaskInfos;
    }
}
