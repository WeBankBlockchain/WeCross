package com.webank.wecross.stub.bcos;

import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelService {

    private Logger logger = LoggerFactory.getLogger(ChannelService.class);

    private Integer timeout;
    private int groupId;
    private String agencyName = "fisco";
    private GroupChannelConnections groupChannelConnections;

    public ChannelService() {}

    public ChannelService(
            Integer timeout, int groupId, GroupChannelConnections groupChannelConnections) {
        this.timeout = timeout;
        this.groupId = groupId;
        this.groupChannelConnections = groupChannelConnections;
    }

    public ChannelService(
            int groupId, String agencyName, GroupChannelConnections groupChannelConnections) {
        this.groupId = groupId;
        this.agencyName = agencyName;
        this.groupChannelConnections = groupChannelConnections;
    }

    public Service getService(GroupChannelConnectionsConfig groupChannelConnectionsConfig) {
        Service channelService = new Service();
        channelService.setConnectSeconds(2147483);
        channelService.setOrgID(agencyName);
        logger.info("agencyName : {}", agencyName);
        channelService.setConnectSleepPerMillis(1);
        channelService.setGroupId(groupId);
        channelService.setAllChannelConnections(groupChannelConnectionsConfig);
        return channelService;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
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
