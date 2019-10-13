package com.webank.wecross.bcos.config;

import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelService {

    private Logger logger = LoggerFactory.getLogger(ChannelService.class);

    private int groupId;
    private String agencyName;
    private GroupChannelConnections groupChannelConnections;

    public Service getService(GroupChannelConnectionsConfig groupChannelConnectionsConfig) {
        Service channelService = new Service();
        channelService.setConnectSeconds(30);
        channelService.setOrgID(agencyName);
        logger.info("agencyName : {}", agencyName);
        channelService.setConnectSleepPerMillis(1);
        channelService.setGroupId(groupId);
        channelService.setAllChannelConnections(groupChannelConnectionsConfig);
        return channelService;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }

    public GroupChannelConnections getGroupChannelConnections() {
        return groupChannelConnections;
    }

    public void setGroupChannelConnections(GroupChannelConnections groupChannelConnections) {
        this.groupChannelConnections = groupChannelConnections;
    }
}
