package com.webank.wecross.stub.remote;

import com.webank.wecross.p2p.P2PMessageData;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.request.SetDataRequest;

public class SetDataRequestMessageData implements P2PMessageData {
    private String version = "0.1";
    private String path;
    private SetDataRequest data;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getMethod() throws Exception {

        String uri = Path.decode(path).toURI();
        return uri + "/setData";
    }

    public SetDataRequest getData() {
        return data;
    }

    public void setData(SetDataRequest data) {
        this.data = data;
    }
}
