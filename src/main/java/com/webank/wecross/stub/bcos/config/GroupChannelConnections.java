package com.webank.wecross.stub.bcos.config;

import java.util.ArrayList;
import java.util.List;
import org.fisco.bcos.channel.handler.ChannelConnections;
import org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig;
import org.springframework.core.io.Resource;

public class GroupChannelConnections {

    private Resource caCert;
    private Resource sslCert;
    private Resource sslKey;
    List<ChannelConnections> allChannelConnections = new ArrayList<>();

    public GroupChannelConnectionsConfig getGroupChannelConnections() {
        GroupChannelConnectionsConfig groupChannelConnectionsConfig =
                new GroupChannelConnectionsConfig();
        groupChannelConnectionsConfig.setCaCert(caCert);
        groupChannelConnectionsConfig.setSslCert(sslCert);
        groupChannelConnectionsConfig.setSslKey(sslKey);
        groupChannelConnectionsConfig.setAllChannelConnections(allChannelConnections);
        return groupChannelConnectionsConfig;
    }

    public Resource getCaCert() {
        return caCert;
    }

    public void setCaCert(Resource caCert) {
        this.caCert = caCert;
    }

    public Resource getSslCert() {
        return sslCert;
    }

    public void setSslCert(Resource sslCert) {
        this.sslCert = sslCert;
    }

    public Resource getSslKey() {
        return sslKey;
    }

    public void setSslKey(Resource sslKey) {
        this.sslKey = sslKey;
    }

    public List<ChannelConnections> getAllChannelConnections() {
        return allChannelConnections;
    }

    public void setAllChannelConnections(List<ChannelConnections> allChannelConnections) {
        this.allChannelConnections = allChannelConnections;
    }
}
