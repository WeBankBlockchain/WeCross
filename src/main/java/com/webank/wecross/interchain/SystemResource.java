package com.webank.wecross.interchain;

import com.webank.wecross.account.AccountManager;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.zone.ZoneManager;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

public class SystemResource {
    private AccountManager accountManager;
    private ZoneManager zoneManager;

    private Resource proxyResource;
    private Resource hubResource;

    private Timer timer = new HashedWheelTimer();

    public SystemResource() {}

    public SystemResource(
            AccountManager accountManager,
            ZoneManager zoneManager,
            Resource proxyResource,
            Resource hubResource) {
        this.accountManager = accountManager;
        this.zoneManager = zoneManager;
        this.proxyResource = proxyResource;
        this.hubResource = hubResource;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public void setAccountManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public ZoneManager getZoneManager() {
        return zoneManager;
    }

    public void setZoneManager(ZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }

    public Resource getProxyResource() {
        return proxyResource;
    }

    public void setProxyResource(Resource proxyResource) {
        this.proxyResource = proxyResource;
    }

    public Resource getHubResource() {
        return hubResource;
    }

    public void setHubResource(Resource hubResource) {
        this.hubResource = hubResource;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    @Override
    public String toString() {
        return "SystemResource{"
                + "proxyResource="
                + proxyResource
                + ", hubResource="
                + hubResource
                + '}';
    }
}
