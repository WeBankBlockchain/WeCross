package com.webank.wecross.stub.remote;

import com.webank.wecross.p2p.P2PMessageData;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.request.GetDataRequest;

public class GetDataRequestMessageData implements P2PMessageData {
    private String version = "0.1";
    private String path;
    private GetDataRequest data;

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
        return uri + "/getData";
    }

    public GetDataRequest getData() {
        return data;
    }

    public void setData(GetDataRequest data) {
        this.data = data;
    }
}
